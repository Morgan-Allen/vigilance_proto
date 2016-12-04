

package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.world.*;



public class TypeBribe extends StepType {
  
  
  static enum Needs {
  };
  static enum Gives {
    BRIBED
  };
  
  TypeBribe() {
    super("Bribe", "step_type_bribe", Needs.values(), Gives.values());
  }
  
  
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Kind.TYPE_PERSON) {
      return new PlanStep(this, by.plan).setGive(Gives.BRIBED, needed);
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Bribe "+step.give(Gives.BRIBED);
  }
  
  
}


