

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
  
  
  public Series <PlanStep> steps() {
    return steps;
  }
  
  
  public void addObtained(Element thing) {
    preObtained.add(thing);
  }
  
  
  public void addGoal(PlanStep goal, float priority) {
    goal.assignRating(priority);
    addStep(goal);
    fillNeeds(goal);
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
        if (verbose) I.say("  considering: "+goal.langDescription());
      }
    }
    
    PlanStep picked = pickStepFrom(goals);
    if (picked != null) addGoal(picked, picked.rating());
    else if (verbose) I.say("  No goal selected!");
  }
  
  
  public void advancePlan(int maxIterations) {
    while (maxIterations-- > 0) {
      if (verbose) I.say("\nAdvancing plan-");
      
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
    float sumRatings = 0;
    for (PlanStep child : nextGen) {
      if (verbose) I.say("  Rating for "+child+": "+child.rating());
      sumRatings += child.rating();
    }
    
    float rollInSum = Rand.num() * sumRatings;
    PlanStep picked = null;
    for (PlanStep child : nextGen) {
      rollInSum -= child.rating();
      if (rollInSum <= 0) { picked = child; break; }
    }
    
    return picked;
  }
  
  
  
  /**  TODO:  Move some of these out to the PlanStep class?
    */
  private void addStep(PlanStep step) {
    step.uniqueID = steps.size();
    steps.addFirst(step);
    if (step.parent != null) {
      step.parent.setStepForNeed(step.parentNeedID, step);
    }
    if (verbose) I.say("Have added step: "+step.langDescription());
  }
  
  
  private void fillNeeds(PlanStep step) {
    for (Object role : step.needTypes()) {
      if (! step.type.isNeeded(role, step)) continue;
      Element used = step.need(role);
      Series <Element> available = step.type.availableTargets(role, world);
      if (available == null || used != null) continue;
      
      float bestRating = 0;
      for (Element match : available) {
        if (step.doesGive(match)     ) continue;
        if (neededDuring(step, match)) continue;
        float rating = step.calcSuitability(match, role) * (Rand.num() + 0.5f);
        if (rating > bestRating) { used = match; bestRating = rating; }
      }
      
      if (used == null) {
        ///I.say("No match found for: "+role+"/"+step);
      }
      else step.setNeed(role, used);
    }
  }
  
  
  private void addStepsFrom(PlanStep step, Batch <PlanStep> toEval) {
    for (Object role : step.needTypes()) {
      Element  needed   = step.need(role);
      PlanStep needStep = step.stepForNeed(role);
      if (needed == null || needStep != null) continue;
      if (obtainedBefore(step, needed)      ) continue;
      
      PlanStep possible[] = step.actionsToObtain(needed, role);
      for (PlanStep child : possible) {
        fillNeeds(child);
        child.setParent(step, role);
        child.calcStepRatingFromParent(step);
        toEval.add(child);
      }
    }
  }
  
  
  
  /**  Determining pre-conditions and keeping track of resource-reservations.
    */
  private boolean obtainedBefore(PlanStep step, Element need) {
    //  TODO:  Make this a generic check for 'publicly known' things.
    if (need.type == Element.TYPE_PLACE) return true;
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
    I.say("\n\nFinal plan: ");
    for (PlanStep step : steps()) {
      I.say("  "+step.langDescription()+": "+step.rating());
    }
  }
}








