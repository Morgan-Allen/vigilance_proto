

package proto.game.plans;
import proto.util.*;
import static proto.game.plans.StepType.*;



public class Plan {
  
  Thing agent;
  Thing world;
  
  List <Thing> preObtained = new List();
  List <PlanStep> steps = new List();
  
  
  Plan(Thing agent, Thing world) {
    this.agent = agent;
    this.world = world;
  }
  
  
  void addObtained(Thing thing) {
    preObtained.add(thing);
  }
  
  
  void addGoal(PlanStep goal, float priority) {
    goal.rating = priority;
    addStep(goal);
    fillNeeds(goal);
    I.say("Have added goal: "+goal.longDescription());
  }
  
  
  void advancePlan() {
    I.say("\nAdvancing plan...");
    
    Batch <PlanStep> nextGen = new Batch();
    for (PlanStep step : steps) addStepsFrom(step, nextGen);
    
    PlanStep picked = null;
    float bestRating = 0;
    for (PlanStep child : nextGen) {
      if (child.rating <= bestRating) continue;
      picked     = child;
      bestRating = child.rating;
    }
    
    if (picked != null) {
      addStep(picked);
      I.say("  Adding step: "+picked.longDescription());
    }
  }
  
  
  
  /**  TODO:  Move these both out to the ActionType class?
    */
  private void addStep(PlanStep step) {
    step.uniqueID = steps.size();
    steps.addFirst(step);
    if (step.parent != null) step.parent.needSteps[step.parentNeedID] = step;
  }
  
  
  private void fillNeeds(PlanStep step) {
    for (Role role : step.needsRoles()) {
      Thing used = step.needs[role.ID];
      if (used != null) continue;
      
      float bestRating = 0;
      for (Thing match : world.inside) {
        if (neededDuring(step, match)) continue;
        float rating = step.calcSuitability(match, role);
        if (rating > bestRating) { used = match; bestRating = rating; }
      }
      step.needs[role.ID] = used;
    }
  }
  
  
  private void addStepsFrom(PlanStep step, Batch <PlanStep> toEval) {
    for (Role role : step.needsRoles()) {
      Thing needed = step.needs[role.ID];
      if (needed == null                 ) continue;
      if (step.needSteps[role.ID] != null) continue;
      if (obtainedBefore(step, needed)   ) continue;
      
      PlanStep possible[] = step.actionsToObtain(needed, role);
      for (PlanStep child : possible) {
        fillNeeds(child);
        child.bindParent(step, role);
        
        float chance = child.calcSuccessChance();
        child.rating = chance * step.rating;
        child.rating += chance * child.baseAppeal();
        
        toEval.add(child);
      }
    }
  }
  
  
  
  /**  Determining pre-conditions and keeping track of resource-reservations.
    */
  private boolean obtainedBefore(PlanStep step, Thing used) {
    if (preObtained.includes(used)) return true;
    for (PlanStep prior : steps) {
      if (prior == step) break;
      if (prior.doesGive(used)) return true;
    }
    return false;
  }
  
  
  private boolean neededDuring(PlanStep step, Thing used) {
    for (PlanStep other : steps) if (other.activeDuring(step)) {
      for (Thing o : other.needs) if (o == used) return true;
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
    float rating = 0, chance = 1.0f;
    for (PlanStep step : steps) {
      chance *= step.calcSuccessChance();
      rating += step.rating * chance;
    }
    return rating;
  }
  
}






