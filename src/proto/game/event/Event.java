

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
  
  
  public boolean isPlot() {
    return this instanceof Plot;
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
  
  
  public void completeAfterScene(Scene scene, EventReport report) {
    for (Person perp : involved) perp.removeAssignment(this);
    involved.clear();
    complete = true;
    world.events.closeEvent(this);
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





