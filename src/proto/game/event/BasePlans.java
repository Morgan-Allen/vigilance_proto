

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
    //
    //  If you currently lack a viable plan, and/or it's finished, generate a
    //  new one from scratch-
    if (currentPlan == null || planComplete(currentPlan)) {
      currentPlan = generateNextPlan(numPlans, maxDepth, null);
    }
    if (currentPlan == null) return;
    //
    //  If you don't have a step scheduled, or the current step is complete,
    //  move on to the next step in the plan.
    if (nextStep == null || nextStep.matchedEvent().complete()) {
      if (nextStep == null) nextStep = currentPlan.firstStep();
      else nextStep = currentPlan.stepAfter(nextStep);
      //
      //  In the event that the next step is possible, schedule the
      //  corresponding event.  Otherwise, considering revising the plan or
      //  abandoning it entirely.
      if (nextStep.currentlyPossible()) {
        int hoursDelay = 20 + Rand.index(5);
        base.world().events.scheduleEvent(nextStep.matchedEvent(), hoursDelay);
      }
      else {
        currentPlan.reviseAfter(nextStep, maxDepth / 2, base);
        currentPlan = generateNextPlan(1, maxDepth, currentPlan);
        nextStep = null;
      }
    }
  }
  
  
  private Plan generateNextPlan(int numPlans, int maxDepth, Plan current) {
    Plan picked = null;
    float bestRating = 0;
    
    if (current != null) {
      picked = current;
      bestRating = current.calcPlanRating() * 2;
    }
    
    for (int n = numPlans; n-- > 0;) {
      final Plan plan = new Plan(base.leader(), base.world(), stepTypes);
      plan.selectInitialGoal(base);
      plan.advancePlan(maxDepth);
      float rating = plan.calcPlanRating();
      if (rating > bestRating) { picked = plan; bestRating = rating; }
    }
    
    if (picked != null) picked.printFullPlan();
    return picked;
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









