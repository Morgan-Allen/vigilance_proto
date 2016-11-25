

package proto.game.plans;



public class TypeAssassination extends TypeMajorCrime {
  
  static enum Gives {
    BODY
  };
  
  
  TypeAssassination() { super(
    "Murder", Needs.values(), Gives.values()
  ); }
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    //  TODO:  Use this step-type for revenge or to silence a witness.
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Assassinate "+step.give(Gives.BODY);
  }
}