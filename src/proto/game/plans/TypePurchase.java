

package proto.game.plans;



public class TypePurchase extends StepType {
  
  
  static enum Stages {
    BUYING
  };
  static enum Needs {
  };
  static enum Gives {
    BOUGHT
  };
  
  TypePurchase() { super(
    "Purchase",
    Stages.values(), Needs.values(), Gives.values()
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
  
}









