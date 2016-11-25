

package proto.game.plans;
import proto.util.*;



public class TypeKidnapping extends TypeMajorCrime {
  
  static enum Gives {
    VICTIM
  };
  
  
  TypeKidnapping() { super(
    "Kidnapping", Needs.values(), Gives.values()
  ); }
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_PERSON) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Kidnap "+step.give(Gives.VICTIM);
  }
}
