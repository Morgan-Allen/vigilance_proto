

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class BaseLeads {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  Table <Object, CaseFile> files = new Table();
  List <Lead> leads = new List();
  int nextCaseID = 0;
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadTable(files);
    s.loadObjects(leads);
    nextCaseID = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveTable(files);
    s.saveObjects(leads);
    s.saveInt(nextCaseID);
  }
  
  
  
  /**  Providing and updating Leads-
    */
  public void updateLeads() {
    for (Lead lead : leads) {
      if (lead.assigned().empty()) {
        leads.remove(lead);
        continue;
      }
      lead.updateAssignment();
    }
  }
  
  
  Lead leadFor(Element focus, Lead.Type type) {
    for (Lead lead : leads) {
      if (lead.focus == focus && lead.type == type) {
        return lead;
      }
    }
    Lead lead = new Lead(base, type, focus);
    leads.add(lead);
    return lead;
  }
  
  
  public Series <Lead> leadsFor(Element focus) {
    final Batch <Lead> all = new Batch();
    if (focus.isPerson()) {
      all.add(leadFor(focus, Lead.LEAD_SURVEIL_PERSON));
      all.add(leadFor(focus, Lead.LEAD_QUESTION));
    }
    if (focus.isPlace()) {
      all.add(leadFor(focus, Lead.LEAD_SURVEIL_BUILDING));
      all.add(leadFor(focus, Lead.LEAD_WIRETAP));
    }
    if (focus.isRegion()) {
      all.add(leadFor(focus, Lead.LEAD_PATROL));
      all.add(leadFor(focus, Lead.LEAD_CANVASS));
    }
    return all;
  }
  
  
  public CaseFile caseFor(Object subject) {
    CaseFile match = files.get(subject);
    if (match != null) return match;
    final CaseFile file = new CaseFile(base, subject);
    files.put(subject, file);
    
    if (subject instanceof Plot) {
      Plot plot = (Plot) subject;
      plot.caseID = nextCaseID++;
    }
    return file;
  }
  
  
  
  /**  Assorted utility methods-
    */
  public Series <Plot> knownPlots() {
    Batch <Plot> known = new Batch();
    for (Clue c : cluesFor(null, null, null, null, false)) {
      known.include(c.plot);
    }
    return known;
  }
  
  
  public Series <Plot> knownPlotsForRegion(Region r) {
    Batch <Plot> known = new Batch();
    for (Clue c : cluesFor(null, null, null, r, false)) if (c.confirmed) {
      known.include(c.plot);
    }
    return known;
  }
  
  
  public Series <Element> knownSuspectsForRegion(Region r) {
    Batch <Element> known = new Batch();
    for (Clue c : cluesFor(null, null, null, r, false)) if (c.confirmed) {
      known.include(c.match);
    }
    return known;
  }
  
  
  public Series <Clue> cluesFor(Plot plot, Element match, boolean sort) {
    return cluesFor(plot, match, null, null, sort);
  }
  
  
  public Series <Clue> cluesFor(Plot plot, Plot.Role role, boolean sort) {
    return cluesFor(plot, null, role, null, sort);
  }
  
  
  public Series <Clue> cluesFor(Element match, boolean sort) {
    if (match.isRegion()) {
      return cluesFor(null, null, null, (Region) match, sort);
    }
    else return cluesFor(null, match, null, null, sort);
  }
  
  
  public Series <Clue> cluesFor(Plot plot, boolean sort) {
    return cluesFor(plot, null, null, null, sort);
  }
  
  
  public Series <Clue> cluesFor(
    Plot plot, Element match, Plot.Role role, Region area, boolean sort
  ) {
    List <Clue> matches = new List <Clue> () {
      protected float queuePriority(Clue r) {
        return r.timeFound;
      }
    };
    for (CaseFile file : files.values()) {
      for (Clue c : file.clues) {
        if (plot  != null && plot  != c.plot ) continue;
        if (match != null && match != c.match) continue;
        if (role  != null && role  != c.role ) continue;
        if (area  != null) {
          if (c.match == null         ) continue;
          if (c.match.region() != area) continue;
        }
        matches.add(c);
      }
    }
    
    if (sort) matches.queueSort();
    return matches;
  }
  
  
  public Series <Element> suspectsFor(Plot.Role role, Plot plot) {
    Series <Clue> related = cluesFor(plot, null, role, null, false);
    Batch <Element> matches = new Batch();
    
    for (Clue c : related) if (c.confirmed) {
      matches.add(c.match);
      return matches;
    }
    
    search: for (Element e : base.world().inside()) {
      for (Clue c : related) if (! c.matchesSuspect(e)) continue search;
      matches.add(e);
    }
    return matches;
  }
  
  
  public Series <Plot> involvedIn(Element subject, boolean confirmedOnly) {
    Batch <Plot> matches = new Batch();
    CaseFile file = caseFor(subject);
    
    for (Clue c : file.clues) {
      if (confirmedOnly && ! c.confirmed) continue;
      matches.include(c.plot);
    }
    return matches;
  }
  
  
  public Series <Plot.Role> knownRolesFor(Plot plot) {
    Batch <Plot.Role> known = new Batch();
    known.include(Plot.ROLE_HIDEOUT  );
    known.include(Plot.ROLE_ORGANISER);
    known.include(Plot.ROLE_TARGET   );
    
    for (CaseFile file : files.values()) {
      for (Clue c : file.clues) if (c.plot == plot) {
        known.include(c.role);
      }
    }
    return known;
  }
  
  
  public float evidenceForInvolvement(Plot plot, Element subject) {
    CaseFile file = caseFor(subject);
    float evidence = 0;
    
    for (Clue c : file.clues) if (c.plot == plot) {
      evidence += c.confidence;
    }
    return evidence;
  }
  
  
  public boolean plotIsUrgent(Plot plot) {
    return involvedIn(plot.target(), true).includes(plot);
  }
  
  
  public boolean suspectIsUrgent(Element suspect) {
    for (Plot plot : involvedIn(suspect, true)) {
      if (suspect == plot.target()) return true;
    }
    return false;
  }
  
}





