

package proto.game.plans;



public class TypePurchase extends StepType {
  
  
  static enum Needs {
    VENDOR
  };
  static enum Gives {
    BOUGHT
  };
  
  TypePurchase() { super(
    "Purchase",
    Needs.values(), Gives.values()
  ); }
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_ITEM) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  float baseAppeal(PlanStep step) {
    final Thing bought = step.need(Gives.BOUGHT);
    return (0 - bought.statValue(Thing.STAT_BUY_COST)) / 10f;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Buy "+step.give(Gives.BOUGHT)+" from "+step.need(Needs.VENDOR);
  }
  
}



