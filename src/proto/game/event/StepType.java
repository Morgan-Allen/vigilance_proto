

package proto.game.event;
import proto.game.world.*;
import proto.common.Kind;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;



public abstract class StepType extends EventType {
  
  
  /**  Data fields, construction and state restoration-
    */
  final Object needTypes[], giveTypes[];
  
  
  protected StepType(
    String name, String ID, String iconPath,
    Object needTypes[], Object giveTypes[]
  ) {
    super(name, ID, iconPath);
    this.needTypes = needTypes;
    this.giveTypes = giveTypes;
  }
  
  
  protected static Object[] rolesFor(Object... roleTypes) {
    return roleTypes;
  }
  
  
  
  /**  Evaluating suitability of nominees for various roles, providing viable
    *  targets, generating the initial step, and evaluating the odds and appeal
    *  of such an action.
    */
  public PlanStep asGoal(Element target, Plan plan) {
    return null;
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    return null;
  }
  
  
  protected Series <Element> availableTargets(Object needType, World world) {
    return world.inside();
  }
  
  
  protected boolean activeDuring(PlanStep step, PlanStep other) {
    return step == other;
  }
  
  
  protected float urgency(Object needType, PlanStep step) {
    return 1;
  }
  
  
  protected float calcFitness(Element used, Object needType, PlanStep step) {
    return 0;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
  protected float baseFailCost(PlanStep step) {
    return 0;
  }
  
  
  protected float baseSuccessChance(PlanStep step) {
    float chance = 0, numF = 0;
    
    for (Object role : needTypes) {
      float urgency = urgency(role, step);
      if (urgency <= 0) continue;
      
      final Element used = step.need(role);
      float fitness = used == null ? 0 : calcFitness(used, role, step);
      if (urgency >= 1 && fitness <= 0) return 0;
      
      urgency = Nums.clamp(urgency, 0, 1);
      fitness = Nums.clamp(fitness, 0, 1);
      chance += (1 - ((1 - fitness) * urgency));
      numF++;
    }
    
    return (numF == 0) ? 1 : (chance / numF);
  }
  
  
  public float harmLevel(PlanStep step, Element involved) {
    return 0;
  }
  
  
  
  /**  AI-control methods for within a particular scene:
    */
  public Action specialAction(Person onTeam, PlanStep step, Scene scene) {
    return null;
  }
  
  
  public float rateSpecialAction(Action action) {
    return 5;
  }
  
  
  public boolean onSpecialActionEnd(Action action) {
    return false;
  }
  
  
  
  /**  And finally, handling the after-effects of a step:
    */
  protected int duration(PlanStep step) {
    return Task.TIME_MEDIUM;
  }
  
  
  public Series <Person> generateGroundForces(Event event) {
    final PlanStep step = event.planStep();
    final List <Person> forces = new List();
    
    for (Element e : step.needs()) {
      if (e == null || e.type != Kind.TYPE_PERSON) continue;
      forces.add((Person) e);
    }
    
    final float dangerLevel = 0.5f;
    final Base faction = step.plan.agent.base();
    final PersonType GOONS[] = faction.goonTypes().toArray(PersonType.class);
    float forceLimit = dangerLevel * 10;
    float forceSum   = 0;
    
    while (forceSum < forceLimit) {
      PersonType ofGoon = (PersonType) Rand.pickFrom(GOONS);
      Person goon = Person.randomOfKind(ofGoon, event.world());
      forceSum += goon.stats.powerLevel();
      forces.add(goon);
    }
    
    return forces;
  }
  
  
  public void updateReport(Event event, EventReport report) {
    if (isDangerous(event) && report.outcomeState == Scene.STATE_INIT) {
      report.composeFromEvent(event, Rand.num(), 1);
    }
  }
  
  
  protected void applyEffectsAfter(Event event) {
    final PlanStep step = event.planStep();
    final Base base = step.plan.agent.base();
    for (Element e : step.gives()) {
      base.world().setInside(e, true);
      base.setAttached(e, true);
    }
    return;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected abstract String langDescription(PlanStep step);
  

  protected String nameFor(Event event) {
    PlanStep step = event.step;
    return langDescription(step);
  }
  
  
  protected String infoFor(Event event) {
    PlanStep step = event.step;
    return step.longDescription();
  }
}










