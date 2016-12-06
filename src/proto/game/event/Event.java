

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;


//  TODO:  You need a certain chance to deposit tipoffs for each of the actors
//  involved- and definitely once the event has transpired!



public class Event implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public EventType type;
  
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
    if (step == null || place == null) {
      I.complain("Step/place were: "+step+"/"+place+", must be non-null!");
      return;
    }
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
  
  
  public boolean complete() {
    return complete;
  }
  
  
  public boolean involves(Element element) {
    if (step == null) return false;
    if (Visit.arrayIncludes(step.needs(), element)) return true;
    if (Visit.arrayIncludes(step.gives(), element)) return true;
    return false;
  }
  
  
  
  /**  Regular updates and life cycle:
    */
  public void onEventBegun() {
    if (step != null) {
      Base played = world().playerBase();
      Region region = place().region();
      float tipoffChance = region.currentValue(Region.TRUST) / 100f;
      
      I.say("Event begun: "+this);
      I.say("  Tipoff chance in "+region+": "+tipoffChance);
      
      for (Element e : step.needs()) {
        if (e == null || e.type != Kind.TYPE_PERSON) continue;
        final Person perp = (Person) e;
        
        if (Rand.num() < tipoffChance) {
          played.leads.addLead(perp);
          I.say("  Generating tipoff from: "+perp);
        }
        else I.say("No tipoff generated from "+perp);
      }
    }
  }
  
  
  public void updateEvent() {
    
  }
  
  
  public void onEventComplete() {
    if (step != null && step.type.isDangerous(this)) {
      Base played = world().playerBase();
      played.leads.addLead(this);
    }
  }
  
  
  
  /**  Helping with scene configuration:
    */
  public Series <Person> populateScene(Scene scene) {
    final List <Person> forces = new List();
    if (step == null) return forces;
    
    for (Element e : step.needs()) {
      if (e == null || e.type != Kind.TYPE_PERSON) continue;
      forces.add((Person) e);
    }
    
    //  TODO:  Move this out to the StepType class, so that any special items
    //  can also be equipped?
    
    //  TODO:  You also need to populate with civilian passerbys and/or
    //  hostages!  Also, the types of goon/civilian should be specified under
    //  types in the content package- or perhaps associated with a given base/
    //  faction.  Yeah.
    
    final float dangerLevel = 0.5f;
    final Base faction = step.plan.agent.base();
    final Kind GOONS[] = faction.goonTypes().toArray(Kind.class);
    float forceLimit = dangerLevel * 10;
    float forceSum   = 0;
    
    while (forceSum < forceLimit) {
      Kind ofGoon = (Kind) Rand.pickFrom(GOONS);
      Person goon = Person.randomOfKind(ofGoon, scene.world());
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




