

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;

//  TODO:  Remove this direct reference!
import proto.content.agents.Crooks;



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
  
  
  public PlanStep planStep() {
    return step;
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
  
  
  
  /**  Helping with scene configuration:
    */
  public Series <Person> populateScene(Scene scene) {
    final List <Person> forces = new List();
    if (step == null) return forces;
    
    for (Element e : step.needs()) {
      if (e == null || e.type != Element.TYPE_PERSON) continue;
      forces.add((Person) e);
    }
    
    //  TODO:  Move this out to the StepType class, so that any special items
    //  can also be equipped...
    
    //  TODO:  You also need to populate with civilian passerbys and/or
    //  hostages!  Also, the types of goon/civilian should be specified under
    //  types in the content package.
    
    final float dangerLevel = 0.5f;
    final Kind GOONS[] = {
      Crooks.MOBSTER,
      Crooks.GOON   ,
      Crooks.GOON   ,
    };
    final float GOON_CHANCES[] = { 1, 1, 1 };
    
    float forceLimit = dangerLevel * 10;
    float forceSum   = 0;
    while (forceSum < forceLimit) {
      Kind ofGoon = (Kind) Rand.pickFrom(GOONS, GOON_CHANCES);
      Person goon = new Person(ofGoon, scene.world());
      forceSum += goon.stats.powerLevel();
      forces.add(goon);
    }
    
    for (Person p : forces) {
      int nX = scene.size() / 2, nY = scene.size() / 2;
      nX += 5 - Rand.index(10);
      nY += 5 - Rand.index(10);
      Tile entry = scene.findEntryPoint(nX, nY, p);
      if (entry == null) { forces.remove(p); continue; }
      scene.addToTeam(p);
      scene.enterScene(p, entry.x, entry.y);
    }
    return forces;
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




