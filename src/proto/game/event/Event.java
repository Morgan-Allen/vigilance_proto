

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Image;



public abstract class Event implements Session.Saveable, Assignment {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public EventType type;
  
  final World world;
  int timeBegins = -1;
  boolean complete;
  
  List <Person> involved = new List();
  
  
  protected Event(EventType type, World world) {
    this.type = type;
    this.world = world;
  }
  
  
  public Event(Session s) throws Exception {
    s.cacheInstance(this);
    type       = (EventType) s.loadObject();
    world      = (World    ) s.loadObject();
    timeBegins = s.loadInt ();
    complete   = s.loadBool();
    s.loadObjects(involved);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(type      );
    s.saveObject(world     );
    s.saveInt   (timeBegins);
    s.saveBool  (complete  );
    s.saveObjects(involved);
  }
  
  
  
  /**  Supplemental setup/progression methods-
    */
  public World world() {
    return world;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_PLAN_STEP;
  }
  
  
  
  /**  Assigning perps-
    */
  public Series <Person> assigned() {
    return involved;
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
  }
  
  
  public void setAssigned(Person p, boolean is) {
    involved.toggleMember(p, is);
  }
  
  
  
  /**  Regular updates and life cycle:
    */
  public void setBeginTime(int time) {
    this.timeBegins = time;
  }
  
  
  public int timeBegins() {
    return timeBegins;
  }
  
  
  public boolean scheduled() {
    return timeBegins >= 0;
  }
  
  
  public boolean hasBegun() {
    if (timeBegins == -1) return false;
    return world.timing.totalHours() >= timeBegins;
  }
  
  
  public boolean complete() {
    return complete;
  }
  
  
  public void beginEvent() {
    return;
    /*
    if (step != null) {
      Base played = world().playerBase();
      Place place = targetLocation();
      Region region = place.region();
      I.say("Event begun: "+this);
      
      if (GameSettings.SIMPLE_EVENTS) {
        final Lead tipoff = new LeadTipoff(played, place);
        final CaseFile file = played.leads.caseFor(place);
        Object role = file.recordRole(this, CaseFile.ROLE_SCENE, tipoff);
        presentTipoffMessage(TIPOFF_HEADER, "", file, role, null);
      }
      
      for (Element e : step.needs()) {
        if (e == null || e.type != Kind.TYPE_PERSON) continue;
        
        final Person perp = (Person) e;
        place.setAttached(perp, true);
        perp.addAssignment(this);
        setAssigned(perp, true);
        
        float tipoffChance = region.currentValue(Region.TRUST) / 100f;
        if (perp.isCivilian()) tipoffChance *= 2;
        I.say("  Tipoff chance for "+e+" in "+region+": "+tipoffChance);
        boolean tips = Rand.num() < tipoffChance || GameSettings.freeTipoffs;
        if (GameSettings.SIMPLE_EVENTS) tips = false;
        
        if (tips) {
          final Lead tipoff = new LeadTipoff(played, perp);
          final CaseFile file = played.leads.caseFor(perp);
          Object role = file.recordCurrentRole(this, tipoff);
          presentTipoffMessage(TIPOFF_HEADER, "", file, role, null);
        }
      }
    }
    //*/
  }
  
  
  public void updateEvent() {
    return;
  }
  
  
  
  /**  Helping with scene configuration and after-effects:
    */
  public Series <Person> populateScene(Scene scene) {
    //if (step == null) return new Batch();
    //final Series <Person> forces = step.type.generateGroundForces(this);
    //scene.entry.provideInProgressEntry(forces);
    //return forces;
    return new Batch();
  }
  
  
  public void completeEvent() {
    for (Person perp : involved) perp.removeAssignment(this);
    involved.clear();
    complete = true;
    
    /*
    if (step == null) return;
    EventReport report = new EventReport();
    step.type.updateReport(this, report);
    step.type.applyEffectsAfter(this);
    report.applyOutcomeEffects(targetLocation());
    //*/
    
    /*
    if (dangerous()) {
      Base played = world().playerBase();
      final Lead crimeReport = new LeadCrimeReport(played, this);
      final CaseFile file = played.leads.caseFor(this);
      Object role = file.recordRole(this, CaseFile.ROLE_CRIME, crimeReport);
      presentTipoffMessage(REPORT_HEADER, "", file, role, report);
    }
    //*/
  }
  
  
  public void completeAfterScene(Scene scene, EventReport report) {
    for (Person perp : involved) perp.removeAssignment(this);
    involved.clear();
    complete = true;
    world.events.closeEvent(this);
    
    //if (step == null) return;
    //step.type.updateReport(this, report);
    //if (! report.playerWon()) step.type.applyEffectsAfter(this);
  }
  
  
  public boolean dangerous() {
    return type.isDangerous(this);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String name() {
    return type.nameFor(this);
  }
  
  
  public String info() {
    return type.infoFor(this);
  }
  
  
  public String toString() {
    return name();
  }
  
  
  public String activeInfo() {
    return "On job: "+name();
  }
  
  
  public String helpInfo() {
    return type.infoFor(this);
  }
  
  
  public Image icon() {
    return type.iconFor(this);
  }
  
  
  /*
  final static String
    TIPOFF_HEADER = "New Tipoff",
    REPORT_HEADER = "News Report";
  
  protected void presentTipoffMessage(
    String header, String mainText,
    CaseFile file, Object role, EventReport report
  ) {
    if (file == null || role == null) return;
    StringBuffer s = new StringBuffer(mainText);
    if (mainText.length() > 0) s.append("\n\n");
    file.shortDescription(role, s);
    
    if (report != null) {
      Region region = targetLocation().region();
      float trust = report.trustEffect, deter = report.deterEffect;
      s.append("\n"+region+" Trust "     +I.signNum((int) trust)+"%");
      s.append("\n"+region+" Deterrence "+I.signNum((int) deter)+"%");
    }
    
    final MainView view = world.view();
    view.queueMessage(new MessageView(
      view, icon(), header, s.toString(), "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        view.dismissMessage(this);
      }
    });
  }
  //*/
  
}





