

package proto.game.event;
import proto.game.world.*;
import proto.common.*;
import proto.util.*;



public class BasePlans {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  private StepType stepTypes[] = new StepType[0];
  private Plan currentPlan;
  private PlanStep nextStep;
  
  
  public BasePlans(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    stepTypes   = (StepType[]) s.loadObjectArray(StepType.class);
    currentPlan = (Plan    ) s.loadObject();
    nextStep    = (PlanStep) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(stepTypes);
    s.saveObject(currentPlan);
    s.saveObject(nextStep   );
  }
  
  
  
  /**  Assigning step-types and updating plans:
    */
  public void assignStepTypes(StepType... types) {
    this.stepTypes = types;
  }
  
  
  public void updatePlanning() {
    if (Visit.empty(stepTypes)) return;
    final int maxDepth = 6, numPlans = 4;
    
    if (currentPlan == null || planComplete(currentPlan)) {
      Plan picked = null;
      float bestRating = 0;
      
      for (int n = numPlans; n-- > 0;) {
        final Plan plan = new Plan(base.leader(), base.world(), stepTypes);
        plan.selectInitialGoal(base);
        plan.advancePlan(maxDepth);
        float rating = plan.calcPlanRating();
        if (rating > bestRating) { picked = plan; bestRating = rating; }
      }
      
      currentPlan = picked;
      nextStep = picked == null ? null : picked.steps().first();
      
      if (picked != null) picked.printFullPlan();
    }
    if (currentPlan == null) return;
    
    PlanStep after = currentPlan.stepAfter(nextStep);
    if (after != null && nextStep.matchedEvent().complete()) {
      if (after.currentlyPossible()) {
        int hoursDelay = 20 + Rand.index(5);
        base.world().events.scheduleEvent(after.matchedEvent(), hoursDelay);
      }
      else {
        currentPlan.reviseAfter(nextStep, maxDepth / 2, base);
      }
    }
  }
  
  
  public boolean planComplete(Plan plan) {
    if (plan != currentPlan) return true;
    if (currentPlan == null || nextStep == null) return false;
    if (! nextStep.matchedEvent().complete()) return false;
    return nextStep == currentPlan.steps().last();
  }
  
  
  public PlanStep stepAfter(PlanStep step) {
    if (currentPlan == null || ! currentPlan.steps().includes(step)) {
      return null;
    }
    Series <PlanStep> steps = currentPlan.steps();
    int index = steps.indexOf(step) + 1;
    if (index >= steps.size()) return null;
    return steps.atIndex(index);
  }
  
}









