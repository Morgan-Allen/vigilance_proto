

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;



public class CaseFile implements Session.Saveable {
  
  
  final static int
    ROLE_NONE    = -1,
    ROLE_CLUE    = -2,
    ROLE_GOON    = -3,
    ROLE_HIDEOUT = -4,
    ROLE_CRIME   = -5,
    ROLE_SCENE   = -6;
  final static float
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
    List <Lead> evidence = new List();
  }
  List <Role> roles = new List();
  
  //  NOTE:  This is intended for temporary storage and will get wiped whenever
  //  changes are made.
  private boolean refreshOptions = false;
  final List <Lead> followOptions = new List();
  
  
  CaseFile(Base base, Object subject) {
    this.base    = base   ;
    this.subject = subject;
    knownLocation = trueLocation();
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base   ) s.loadObject();
    subject = (Element) s.loadObject();
    
    knownLocation = (Place) s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      final Role r = new Role();
      r.event       = (Event) s.loadObject();
      r.roleID      = s.loadInt();
      r.sentence    = s.loadInt();
      r.maxEvidence = s.loadFloat();
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
      s.saveObjects(r.evidence);
    }
    s.saveBool(refreshOptions);
    s.saveObjects(followOptions);
  }
  
  
  
  /**  Recording involvement in criminal actions and other information
    *  updates-
    */
  boolean recordCurrentRole(Event event, Lead lead) {
    int roleID = Visit.indexOf(subject, event.planStep().needs());
    if (roleID == -1) { I.complain("Subject not involved!"); return false; }
    return recordRole(event, roleID, lead);
  }
  
  
  boolean recordRole(Event event, int roleID, Lead lead) {
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
    else if (role.evidence.includes(lead)) return false;
    //
    //  Record the new evidence, refresh options and return-
    I.say("RECORDING new role for "+subject+", event: "+event+": "+roleID);
    role.maxEvidence = Nums.max(role.maxEvidence, lead.evidenceLevel());
    role.evidence.include(lead);
    this.refreshOptions = true;
    this.base.world().pauseMonitoring();
    return true;
  }
  
  
  void updateLocation(Place location) {
    this.knownLocation = location;
    this.refreshOptions = true;
  }
  
  
  
  /**  Information queries relevant to investigation options-
    */
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
    if (step == null) return true;
    if (role.sentence > 0) return true;
    if (step.plan.agent.base().plans.planComplete(step.plan)) return true;
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
    int worldTime = base.world().totalMinutes();
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
  
  
  
  /**  Generating subsequent investigation options-
    */
  public Series <Lead> investigationOptions() {
    
    for (Lead l : followOptions) if (l.complete()) {
      followOptions.remove(l);
      refreshOptions = true;
    }
    
    if (! refreshOptions) return followOptions;
    followOptions.clear();
    refreshOptions = false;
    
    //  TODO:  I'm going to return to these later, and gradually add more
    //  options.
    /*
    if (leadsTo instanceof Item || leadsTo instanceof Clue) {
    }
    //*/
    
    if (subject instanceof Person) {
      final Person person = (Person) subject;
      final Event next      = nextEventInvolved();
      final Event suspected = lastSuspectEvent();
      final Event threatens = base.leads.threateningEvent(person);
      
      if (next != null) {
        Lead tailing = new LeadSurveil(base, person);
        followOptions.add(tailing);
      }
      if (suspected != null) {
        //  TODO:  Include questioning and arrest options.
      }
      if (threatens != null) {
        Lead guarding = new LeadGuard(base, person.place(), threatens);
        followOptions.add(guarding);
      }
    }
    
    if (subject instanceof Place) {
      final Place place = (Place) subject;
      Event crime = eventWithRole(ROLE_SCENE);
      
      if (crime != null && crime.dangerous()) {
        Lead guarding = new LeadGuard(base, place, crime);
        followOptions.add(guarding);
      }
      if (crime != null) {
        Lead surveil = new LeadSurveil(base, place);
        followOptions.add(surveil);
      }
    }
    
    return followOptions;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  final static Table <Integer, String[]> ROLE_DESC = Table.make(
    ROLE_GOON   , new String[] {
      "",
      "",
      "<subject> was a low-level accomplice during the <event>."
    },
    ROLE_HIDEOUT, new String[] {
      "You have a tipoff that <subject> is a criminal hideout.",
      "Evidence confirms that <subject> is a criminal hideout.",
      ""
    },
    ROLE_CRIME  , new String[] {
      "You have a tipoff that the <event> is being planned.",
      "You have direct evidence for the <event>.",
      ""
    },
    ROLE_SCENE  , new String[] {
      "You have a tipoff that <subject> will be the venue for the <event>.",
      "Evidence confirms that <subject> will be the venue for the <event>.",
      ""
    }
  );
  
  
  private Role pickMostProminentRole() {
    int time = base.world().totalMinutes();
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
    
    Lead lead = role.evidence.last();
    String tenseDesc = "is", strengthDesc = "suggests";
    
    String fixedDesc[] = ROLE_DESC.get((Integer) role.roleID);
    /*
      String subDesc = desc[0];
      if (latest.evidence >= LEVEL_EVIDENCE ) subDesc = desc[1];
      if (latest.evidence >= LEVEL_CONVICTED) subDesc = desc[2];
      subDesc = subDesc.replace("<subject>", subject     .toString());
      subDesc = subDesc.replace("<event>"  , latest.event.toString());
      s.append(subDesc);
    //*/
    
    //  TODO:  Move this out to the Leads themselves, so they can override as
    //  needed.
    
    boolean pastTense = true, futureTense = true;
    if (role.event.complete()) futureTense = false;
    if (role.event.hasBegun()) pastTense   = false;
    if (futureTense) tenseDesc = "will be";
    if (pastTense  ) tenseDesc = "was";
    
    if (role.maxEvidence >= LEVEL_EVIDENCE) strengthDesc = "confirms";
    
    String desc = "";
    desc += lead.activeInfo()+" "+strengthDesc;
    desc += " that "+subject+" "+tenseDesc;
    desc += " involved in "+role.event;
    s.append(desc);
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
}







