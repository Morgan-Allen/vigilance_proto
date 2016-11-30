

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import java.awt.Image;



public class Event implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final EventType type;
  PlanStep step;
  Place place;
  float timeBegins, timeEnds;
  boolean complete;
  
  
  protected Event(EventType type) {
    this.type = type;
  }
  
  
  public Event(Session s) throws Exception {
    s.cacheInstance(this);
    type       = (EventType) s.loadObject();
    step       = (PlanStep ) s.loadObject();
    place      = (Place    ) s.loadObject();
    timeBegins = s.loadFloat();
    timeEnds   = s.loadFloat();
    complete   = s.loadBool ();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(type      );
    s.saveObject(step      );
    s.saveObject(place     );
    s.saveFloat (timeBegins);
    s.saveFloat (timeEnds  );
    s.saveBool  (complete  );
  }
  
  
  
  /**  Supplemental setup/progression methods-
    */
  public void assignParameters(
    PlanStep step, Place place, float begins, float ends
  ) {
    this.step       = step  ;
    this.place      = place ;
    this.timeBegins = begins;
    this.timeEnds   = ends  ;
  }
  
  
  public World world() {
    return place.world();
  }
  
  
  public Place place() {
    return place;
  }
  
  
  public float timeBegins() {
    return timeBegins;
  }
  
  
  public float timeEnds() {
    return timeEnds;
  }
  
  
  public void updateEvent() {
  }
  
  
  public boolean complete() {
    return complete;
  }
  
  
  public boolean involves(Element element) {
    if (step == null) return false;
    if (Visit.arrayIncludes(step.needs(), element)) return true;
    if (Visit.arrayIncludes(step.gives(), element)) return true;
    return false;
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
}









