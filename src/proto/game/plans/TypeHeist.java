

package proto.game.plans;
import proto.util.*;



public class TypeHeist extends TypeMajorCrime {

  static enum Gives {
    LOOT
  };
  
  
  TypeHeist() { super(
    "Heist", Needs.values(), Gives.values()
  ); }
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    //  TODO:  Use this step-type to acquire items or cash...
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Heist to steal "+step.give(Gives.LOOT);
  }
}

