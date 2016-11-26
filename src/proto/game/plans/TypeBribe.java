

package proto.game.plans;



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
  
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_PERSON) {
      return new PlanStep(this, by.plan).setGives(needed);
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Bribe "+step.give(Gives.BRIBED);
  }
  
  
}








