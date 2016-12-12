

package proto.game.event;
import java.awt.Image;

import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;



//  TODO:  Events have to implement Assignment.  Yeah.


public class Event implements Session.Saveable, Assignment {
  
  
  /**  Data fields, construction and save/load methods-
    */
  public static boolean freeTipoffs = true;
  
  final public EventType type;
  
  PlanStep step;
  Place place;
  float timeBegins, timeEnds;
  boolean complete;
  
  List <Person> involved = new List();
  
  
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
    s.loadObjects(involved);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(type      );
    s.saveObject(step      );
    s.saveObject(place     );
    s.saveFloat (timeBegins);
    s.saveFloat (timeEnds  );
    s.saveBool  (complete  );
    s.saveObjects(involved);
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
  
  
  public Place targetLocation() {
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
  
  
  
  /**  Assigning perps-
    */
  public Series <Person> assigned() {
    return involved;
  }
  
  
  public boolean allowsAssignment(Person p) {
    if (step == null) return false;
    return Visit.arrayIncludes(step.needs(), p);
  }
  
  
  public void setAssigned(Person p, boolean is) {
    involved.toggleMember(p, is);
  }
  
  
  
  /**  Regular updates and life cycle:
    */
  public void beginEvent() {
    if (step != null) {
      Base played = world().playerBase();
      Region region = targetLocation().region();
      float tipoffChance = region.currentValue(Region.TRUST) / 100f;
      
      I.say("Event begun: "+this);
      I.say("  Tipoff chance in "+region+": "+tipoffChance);
      
      for (Element e : step.needs()) {
        if (e == null || e.type != Kind.TYPE_PERSON) continue;
        
        final Person perp = (Person) e;
        perp.setAssignment(this);
        setAssigned(perp, true);
        
        boolean tips = Rand.num() < tipoffChance || freeTipoffs;
        //if (! perp.isCriminal()) tips = false;
        
        if (tips) {
          //  TODO:  You still need a tipoff to display!
          final CaseFile file = played.leads.caseFor(perp);
          file.recordInvolvement(this, CaseFile.LEVEL_TIPOFF);
          //played.leads.confirmLead(new LeadTipoff(played, perp), perp);
          I.say("  Generating tipoff from: "+perp);
        }
        else I.say("No tipoff generated from "+perp);
      }
    }
  }
  
  
  public void updateEvent() {
    
  }
  
  
  public void completeEvent() {
    completeWithEffects(false, Rand.num(), 1.0f);
  }
  
  
  
  /**  Helping with scene configuration and after-effects:
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
  
  
  public void completeWithEffects(
    boolean playerWon, float collateral, float getaways
  ) {
    for (Person perp : involved) perp.setAssignment(null);
    involved.clear();
    complete = true;
    final boolean danger = step.type.isDangerous(this);
    
    if (step != null && danger) {
      Base played = world().playerBase();
      //  TODO:  You still need to generate a news-report for this!
      final CaseFile file = played.leads.caseFor(this);
      file.recordInvolvement(this, CaseFile.LEVEL_CONVICTED);
    }
    if (step != null) step.type.applyRealStepEffects(
      step, place, ! playerWon, collateral, getaways
    );
    if (danger) {
      final Region region = place.region();
      
      float deterEffect = playerWon ? 2 : 0;
      deterEffect -= getaways * 4;
      float trustEffect = playerWon ? 2 : 0;
      trustEffect -= collateral * 4;
      
      region.nudgeCurrentStat(Region.DETERRENCE, deterEffect);
      region.nudgeCurrentStat(Region.TRUST     , trustEffect);
    }
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
  
  
  
}




