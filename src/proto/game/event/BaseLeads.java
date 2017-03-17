

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
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadTable(files);
    s.loadObjects(leads);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveTable(files);
    s.saveObjects(leads);
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
    return file;
  }
  
  
  
  /**  Assorted utility methods-
    */
  public Series <Plot> knownPlots() {
    Batch <Plot> known = new Batch();
    for (CaseFile file : files.values()) {
      for (Clue c : file.clues) if (! c.plot.complete()) {
        known.include(c.plot);
      }
    }
    return known;
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
  
  
  public Series <Element> suspectsFor(Plot.Role role, Plot plot) {
    Series <Clue> related = cluesFor(plot, null, role);
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
  
  
  public Series <Clue> cluesFor(
    Plot plot, Object match, Plot.Role role
  ) {
    Batch <Clue> matches = new Batch();
    for (CaseFile file : files.values()) {
      for (Clue c : file.clues) {
        if (plot  != null && plot  != c.plot ) continue;
        if (match != null && match != c.match) continue;
        if (role  != null && role  != c.role ) continue;
        matches.add(c);
      }
    }
    return matches;
  }
  
  
  public Series <Plot> involvedIn(Object subject) {
    Batch <Plot> matches = new Batch();
    CaseFile file = caseFor(subject);
    
    for (Clue c : file.clues) {
      matches.include(c.plot);
    }
    return matches;
  }
  
  
  public float evidenceForInvolvement(Plot plot, Object subject) {
    CaseFile file = caseFor(subject);
    float evidence = 0;
    
    for (Clue c : file.clues) if (c.plot == plot) {
      evidence += c.confidence;
    }
    return evidence;
  }
  
  
  
}




