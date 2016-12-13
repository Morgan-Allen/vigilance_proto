

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public class Plan implements Session.Saveable {
  
  final public Person agent;
  final public World world;
  
  StepType stepTypes[];
  List <Element> preObtained = new List();
  List <PlanStep> steps = new List();
  
  public boolean verbose = false;
  
  
  public Plan(Person agent, World world, StepType... stepTypes) {
    this.agent = agent;
    this.world = world;
    this.stepTypes = stepTypes;
  }
  
  
  public Plan(Session s) throws Exception {
    s.cacheInstance(this);
    
    agent = (Person) s.loadObject();
    world = (World ) s.loadObject();
    
    stepTypes = (StepType[]) s.loadObjectArray(StepType.class);
    s.loadObjects(preObtained);
    s.loadObjects(steps);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(agent);
    s.saveObject(world);
    
    s.saveObjectArray(stepTypes);
    s.saveObjects(preObtained);
    s.saveObjects(steps);
  }
  
  
  
  /**  
    */
  public Series <PlanStep> steps() {
    return steps;
  }
  
  
  public PlanStep stepAfter(PlanStep step) {
    int index = steps.indexOf(step);
    if (index == -1 || index + 1 >= steps.size()) return null;
    return steps.atIndex(index + 1);
  }
  
  
  
  
  /**  Initial conditions and goal configuration-
    */
  public void addObtained(Element thing) {
    preObtained.add(thing);
  }
  
  
  public void addGoal(PlanStep goal, float priority) {
    goal.assignRating(priority);
    fillNeeds(goal);
    addStep(goal);
  }
  
  
  public void selectInitialGoal() {
    if (verbose) I.say("\nSelecting goal-");
    
    final Batch <PlanStep> goals = new Batch();
    final Series <Element> targets = world.inside();
    
    for (StepType type : stepTypes) {
      for (Element target : targets) {
        PlanStep goal = type.asGoal(target, this);
        if (goal == null) continue;
        goal.assignRating(type.baseAppeal(goal));
        goals.add(goal);
      }
    }
    
    PlanStep picked = pickStepFrom(goals);
    if (picked != null) addGoal(picked, picked.rating());
    else if (verbose) I.say("  No goal selected!");
  }
  
  
  
  /**  Plan advancement and step selection/addition-
    */
  public void advancePlan(int maxIterations) {
    while (maxIterations-- > 0) {
      if (verbose) I.say("\nAdvancing plan- "+this.hashCode());
      
      Batch <PlanStep> nextGen = new Batch();
      for (PlanStep step : steps) {
        addStepsFrom(step, nextGen);
      }
      
      PlanStep picked = pickStepFrom(nextGen);
      if (picked != null) addStep(picked);
      else { if (verbose) I.say("  No new step chosen!"); break; }
    }
  }
  
  
  private PlanStep pickStepFrom(Series <PlanStep> nextGen) {
    PlanStep picked = null;
    float bestRating = 0;
    
    for (PlanStep child : nextGen) {
      if (verbose) I.say("  Rating for "+child+": "+child.rating());
      float rating = child.rating() * (Rand.num() + 0.5f);
      if (rating > bestRating) { picked = child; bestRating = rating; }
    }
    
    return picked;
  }
  
  
  private void addStep(PlanStep step) {
    step.uniqueID = steps.size();
    steps.addFirst(step);
    if (step.parent != null) {
      step.parent.setStepForNeed(step.parentNeedID, step);
    }
    if (verbose) I.say("Have added step: "+step.langDescription());
  }
  
  
  private void fillNeeds(PlanStep step) {
    final Base base = agent.base();
    final Object roles[] = step.needTypes();
    final boolean markDone[] = new boolean[roles.length];
    while (true) {
      //
      //  Our first task is to find the single most urgent need to fulfil, with
      //  a little bit of random weighting for spice-
      int roleIndex = -1;
      float maxUrgency = 0;
      for (int i = roles.length; i-- > 0;) if (! markDone[i]) {
        float u = step.type.urgency(roles[i], step);
        if (u <= 0) continue;
        if (u <  1) u = (u + Rand.num()) / 2;
        else u += Rand.num() / 2;
        if (u > maxUrgency) { roleIndex = i; maxUrgency = u; }
      }
      if (roleIndex == -1) break;
      //
      //  (We mark any previous attempts to avoid infinite loops in the event
      //  that satisfaction is impossible.)
      markDone[roleIndex] = true;
      Object role = roles[roleIndex];
      //
      //  Having identified the need to fulfill, we identify possible matches
      //  that could satisfy it, and pick whichever has the highest fitness
      //  rating (again, with some random spice.)
      Element used = step.need(role);
      if (used != null) continue;
      Series <Element> available = step.type.availableTargets(role, world);
      if (available == null || available.empty()) continue;
      
      float bestRating = 0;
      for (Element match : available) if (match != null) {
        if (match.accessLevel(base) == Element.Access.SECRET) continue;
        if (step.doesGive(match)     ) continue;
        if (neededDuring(step, match)) continue;
        float rating = step.calcFitness(match, role) * (Rand.num() + 0.5f);
        if (rating > bestRating) { used = match; bestRating = rating; }
      }
      if (used != null) step.setNeed(role, used, null);
    }
  }
  
  
  private void addStepsFrom(PlanStep step, Batch <PlanStep> toEval) {
    for (Object role : step.needTypes()) {
      Element  needed   = step.need(role);
      PlanStep needStep = step.stepForNeed(role);
      if (needed == null || needStep != null) continue;
      
      if (obtainedBefore(step, needed)) {
        step.setNeed(role, needed, step);
        continue;
      }
      
      PlanStep possible[] = step.actionsToObtain(needed, role);
      for (PlanStep child : possible) {
        fillNeeds(child);
        child.setParent(step, role);
        child.calcRatingFromBase(step.rating());
        toEval.add(child);
      }
    }
  }
  
  
  
  /**  Determining pre-conditions and keeping track of resource-reservations.
    */
  private boolean obtainedBefore(PlanStep step, Element need) {
    final Base base = agent.base();
    if (need.accessLevel(base) == Element.Access.GRANTED) return true;
    if (preObtained.includes(need)) return true;
    
    for (PlanStep prior : steps) {
      if (prior == step) break;
      if (prior.doesGive(need)) return true;
    }
    return false;
  }
  
  
  private boolean neededDuring(PlanStep step, Element used) {
    for (PlanStep other : steps) if (other.activeDuring(step)) {
      for (Element o : other.needs()) if (o == used) return true;
    }
    return false;
  }
  
  
  
  /**  Evaluating success-chance:
    */
  public float successChance() {
    float chance = 1.0f;
    for (PlanStep step : steps) {
      chance *= step.calcSuccessChance();
    }
    return chance;
  }
  
  
  public float calcPlanRating() {
    float rating = 0, numSteps = 0;
    for (PlanStep step : steps) {
      rating += step.rating();
      numSteps++;
    }
    if (numSteps <= 0) return -1;
    return rating / numSteps;
  }
  
  
  
  /**  Debugging, graphics and interface methods-
    */
  public void printFullPlan() {
    I.say("\n\nFinal plan: "+this.hashCode());
    for (PlanStep step : steps()) {
      I.say("  "+step.langDescription()+": "+step.rating());
    }
  }
}








