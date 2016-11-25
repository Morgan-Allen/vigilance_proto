

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
    I.say("Have added goal: "+goal.langDescription());
  }
  
  
  void advancePlan() {
    I.say("\nAdvancing plan...");
    
    Batch <PlanStep> nextGen = new Batch();
    for (PlanStep step : steps) addStepsFrom(step, nextGen);
    
    PlanStep picked = null;
    float bestRating = 0;
    for (PlanStep child : nextGen) {
      I.say("  Rating for "+child.langDescription()+": "+child.rating);
      if (child.rating <= bestRating) continue;
      picked     = child;
      bestRating = child.rating;
    }
    
    if (picked != null) {
      addStep(picked);
      I.say("  Adding step: "+picked.langDescription());
    }
    else {
      I.say("  No new step chosen!");
    }
  }
  
  
  
  /**  TODO:  Move some of these out to the PlanStep class?
    */
  private void addStep(PlanStep step) {
    step.uniqueID = steps.size();
    steps.addFirst(step);
    if (step.parent != null) {
      step.parent.setStepForNeed(step.parentNeedType, step);
    }
  }
  
  
  private void fillNeeds(PlanStep step) {
    for (Object role : step.needTypes()) {
      if (! step.type.isNeeded(role, step)) continue;
      Thing used = step.need(role);
      Series <Thing> available = step.type.availableTargets(role, world);
      if (available == null || used != null) continue;
      
      float bestRating = 0;
      for (Thing match : available) {
        if (neededDuring(step, match)) continue;
        float rating = step.calcSuitability(match, role);
        if (rating > bestRating) { used = match; bestRating = rating; }
      }
      step.setNeed(role, used);
    }
  }
  
  
  private void addStepsFrom(PlanStep step, Batch <PlanStep> toEval) {
    for (Object role : step.needTypes()) {
      Thing    needed   = step.need (role);
      PlanStep needStep = step.stepForNeed(role);
      if (needed == null || needStep != null) continue;
      if (canAccessBy(step, needed)         ) continue;
      
      PlanStep possible[] = step.actionsToObtain(needed, role);
      for (PlanStep child : possible) {
        fillNeeds(child);
        child.bindParent(step, role);
        
        float chance = child.calcSuccessChance();
        child.rating = chance * step.rating;
        child.rating += chance * child.baseAppeal();
        child.rating -= (1 - chance) * child.baseFailRisk();
        
        toEval.add(child);
      }
    }
  }
  
  
  
  /**  Determining pre-conditions and keeping track of resource-reservations.
    */
  private boolean canAccessBy(PlanStep step, Thing used) {
    //  TODO:  Make this a generic check for 'publicly known' things.
    if (used.type == Thing.TYPE_PLACE) return true;
    
    if (preObtained.includes(used)) return true;
    for (PlanStep prior : steps) {
      if (prior == step) break;
      if (prior.doesGive(used)) return true;
    }
    return false;
  }
  
  
  private boolean neededDuring(PlanStep step, Thing used) {
    for (PlanStep other : steps) if (other.activeDuring(step)) {
      for (Thing o : other.needs()) if (o == used) return true;
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






