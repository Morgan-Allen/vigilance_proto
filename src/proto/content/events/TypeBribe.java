

package proto.content.events;
import proto.game.event.PlanStep;
import proto.game.event.StepType;
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
    if (needed.type == Element.TYPE_PERSON) {
      return new PlanStep(this, by.plan).setGives(needed);
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Bribe "+step.give(Gives.BRIBED);
  }
  
  
}

