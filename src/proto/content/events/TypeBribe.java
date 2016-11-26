

package proto.content.events;
import proto.game.plans.PlanStep;
import proto.game.plans.StepType;
import proto.game.world.*;



public class TypeBribe extends StepType {
  
  
  static enum Needs {
  };
  static enum Gives {
    BRIBED
  };
  
  TypeBribe() { super(
    "Bribe",
    Needs.values(), Gives.values()
  ); }
  
  
  
  
  protected PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Element.TYPE_PERSON) {
      return new PlanStep(this, by.plan).setGives(needed);
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Bribe "+step.give(Gives.BRIBED);
  }
  
  
}


