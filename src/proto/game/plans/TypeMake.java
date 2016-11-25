

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
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  float calcSuccessChance(PlanStep step) {
    Thing makes = step.need(Needs.MAKES);
    Thing made  = step.need(Gives.MADE );
    
    float skill  = makes.statValue(Thing.STAT_WIRING );
    skill       -= made .statValue(Thing.STAT_MAKE_DC);
    return Nums.clamp(skill / 10f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Make "+step.give(Gives.MADE)+" from "+step.need(Needs.MATERIALS);
  }
}



