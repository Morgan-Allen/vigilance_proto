

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.PlanStep;
import proto.game.event.StepType;
import proto.game.person.*;
import proto.util.*;



public class TypePurchase extends StepType {
  
  
  static enum Needs {
    VENDOR
  };
  static enum Gives {
    BOUGHT
  };
  
  TypePurchase() {
    super("Purchase", "step_type_purchase", Needs.values(), Gives.values());
  }
  
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Kind.TYPE_ITEM) {
      return new PlanStep(this, by.plan).setGive(Gives.BOUGHT, needed);
    }
    return null;
  }
  
  
  protected float calcSuitability(
    Element used, Object needType, PlanStep step
  ) {
    return super.calcSuitability(used, needType, step);
  }
  
  
  protected float baseSuccessChance(PlanStep step) {
    return 1;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    final Item bought = (Item) step.need(Gives.BOUGHT);
    return (0 - bought.kind().buildCost) / 10f;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Buy "+step.give(Gives.BOUGHT)+" from "+step.need(Needs.VENDOR);
  }
  
}



