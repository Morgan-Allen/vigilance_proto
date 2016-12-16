

package proto.content.events;
import proto.game.event.*;
import proto.game.world.*;



public class TypeMurder extends TypeMajorCrime {
  
  static enum Gives {
    BODY
  };
  
  
  TypeMurder() {
    super("Murder", "step_type_murder", Needs.values(), Gives.values());
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    //  TODO:  Use this step-type for revenge or to silence a witness.
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "murdering "+step.give(Gives.BODY);
  }
}