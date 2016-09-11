

package proto.game.plans;
import proto.util.*;
import static proto.game.plans.ActionType.*;



public class Plan {
  
  Thing agent;
  Thing world;
  
  List <Thing> preObtained = new List();
  List <Action> steps = new List();
  
  
  Plan(Thing agent, Thing world) {
    this.agent = agent;
    this.world = world;
  }
  
  
  void addObtained(Thing thing) {
    preObtained.add(thing);
  }
  
  
  void addGoal(Action goal, float priority) {
    goal.rating = priority;
    addStep(goal);
    fillNeeds(goal);
    I.say("Have added goal: "+goal.longDescription());
  }
  
  
  void advancePlan() {
    I.say("\nAdvancing plan...");
    
    Batch <Action> nextGen = new Batch();
    for (Action step : steps) addStepsFrom(step, nextGen);
    
    Action picked = null;
    float bestRating = 0;
    for (Action child : nextGen) {
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
  private void addStep(Action step) {
    step.uniqueID = steps.size();
    steps.addFirst(step);
    if (step.parent != null) step.parent.needSteps[step.parentNeedID] = step;
  }
  
  
  private void fillNeeds(Action step) {
    for (Role role : step.type.needs) {
      Thing used = step.needs[role.ID];
      if (used != null) continue;
      
      //  TODO:  You need to skip over anything current required by another
      //  step.
      float bestRating = 0;
      for (Thing match : world.inside) {
        if (neededDuring(step, match)) continue;
        float rating = step.type.calcSuitability(match, role, step);
        if (rating > bestRating) { used = match; bestRating = rating; }
      }
      step.needs[role.ID] = used;
    }
  }
  
  
  private void addStepsFrom(Action step, Batch <Action> toEval) {
    for (Role role : step.type.needs) {
      Thing needed = step.needs[role.ID];
      if (needed == null                 ) continue;
      if (step.needSteps[role.ID] != null) continue;
      if (obtainedBefore(step, needed)   ) continue;
      
      Action possible[] = step.type.actionsToObtain(needed, role, step);
      for (Action child : possible) {
        fillNeeds(child);
        child.bindParent(step, role);
        
        float chance = child.type.calcSuccessChance(child);
        child.rating = chance * step.rating;
        child.rating += child.type.baseAppeal(child, this);
        
        toEval.add(child);
      }
    }
  }
  
  
  
  /**  Determining pre-conditions and keeping track of resource-reservations.
    */
  private boolean obtainedBefore(Action step, Thing used) {
    if (preObtained.includes(used)) return true;
    for (Action prior : steps) {
      if (prior == step) break;
      if (prior.type.provides(prior, used)) return true;
    }
    return false;
  }
  
  
  private boolean neededDuring(Action step, Thing used) {
    for (Action other : steps) if (other.type.activeDuring(other, step)) {
      for (Thing o : other.needs) if (o == used) return true;
    }
    return false;
  }
  
  
  
  /**  Evaluating success-chance:
    */
  public float successChance() {
    float chance = 1.0f;
    for (Action step : steps) {
      chance *= step.type.calcSuccessChance(step);
    }
    return chance;
  }
  
  
  public float calcPlanRating() {
    float rating = 0, chance = 1.0f;
    for (Action step : steps) {
      chance *= step.type.calcSuccessChance(step);
      rating += step.rating * chance;
    }
    return rating;
  }
  
}






