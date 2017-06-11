

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.event.LeadType.*;



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
  
  
  public Series <Clue> cluesFor(Plot plot, Role role, boolean sort) {
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
    Plot plot, Element match, Role role, Region area, boolean sort
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
    file.caseID = nextCaseID++;
    files.put(plot, file);
    return file;
  }
  
  
  
  /**  Helper methods for determining which plots and suspects should be tailed
    *  most urgently-
    */
  public Series <Plot> knownPlots() {
    Batch <Plot> known = new Batch();
    for (Clue c : cluesFor(null, null, null, null, false)) {
      if (! c.plot.complete()) {
        known.include(c.plot);
      }
      else if (base.world().events.recent().includes(c.plot)) {
        known.include(c.plot);
      }
    }
    return known;
  }
  
  
  public Series <Role> knownRolesFor(Plot plot) {
    Batch <Role> known = new Batch();
    known.include(Plot.ROLE_ORGANISER);
    known.include(Plot.ROLE_TARGET   );
    
    for (CaseFile file : files.values()) {
      for (Clue c : file.clues) if (c.plot == plot) {
        known.include(c.role);
      }
    }
    return known;
  }
  
  
  public Series <Clue> latestPlotClues(Region r) {
    //
    //  Returns the most recent clue on the identity of participants in any
    //  active plot, if that most recent clue was found in the region.
    Batch <Clue> latest = new Batch();
    
    for (Plot plot : knownPlots()) {
      Series <Clue> clues = cluesFor(plot, null, null, null, false);
      Batch <Role> roles = new Batch();
      for (Clue c : clues) roles.include(c.role());
      
      loop: for (Role role : roles) {
        if (Visit.arrayIncludes(Plot.NEVER_SHOW, role)) continue;
        
        Series <Clue> roleClues = cluesFor(plot, role, true);
        Clue top = roleClues.first();
        
        for (Clue c : roleClues) if (c.isConfirmation()) {
          Place at = c.locationGiven();
          if (at != null && at.region() == r) latest.add(c);
          continue loop;
        }
        
        if (top != null && top.found().region() == r) latest.add(top);
      }
    }
    
    return latest;
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
  
  
  public boolean suspectConfirmed(Element subject, Role role, Plot plot) {
    Series <Clue> related = this.cluesFor(plot, subject, role, null, false);
    for (Clue c : related) if (c.isConfirmation()) return true;
    return false;
  }
  
  
  public Series <Element> suspectsFor(Role role, Plot plot) {
    //
    //  NOTE:  In the event that we're looking for suspects for the role of
    //  headquarters or mastermind, we include clues from ALL plots, as these
    //  should be persistent between multiple plots.
    boolean allPlots = Visit.arrayIncludes(Plot.PERSIST_ROLES, role);
    Series <Clue> related = cluesFor(allPlots ? null : plot, role, true);
    Element singleMatch = null;
    List <Element> matches = new List();
    Visit.appendTo(matches, base.world().inside());
    //
    //  ...However, for persistent roles we skip clues for different bases, and
    //  any evidence collected before an earlier assault (which could trigger
    //  the move to different headquarters or picking a new mastermind.)
    for (Clue c : related) {
      if (allPlots && c.plot != plot) {
        if (c.plot.base() != plot.base()) continue;
        if (c.leadType == LeadType.MAJOR_BUST) break;
      }
      //
      //  Finally, note that any exact confirmations of the suspect for a role
      //  will return a single match.  Otherwise, we screen out any matches
      //  that don't fit with a given clue.
      if (c.isConfirmation()) {
        singleMatch = c.match();
        break;
      }
      for (ListEntry <Element> l = matches; (l = l.nextEntry()) != matches;) {
        if (! c.matchesSuspect(l.refers)) matches.removeEntry(l);
      }
    }
    if (singleMatch != null) { matches.clear(); matches.add(singleMatch); }
    return matches;
  }
  
  
  public float evidenceAgainst(
    Element subject, Plot plot, boolean confirmedOnly
  ) {
    CaseFile file = caseFor(plot);
    Role     role = plot.roleFor(subject);
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
  public Series <Element> possibleLocations(Role role, Plot plot) {
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
      address = p.resides();
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
  
  
  public Lead leadFor(Element focus, LeadType type) {
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
  
  
  public boolean suspectIsVictim(Element suspect) {
    return suspectHasRole(suspect, Plot.ROLE_TARGET, Plot.ROLE_SCENE);
  }
  
  
  public boolean suspectIsBoss(Element suspect) {
    return suspectHasRole(suspect,
      Plot.ROLE_ORGANISER, Plot.ROLE_MASTERMIND,
      Plot.ROLE_HIDEOUT  , Plot.ROLE_HQ
    );
  }
  
  
  public boolean suspectIsUrgent(Element suspect) {
    return suspectIsVictim(suspect) || suspectIsBoss(suspect);
  }
  
  
  private boolean suspectHasRole(Element suspect, Role... roles) {
    for (Clue clue : this.cluesFor(suspect, true)) {
      Plot plot = clue.plot();
      if (plot.complete()) continue;
      
      for (Role r : roles) {
        Element fills = plot.filling(r);
        if (suspect == fills) return true;
      }
    }
    return false;
  }
  
  
  public Series <Lead> leadsFor(Element focus) {
    
    boolean canFind  = atKnownLocation(focus);
    boolean canGuard = canFind && suspectIsVictim(focus) && focus.isPlace();
    boolean canBust  = canFind && suspectIsBoss  (focus) && focus.isPlace();
    
    final Batch <Lead> all = new Batch();
    
    if (canFind) for (LeadType type : LeadType.STANDARD_LEADS) {
      if (type.canFollow(focus)) {
        all.add(leadFor(focus, type));
      }
    }
    if (canGuard) all.add(leadFor(focus, LeadType.GUARD));
    if (canBust ) all.add(leadFor(focus, LeadType.BUST ));
    
    return all;
  }
}








