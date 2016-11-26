

package proto.game.plans;
import proto.util.*;


public class TypeMake extends StepType {
  
  
  static enum Needs {
    MAKES, MATERIALS
  };
  static enum Gives {
    MADE
  };
  
  TypeMake() { super(
    "Make",
    Needs.values(), Gives.values()
  ); }
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_ITEM) {
      return new PlanStep(this, by.plan).setGives(needed);
    }
    return null;
  }
  
  
  float calcSuitability(Thing used, Object needType, PlanStep step) {
    if (needType == Needs.MAKES) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      float skill = used.statValue(Thing.STAT_WIRING);
      return skill / 10f;
    }
    if (needType == Needs.MATERIALS) {
      return 0;
    }
    return 0;
  }
  
  
  float calcSuccessChance(PlanStep step) {
    Thing makes = step.need(Needs.MAKES);
    Thing made  = step.need(Gives.MADE );
    
    float skill  = makes.statValue(Thing.STAT_WIRING );
    skill       -= made .statValue(Thing.STAT_MAKE_DC);
    return Nums.clamp(skill / 10f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Make "+step.give(Gives.MADE);
  }
}



