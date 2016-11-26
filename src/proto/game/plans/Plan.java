

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
    goal.assignRating(priority);
    addStep(goal);
    fillNeeds(goal);
    I.say("Have added goal: "+goal.langDescription());
  }
  
  
  void advancePlan() {
    I.say("\nAdvancing plan-");
    
    Batch <PlanStep> nextGen = new Batch();
    for (PlanStep step : steps) {
      addStepsFrom(step, nextGen);
    }
    
    float sumRatings = 0;
    for (PlanStep child : nextGen) {
      I.say("  Rating for "+child.langDescription()+": "+child.rating());
      sumRatings += child.rating();
    }
    
    float rollInSum = Rand.num() * sumRatings;
    PlanStep picked = null;
    for (PlanStep child : nextGen) {
      rollInSum -= child.rating();
      if (rollInSum <= 0) { picked = child; break; }
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
      Thing    needed   = step.need(role);
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
  private boolean obtainedBefore(PlanStep step, Thing need) {
    //  TODO:  Make this a generic check for 'publicly known' things.
    if (need.type == Thing.TYPE_PLACE) return true;
    if (preObtained.includes(need)) return true;
    
    for (PlanStep prior : steps) {
      if (prior == step) break;
      if (prior.doesGive(need)) return true;
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
    float rating = 0, numSteps = 0;
    for (PlanStep step : steps) {
      rating += step.rating();
      numSteps++;
    }
    if (numSteps <= 0) return -1;
    return rating / numSteps;
  }
}






