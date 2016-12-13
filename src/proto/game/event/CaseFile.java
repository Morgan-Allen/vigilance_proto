

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;



public class CaseFile implements Session.Saveable {
  
  
  final static int
    ROLE_NONE = -1,
    ROLE_GOON = -2,
    ROLE_INFO = -3;
  final static float
    LEVEL_TIPOFF    = 0.5f,
    LEVEL_EVIDENCE  = 1.0f,
    LEVEL_CONVICTED = 1.5f;
  
  final Base base;
  final public Object subject;
  
  Place knownLocation = null;
  float evidenceAsHideout = 0;
  
  private static class Role {
    Event event;
    int roleID = -1;
    int sentence = -1;
    //  TODO:  Record all the Leads pointing at involvement.
    float evidence = 0;
  }
  List <Role> roles = new List();
  
  //  NOTE:  This is intended for temporary storage and will get wiped whenever
  //  changes are made.
  private boolean refreshOptions = false;
  final List <Lead> followOptions = new List();
  
  
  CaseFile(Base base, Object subject) {
    this.base    = base   ;
    this.subject = subject;
    knownLocation = currentSubjectLocation();
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base   ) s.loadObject();
    subject = (Element) s.loadObject();
    
    knownLocation = (Place) s.loadObject();
    evidenceAsHideout = s.loadFloat();
    
    for (int n = s.loadInt(); n-- > 0;) {
      final Role r = new Role();
      r.event    = (Event) s.loadObject();
      r.roleID   = s.loadInt();
      r.sentence = s.loadInt();
      r.evidence = s.loadFloat();
      roles.add(r);
    }
    
    refreshOptions = s.loadBool();
    s.loadObjects(followOptions);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base   );
    s.saveObject(subject);
    
    s.saveObject(knownLocation);
    s.saveFloat(evidenceAsHideout);
    
    s.saveInt(roles.size());
    for (Role r : roles) {
      s.saveObject(r.event   );
      s.saveInt   (r.roleID  );
      s.saveInt   (r.sentence);
      s.saveFloat (r.evidence);
    }
    s.saveBool(refreshOptions);
    s.saveObjects(followOptions);
  }
  
  
  
  /**  Utility methods for determine appropriate actions-
    */
  void recordInvolvement(Event event, float evidenceLevel) {
    
    Role role = null;
    for (Role r : roles) if (r.event == event) { role = r; break; }
    if (role == null) {role = new Role(); roles.add(role); }
    
    role.event  = event;
    role.roleID = Visit.indexOf(subject, event.planStep().needs());
    
    final Series perps = event.assigned();
    if (role.roleID == -1 && perps.includes(subject)) {
      role.roleID = ROLE_GOON;
    }
    
    this.refreshOptions = true;
    
    base.world().pauseMonitoring();
  }
  
  
  void recordHideoutEvidence(float evidenceLevel) {
    this.evidenceAsHideout += evidenceLevel;
  }
  
  
  void updateLocation(Place location) {
    this.knownLocation = location;
    this.refreshOptions = true;
  }
  
  
  Place knownLocation() {
    return knownLocation;
  }
  
  
  Place currentSubjectLocation() {
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
    if (step.plan.agent.base().plans.planComplete(step.plan)) return true;
    if (role.sentence > 0) return true;
    return false;
  }
  
  
  boolean isActiveSuspect() {
    for (Role role : roles) {
      if (defunct(role)) continue;
      return true;
    }
    return false;
  }
  
  
  private Role latestActiveRole() {
    float lastDate = Float.NEGATIVE_INFINITY;
    Role active = null;
    for (Role role : roles) {
      if (defunct(role)) continue;
      float date = role.event.timeEnds();
      if (date > lastDate) { active = role; lastDate = date; }
    }
    return active;
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
    if (! refreshOptions) return followOptions;
    
    followOptions.clear();
    refreshOptions = false;
    
    //  TODO:  I'm going to return to these later, and gradually add more
    //  options.
    /*
    if (leadsTo instanceof Item || leadsTo instanceof Clue) {
    }
    if (leadsTo instanceof Place) {
    }
    //*/
    
    if (subject instanceof Person) {
      final Person person = (Person) subject;
      final Event threatens = base.leads.threateningEvent(person);
      
      if (isActiveSuspect()) {
        Lead tailing = new LeadTail(base, person);
        followOptions.add(tailing);
      }
      if (threatens != null) {
        Lead guarding = new LeadGuard(base, person.place(), threatens);
        followOptions.add(guarding);
      }
    }
    
    if (subject instanceof Place) {
      final Place place = (Place) subject;
      final Event threatens = base.leads.threateningEvent(place);
      
      if (threatens != null) {
        Lead guarding = new LeadGuard(base, place, threatens);
        followOptions.add(guarding);
      }
    }
    
    if (subject instanceof Event) {
      
    }
    
    return followOptions;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void shortDescription(StringBuffer s) {
    
    //  TODO:  Find the most serious/recent suspected role, and mention that
    //  along with the degree of evidence.
    
    //  (The player can click elsewhere for a complete breakdown on their
    //  prior record and any known associates.)
    
    final Role latest = latestActiveRole();
    if (latest != null) {
      if (latest.evidence <= LEVEL_TIPOFF) {
        s.append(subject+" is suspected to have ");
      }
      else {
        s.append(subject+" is playing the role of ");
      }
    }
    
    //  TODO:  See if the subject is currently threatened- if so, mention that
    //  you have the option to guard them.
    //  TODO:  See if the subject is believed to be a hideout/kingpin- if so,
    //  mention that you have the option to raid them.
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






