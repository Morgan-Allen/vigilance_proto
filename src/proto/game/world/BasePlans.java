

package proto.game.world;
import proto.game.event.*;
import proto.common.*;
import proto.util.*;



public class BasePlans {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  private StepType stepTypes[] = new StepType[0];
  private Plan currentPlan;
  private Event nextEvent;
  
  
  BasePlans(Base base) {
    this.base = base;
  }
  
  
  void loadState(Session s) throws Exception {
    stepTypes   = (StepType[]) s.loadObjectArray(StepType.class);
    currentPlan = (Plan ) s.loadObject();
    nextEvent   = (Event) s.loadObject();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjectArray(stepTypes);
    s.saveObject(currentPlan);
    s.saveObject(nextEvent  );
  }
  
  
  
  /**  Assigning step-types and updating plans:
    */
  public void assignStepTypes(StepType... types) {
    this.stepTypes = types;
  }
  
  
  void updatePlanning() {
    if (Visit.empty(stepTypes)) return;
    
    if (currentPlan == null || planComplete()) {
      
      final int maxDepth = 6, numPlans = 4;
      Plan picked = null;
      float bestRating = 0;
      
      for (int n = numPlans; n-- > 0;) {
        final Plan plan = new Plan(base.leader, base.world, stepTypes);
        plan.verbose = true;
        for (Element goon : base.roster()) plan.addObtained(goon);
        plan.selectInitialGoal();
        plan.advancePlan(maxDepth);
        float rating = plan.calcPlanRating();
        if (rating > bestRating) { picked = plan; bestRating = rating; }
      }
      
      currentPlan = picked;
      if (picked != null) {
        picked.printFullPlan();
        I.say("Plan picked...");
      }
    }
    
    if (currentPlan != null && (nextEvent == null || nextEvent.complete())) {
      Series <PlanStep> steps = currentPlan.steps();
      PlanStep next = null;
      if (nextEvent == null) next = steps.first();
      else next = steps.atIndex(steps.indexOf(nextEvent.planStep()) + 1);
      
      if (next != null) {
        nextEvent = next.spawnEvent(base.world, 24);
        base.world.events.scheduleEvent(nextEvent);
      }
    }
  }
  
  
  public boolean planComplete() {
    if (currentPlan == null || nextEvent == null) return false;
    if (! nextEvent.complete()) return false;
    return nextEvent.planStep() == currentPlan.steps().last();
  }
  
}

