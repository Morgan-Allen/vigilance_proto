

package proto.game.plans;
import proto.util.*;



public class TypeCoerce extends StepType {
  
  
  static enum Stages {
    TALKING, UNDER_COERCION
  };
  static enum Needs {
  };
  static enum Gives {
    COERCED
  };
  
  TypeCoerce() { super(
    "Coerce",
    Stages.values(), Needs.values(), Gives.values()
  ); }
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_PERSON) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  float calcSuccessChance(PlanStep step) {
    Thing coerced = step.give(Gives.COERCED);
    float talk = step.plan.agent.statValue(Thing.STAT_CHARM);
    talk += 5 - coerced.statValue(Thing.STAT_CHARM);
    return Nums.clamp(talk / 10f, 0, 1);
  }
  
}




