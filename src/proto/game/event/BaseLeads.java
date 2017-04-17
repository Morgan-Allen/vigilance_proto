

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
  
  List <Clue> newClues = new List();
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadTable(files);
    s.loadObjects(leads);
    nextCaseID = s.loadInt();
    s.loadObjects(newClues);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveTable(files);
    s.saveObjects(leads);
    s.saveInt(nextCaseID);
    s.saveObjects(newClues);
  }
  
  
  
  /**  Extracting any new clues accumulated (mostly for UI/debug purposes)-
    */
  public Series <Clue> extractNewClues() {
    final Batch <Clue> extracts = new Batch();
    Visit.appendTo(extracts, newClues);
    newClues.clear();
    return extracts;
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
        return 0 - r.timeFound;
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
  
  
  public CaseFile caseFor(Plot plot) {
    CaseFile match = files.get(plot);
    if (match != null) return match;
    
    final CaseFile file = new CaseFile(base, plot);
    plot.caseID = nextCaseID++;
    files.put(plot, file);
    return file;
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
    known.include(Plot.ROLE_ORGANISER);
    known.include(Plot.ROLE_TARGET   );
    
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
    
    search: for (Element e : base.world().inside()) {
      for (Clue c : related) if (! c.matchesSuspect(e)) continue search;
      matches.add(e);
    }
    return matches;
  }
  
  
  public float evidenceAgainst(
    Element subject, Plot plot, boolean confirmedOnly
  ) {
    CaseFile file = caseFor(plot);
    Plot.Role role = plot.roleFor(subject);
    float evidence = 0;
    
    for (Clue c : file.clues) {
      if (confirmedOnly && ! c.isConfirmation()) {
        continue;
      }
      if (c.isConfirmation() && c.match == subject) {
        evidence += c.leadType.confidence;
      }
      if (c.role == role) {
        evidence += c.leadType.confidence / 2;
      }
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
    Place last = lastKnownLocation(suspect);
    Place at = suspect.place();
    return at == last;
  }
  
  
  public Place lastKnownLocation(Element suspect) {
    if (suspect.isPlace()) {
      return (Place) suspect;
    }
    if (suspect.isRegion()) {
      return null;
    }
    
    Place address = null;
    
    if (suspect.isPerson()) {
      Person p = (Person) suspect;
      address = p.isCivilian() ? p.resides() : null;
      if (p.place() == address) return address;
    }
    
    Pick <Place> pickL = new Pick();
    for (Clue c : cluesFor(suspect, false)) {
      Place given = c.locationGiven();
      if (given != null) pickL.compare(given, c.timeFound);
    }
    if (! pickL.empty()) {
      return pickL.result();
    }
    
    return address;
  }
  
  
  
  /**  And finally, for obtaining and updating Leads to acquire more
    *  information-
    */
  public void updateLeads() {
    newClues.clear();
    
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
  
  
  public Series <Lead> leadsFor(Element focus) {
    final Batch <Lead> all = new Batch();
    Place   scene    = lastKnownLocation(focus);
    boolean canFind  = scene == focus.place();
    boolean canEnter = scene != null && scene.canEnter(base);
    
    if (focus.isPerson() && canFind) {
      Person suspect = (Person) focus;
      boolean victim = suspect.isCivilian();
      boolean perp   = suspect.isCriminal();
      
      all.add(leadFor(suspect, Lead.LEAD_SURVEIL_PERSON));
      if (canEnter) all.add(leadFor(suspect, Lead.LEAD_QUESTION));
      if (victim  ) all.add(leadFor(suspect, Lead.LEAD_GUARD   ));
      if (perp    ) all.add(leadFor(suspect, Lead.LEAD_BUST    ));
    }
    
    if (focus.isPerson() && canEnter) {
      all.add(leadFor(scene, Lead.LEAD_SURVEIL_BUILDING));
      all.add(leadFor(scene, Lead.LEAD_WIRETAP         ));
      all.add(leadFor(scene, Lead.LEAD_SEARCH          ));
    }
    
    if (focus.isPlace()) {
      all.add(leadFor(focus, Lead.LEAD_SURVEIL_BUILDING));
      all.add(leadFor(focus, Lead.LEAD_WIRETAP         ));
      all.add(leadFor(focus, Lead.LEAD_BUST            ));
      if (canEnter) {
        all.add(leadFor(focus, Lead.LEAD_SEARCH));
        all.add(leadFor(focus, Lead.LEAD_GUARD ));
      }
    }
    
    if (focus.isRegion()) {
      all.add(leadFor(focus, Lead.LEAD_PATROL ));
      all.add(leadFor(focus, Lead.LEAD_SCAN   ));
      all.add(leadFor(focus, Lead.LEAD_CANVASS));
    }
    
    return all;
  }
  
  
  public boolean suspectIsVictim(Element suspect) {
    return suspectHasRole(suspect, Plot.ROLE_TARGET);
  }
  
  
  public boolean suspectIsBoss(Element suspect) {
    return suspectHasRole(suspect, Plot.ROLE_ORGANISER, Plot.ROLE_MASTERMIND);
  }
  
  
  public boolean suspectIsUrgent(Element suspect) {
    return suspectIsVictim(suspect) || suspectIsBoss(suspect);
  }
  
  
  private boolean suspectHasRole(Element suspect, Plot.Role... roles) {
    for (Clue clue : this.cluesFor(suspect, true)) {
      Plot plot = clue.plot();
      if (plot.complete()) continue;
      
      for (Plot.Role r : roles) {
        Element fills = plot.filling(r);
        if (suspect == fills) return true;
      }
    }
    return false;

  }
}






