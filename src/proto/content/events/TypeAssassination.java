

package proto.content.events;
import proto.game.event.PlanStep;
import proto.game.world.*;



public class TypeAssassination extends TypeMajorCrime {
  
  static enum Gives {
    BODY
  };
  
  
  TypeAssassination() {
    super("Murder", "step_type_murder", Needs.values(), Gives.values());
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    //  TODO:  Use this step-type for revenge or to silence a witness.
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Assassinate "+step.give(Gives.BODY);
  }
}