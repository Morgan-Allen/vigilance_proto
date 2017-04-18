

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public abstract class Event implements Session.Saveable, Assignment {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public EventType type;
  
  final World world;
  int timeBegins = -1;
  boolean scheduled, complete;
  
  List <Person> involved = new List();
  List <EventEffects> allEffects = new List();
  
  
  protected Event(EventType type, World world) {
    this.type = type;
    this.world = world;
  }
  
  
  public Event(Session s) throws Exception {
    s.cacheInstance(this);
    type       = (EventType) s.loadObject();
    world      = (World    ) s.loadObject();
    timeBegins = s.loadInt ();
    scheduled  = s.loadBool();
    complete   = s.loadBool();
    s.loadObjects(involved  );
    s.loadObjects(allEffects);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(type      );
    s.saveObject(world     );
    s.saveInt   (timeBegins);
    s.saveBool  (scheduled );
    s.saveBool  (complete  );
    s.saveObjects(involved  );
    s.saveObjects(allEffects);
  }
  
  
  
  /**  Supplemental setup/progression methods-
    */
  public World world() {
    return world;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_PLAN_STEP;
  }
  
  
  public boolean isPlot() {
    return this instanceof Plot;
  }
  
  
  public boolean isTrial() {
    return this instanceof Trial;
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
  public void setBeginTime(int time, boolean scheduled) {
    this.timeBegins = time;
    this.scheduled = scheduled;
  }
  
  
  public int timeBegins() {
    return timeBegins;
  }
  
  
  public boolean scheduled() {
    return scheduled;
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
  }
  
  
  public void updateEvent() {
    return;
  }
  
  
  
  /**  Helping with scene configuration and after-effects:
    */
  public void completeEvent() {
    for (Person perp : involved) perp.removeAssignment(this);
    involved.clear();
    complete = true;
  }
  
  
  public void completeAfterScene(Scene scene, EventEffects effects) {
    completeEvent();
  }
  
  
  public EventEffects generateEffects(Step step) {
    return new EventEffects();
  }
  
  
  public EventEffects generateEffects(Scene scene) {
    return new EventEffects();
  }
  
  
  protected void recordEffects(EventEffects effects) {
    allEffects.add(effects);
  }
  
  
  public Series <EventEffects> allEffects() {
    return allEffects;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String name() {
    return type.name;
  }
  
  
  public String toString() {
    return name();
  }
  
  
  public String activeInfo() {
    return "On job: "+name();
  }
  
  
  public Image icon() {
    return type.icon;
  }
  
  
}





