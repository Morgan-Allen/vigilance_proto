

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class CaseFile implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  //  TODO:  Consider making this specific to Crimes, rather than arbitrary
  //  objects?
  final public Object subject;
  List <Clue> clues = new List();
  
  
  
  CaseFile(Base base, Object subject) {
    this.base    = base   ;
    this.subject = subject;
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base) s.loadObject();
    subject =        s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      clues.add(Clue.loadClue(s));
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base   );
    s.saveObject(subject);
    
    s.saveInt(clues.size());
    for (Clue c : clues) c.saveClue(s);
  }
  
  
  
  /**  Resolving possible suspects:
    */
  public void recordClue(Clue clue) {
    for (Clue prior : clues) {
      if (prior.matchesType(clue)) return;
    }
    clues.add(clue);
  }
  
  
  public Series <Element> matchingSuspects(
    Series <? extends Element> possible
  ) {
    Batch <Element> matches = new Batch();
    search: for (Element e : possible) {
      for (Clue c : clues) if (! c.matchesSuspect(e)) continue search;
      matches.add(e);
    }
    return matches;
  }
  
  
  
  /**  Extraction methods for use in sentencing-
    */
  public void updateEvidenceFrom(CaseFile other) {
    //  TODO:  Restore this!
    /*
    for (Role role : other.roles) for (Lead lead : role.evidence) {
      recordRole(role.event, role.roleID, lead);
    }
    //*/
  }
  
  
}



/*
public class CaseFile implements Session.Saveable {
  
  
  final public static int
    ROLE_NONE    = -1,
    ROLE_CLUE    = -2,
    ROLE_GOON    = -3,
    ROLE_HIDEOUT = -4,
    ROLE_CRIME   = -5,
    ROLE_SCENE   = -6;
  final public static float
    LEVEL_TIPOFF    = 0.5f,
    LEVEL_EVIDENCE  = 1.0f,
    LEVEL_CONVICTED = 1.5f;
  
  final Base base;
  final public Object subject;
  
  Place knownLocation = null;
  
  private static class Role {
    Event event;
    int   roleID   = -1;
    int   sentence = -1;
    float maxEvidence = 0;
    boolean isPast = false;
    List <Lead> evidence = new List();
  }
  List <Role> roles = new List();
  
  //  NOTE:  This is intended for temporary storage and will get wiped whenever
  //  changes are made.
  private boolean refreshOptions = false;
  private List <Lead> followOptions = new List();
  
  
  CaseFile(Base base, Object subject) {
    this.base    = base   ;
    this.subject = subject;
    knownLocation = trueLocation();
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base) s.loadObject();
    subject =        s.loadObject();
    
    knownLocation = (Place) s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      final Role r = new Role();
      r.event       = (Event) s.loadObject();
      r.roleID      = s.loadInt();
      r.sentence    = s.loadInt();
      r.maxEvidence = s.loadFloat();
      r.isPast      = s.loadBool();
      s.loadObjects(r.evidence);
      roles.add(r);
    }
    
    refreshOptions = s.loadBool();
    s.loadObjects(followOptions);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base   );
    s.saveObject(subject);
    
    s.saveObject(knownLocation);
    
    s.saveInt(roles.size());
    for (Role r : roles) {
      s.saveObject(r.event      );
      s.saveInt   (r.roleID     );
      s.saveInt   (r.sentence   );
      s.saveFloat (r.maxEvidence);
      s.saveBool  (r.isPast     );
      s.saveObjects(r.evidence);
    }
    s.saveBool(refreshOptions);
    s.saveObjects(followOptions);
  }
  
  
  
  /**  Recording involvement in criminal actions and other information
    *  updates-
    */
  /*
  public Role recordCurrentRole(Event event, Lead lead) {
    int roleID = Visit.indexOf(subject, event.planStep().needs());
    if (roleID == -1) { I.complain("Subject not involved!"); return null; }
    return recordRole(event, roleID, lead);
  }
  
  
  public Role recordRole(Event event, int roleID, Lead lead) {
    //
    //  Try to find a pre-existing role which matches this signature, or create
    //  a new one otherwise.  Quit if the same lead has already been recorded.
    //  TODO:  Leads might want to supply differing levels of evidence for
    //  differing subjects!
    Role role = null;
    for (Role r : roles) if (r.event == event && r.roleID == roleID) {
      role = r; break;
    }
    if (role == null) {
      roles.add(role = new Role());
      role.event  = event ;
      role.roleID = roleID;
    }
    else for (Lead l : role.evidence) {
      if (l.matchType(lead)) return null;
    }
    //
    //  Record the new evidence, refresh options and return-
    role.maxEvidence = Nums.max(role.maxEvidence, lead.evidenceLevel());
    role.evidence.include(lead);
    
    StringBuffer roleDesc = new StringBuffer();
    shortDescription(role, roleDesc);
    base.world().events.log(roleDesc.toString());
    
    refreshInvestigationOptions();
    base.world().pauseMonitoring();
    return role;
  }
  
  
  void updateLocation(Place location) {
    this.knownLocation = location;
    refreshInvestigationOptions();
  }
  
  
  void updateLeads() {
    boolean refresh = false;
    for (Role r : roles) if (r.event.complete() != r.isPast) {
      r.isPast = true;
      refresh = true;
    }
    if (refresh) refreshInvestigationOptions();
    
    for (Lead lead : investigationOptions()) {
      if (lead.assigned().empty()) continue;
      lead.updateAssignment();
    }
  }
  
  
  
  /**  Information queries relevant to investigation options-
    */
  /*
  Place knownLocation() {
    return knownLocation;
  }
  
  
  Place trueLocation() {
    if (subject instanceof Element) {
      return ((Element) subject).place();
    }
    if (subject instanceof Event) {
      return ((Event) subject).targetLocation();
    }
    return null;
  }
  
  
  private boolean defunct(Role role) {
    final PlanStep step = role.event.planStep();
    if (step == null     ) return true;
    if (role.sentence > 0) return true;
    final Base agentBase = step.plan.agent.base();
    if (agentBase.plans.planComplete(step.plan)) return true;
    return false;
  }
  
  
  Event lastSuspectEvent() {
    for (Role role : roles) {
      if (defunct(role)) continue;
      return role.event;
    }
    return null;
  }
  
  
  Event nextEventInvolved() {
    int worldTime = base.world().timing.totalHours();
    float minTime = Float.POSITIVE_INFINITY;
    Event picked = null;
    
    for (Role role : roles) {
      if (defunct(role)) continue;
      if (role.event.timeEnds() <= worldTime) continue;
      
      int time = role.event.timeBegins();
      if (time < minTime) { minTime = time; picked = role.event; }
    }
    return picked;
  }
  
  
  Event eventWithRole(int roleID) {
    for (Role role : roles) {
      if (defunct(role)) continue;
      if (role.roleID == roleID) return role.event;
    }
    return null;
  }
  
  
  Event threatens(Element subject) {
    for (Role role : roles) {
      if (defunct(role)) continue;
      final PlanStep step = role.event.planStep();
      if (step.type.harmLevel(step, subject) > 0) return role.event;
    }
    return null;
  }
  
  
  public Series <Event> involvedIn() {
    Batch <Event> involved = new Batch();
    for (Role role : roles) {
      if (defunct(role)) continue;
      involved.add(role.event);
    }
    return involved;
  }
  
  
  public float evidenceForInvolvement(Event event) {
    float evidence = 0;
    for (Role role : roles) if (role.event == event) {
      evidence += role.maxEvidence;
    }
    return evidence;
  }
  
  
  public void updateEvidenceFrom(CaseFile other) {
    for (Role role : other.roles) for (Lead lead : role.evidence) {
      recordRole(role.event, role.roleID, lead);
    }
  }
  
  
  
  /**  Generating subsequent investigation options-
    */
  /*
  public void refreshInvestigationOptions() {
    refreshOptions = true;
    investigationOptions();
  }
  
  
  private void tryAddingOption(
    Lead option, Series <Lead> options, Series <Lead> oldOptions
  ) {
    for (Lead l : oldOptions) if (l.matchType(option)) {
      options.add(l);
      return;
    }
    options.add(option);
    base.world().events.log("New lead: "+option);
  }
  
  
  private void scrubOldOptions(
    Series <Lead> options, Series <Lead> oldOptions
  ) {
    for (Lead l : oldOptions) if (! options.includes(l)) {
      base.world().events.log("Lead closed: "+l);
    }
  }
  
  
  public Series <Lead> investigationOptions() {
    
    //  TODO:  You also need to check whether the conditions for a given lead
    //  (i.e, based on timing,) have expired.
    for (Lead l : followOptions) if (l.complete()) {
      followOptions.remove(l);
    }
    
    if (! refreshOptions) return followOptions;
    final Series <Lead> oldOptions = followOptions;
    followOptions = new List();
    refreshOptions = false;
    
    
    //  TODO:  I'm going to return to these later, and gradually add more
    //  options.
    if (leadsTo instanceof Item || leadsTo instanceof Clue) {
    }
    
    if (subject instanceof Person) {
      final Person person = (Person) subject;
      final Event next      = nextEventInvolved();
      final Event suspected = lastSuspectEvent();
      final Event threatens = base.leads.threateningEvent(person);
      
      if (next != null && ! next.complete()) {
        Lead tailing = new LeadSurveil(base, person);
        tryAddingOption(tailing, followOptions, oldOptions);
      }
      if (threatens != null && ! threatens.complete()) {
        Lead guarding = new LeadGuard(base, person.place(), threatens);
        tryAddingOption(guarding, followOptions, oldOptions);
      }
      if (suspected != null) {
        //  TODO:  Include questioning and arrest options.
      }
    }
    
    if (subject instanceof Place) {
      final Place place = (Place) subject;
      Event crime = eventWithRole(ROLE_SCENE  );
      Event lair  = eventWithRole(ROLE_HIDEOUT);
      
      if (crime != null && crime.dangerous() && ! crime.complete()) {
        Lead guarding = new LeadGuard(base, place, crime);
        tryAddingOption(guarding, followOptions, oldOptions);
      }
      if (crime != null && ! crime.complete()) {
        Lead surveil = new LeadSurveil(base, place);
        tryAddingOption(surveil, followOptions, oldOptions);
      }
      if (lair != null) {
        //  TODO:  Include the option to raid.
      }
    }
    
    scrubOldOptions(followOptions, oldOptions);
    return followOptions;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  /*
  final static Table <Integer, String> ROLE_DESC = Table.make(
    ROLE_GOON   , "<subject> <tense> an accomplice during the <event>.",
    ROLE_HIDEOUT, "<subject> <tense> a criminal hideout.",
    ROLE_CRIME  , "<event> <tense> planned.",
    ROLE_SCENE  , "<subject> <tense> the venue for <event>."
  );
  
  
  private Role pickMostProminentRole() {
    int time = base.world().timing.totalHours();
    float bestRating = 0;
    Role active = null;
    
    for (Role role : roles) {
      if (defunct(role)) continue;
      int daysAgo = (time - role.event.timeEnds()) / World.MINUTES_PER_DAY;
      float rating = role.maxEvidence;
      if (role.roleID <= ROLE_NONE) rating /= 2;
      rating *= 10f / (10 + daysAgo);
      if (rating > bestRating) { active = role; bestRating = rating; }
    }
    return active;
  }
  
  
  public void shortDescription(StringBuffer s) {
    final Role role = pickMostProminentRole();
    if (role == null) {
      s.append("You have no current leads on "+subject);
      return;
    }
    shortDescription(role, s);
  }
  
  
  public void shortDescription(Object roleRef, StringBuffer s) {
    if (! (roleRef instanceof Role)) return;
    
    final Role role = (Role) roleRef;
    Lead lead = role.evidence.last();
    String tenseDesc = "is", strengthDesc = "suggests";
    if (! role.event.hasBegun()) tenseDesc = "will be";
    else if (role.event.complete()) tenseDesc = "was";
    if (role.maxEvidence >= LEVEL_EVIDENCE) strengthDesc = "confirmed";
    
    String desc = ROLE_DESC.get((Integer) role.roleID);
    if (desc != null) {
      String subDesc = desc;
      subDesc = subDesc.replace("<subject>", subject   .toString());
      subDesc = subDesc.replace("<event>"  , role.event.toString());
      subDesc = subDesc.replace("<tense>"  , tenseDesc            );
      s.append(lead.activeInfo()+" "+strengthDesc+" ");
      s.append(subDesc);
    }
    else {
      //  TODO:  Move this out to the Leads themselves, so they can override as
      //  needed?
      s.append(lead.activeInfo()+" "+strengthDesc);
      s.append(" that "+subject+" "+tenseDesc);
      s.append(" involved in "+role.event);
    }
  }
  
  
  public Image icon() {
    if (subject instanceof Element) {
      return ((Element) subject).kind().sprite();
    }
    if (subject instanceof Event) {
      return ((Event) subject).icon();
    }
    return null;
  }
  
  
  public String toString() {
    return "Case File: "+subject;
  }
}
//*/




