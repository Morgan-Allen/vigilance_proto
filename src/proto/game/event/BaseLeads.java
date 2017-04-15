

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
      if (lead.assigned().empty() || lead.complete()) {
        leads.remove(lead);
        continue;
      }
      lead.updateAssignment();
    }
  }
  
  
  public Lead leadFor(Element focus, Lead.Type type) {
    for (Lead lead : leads) {
      if (lead.focus == focus && lead.type == type) {
        return lead;
      }
    }
    Lead lead = new Lead(base, type, focus);
    leads.add(lead);
    return lead;
  }
  
  
  public void closeLead(Lead lead) {
    leads.remove(lead);
  }
  
  
  public CaseFile caseFor(Plot plot) {
    CaseFile match = files.get(plot);
    if (match != null) return match;
    
    final CaseFile file = new CaseFile(base, plot);
    plot.caseID = nextCaseID++;
    files.put(plot, file);
    return file;
  }
  
  
  public Series <Lead> leadsFor(Element focus) {
    final Batch <Lead> all = new Batch();
    boolean canMeet = focus.canEnter(base);
    if (! atKnownLocation(focus)) return all;
    
    if (focus.isPerson()) {
      all.add(leadFor(focus, Lead.LEAD_SURVEIL_PERSON));
      if (canMeet) all.add(leadFor(focus, Lead.LEAD_QUESTION));
    }
    if (focus.isPlace()) {
      all.add(leadFor(focus, Lead.LEAD_SURVEIL_BUILDING));
      all.add(leadFor(focus, Lead.LEAD_WIRETAP));
      if (canMeet) all.add(leadFor(focus, Lead.LEAD_SEARCH));
    }
    if (focus.isRegion()) {
      all.add(leadFor(focus, Lead.LEAD_PATROL));
      all.add(leadFor(focus, Lead.LEAD_SCAN));
      if (canMeet) all.add(leadFor(focus, Lead.LEAD_CANVASS));
    }
    return all;
  }
  
  
  
  /**  Utility methods for getting broad sets of active clues:
    */
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
  
  
  
  /**  Helper methods for determining which plots and suspects should be tailed
    *  most urgently-
    */
  public Series <Plot> activePlots() {
    Batch <Plot> known = new Batch();
    for (Clue c : cluesFor(null, null, null, null, false)) {
      if (c.plot.complete()) continue;
      known.include(c.plot);
    }
    return known;
  }
  
  
  public Series <Plot.Role> knownRolesFor(Plot plot) {
    Batch <Plot.Role> known = new Batch();
    known.include(Plot.ROLE_MASTERMIND);
    known.include(Plot.ROLE_ORGANISER );
    known.include(Plot.ROLE_TARGET    );
    
    for (CaseFile file : files.values()) {
      for (Clue c : file.clues) if (c.plot == plot) {
        known.include(c.role);
      }
    }
    return known;
  }
  
  
  public Series <Plot> activePlotsForRegion(Region r) {
    Batch <Plot> known = new Batch();
    for (Clue c : cluesFor(null, null, null, r, false)) {
      if (c.plot.complete() || ! c.isConfirmation()) continue;
      known.include(c.plot);
    }
    return known;
  }
  
  
  public Series <Element> activeSuspectsForRegion(Region r) {
    Batch <Element> known = new Batch();
    for (Clue c : cluesFor(null, null, null, r, false)) {
      if (c.plot.complete() || ! c.isConfirmation()) continue;
      known.include(c.match);
    }
    return known;
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
  
  
  
  /**  Helper methods specific to individual suspects...
    */
  public Series <Plot> involvedIn(Element subject, boolean confirmedOnly) {
    Batch <Plot> matches = new Batch();
    for (Clue c : cluesFor(null, subject, null, null, false)) {
      if (confirmedOnly && ! c.isConfirmation()) continue;
      matches.include(c.plot);
    }
    return matches;
  }
  
  
  public Series <Element> suspectsFor(Plot.Role role, Plot plot) {
    Series <Clue> related = cluesFor(plot, null, role, null, false);
    Batch <Element> matches = new Batch();
    
    for (Clue c : related) if (c.isConfirmation()) {
      matches.add(c.match);
      return matches;
    }
    
    //  TODO:  This will have to do separate eliminations for locations and
    //  their suspects now- either that, or have the 'cluesFor' method include
    //  associated locations...?
    
    search: for (Element e : base.world().inside()) {
      for (Clue c : related) if (! c.matchesSuspect(e)) continue search;
      matches.add(e);
    }
    return matches;
  }
  
  
  public float evidenceAgainst(Element subject, Plot plot) {
    CaseFile file = caseFor(plot);
    float evidence = 0;
    for (Clue c : file.clues) if (c.match == subject) {
      evidence += c.leadType.confidence;
    }
    return evidence;
  }
  
  
  
  /**  ...and to their known locations:
    */
  public Series <Element> possibleLocations(Plot.Role role, Plot plot) {
    //  TODO:  Return these values!
    Batch <Element> matches = new Batch();
    return matches;
  }
  
  
  public boolean atKnownLocation(Element suspect) {
    return suspect.place() == lastKnownLocation(suspect);
  }
  
  
  public Place lastKnownLocation(Element suspect) {
    
    if (suspect.isPerson()) {
      Person p = (Person) suspect;
      if (p.isCaptive()) return p.place();
    }
    else if (suspect.isPlace()) {
      return (Place) suspect;
    }
    else if (suspect.isRegion()) {
      return null;
    }
    
    Series <Clue> clues = cluesFor(suspect, true);
    for (Clue c : clues) {
      if (! c.isLocationClue()) continue;
      if (c.nearRange != 0 || ! c.location.isPlace()) continue;
      return (Place) c.location;
    }
    return null;
  }
}





