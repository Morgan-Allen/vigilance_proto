

package proto.content.events;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.plans.PlanStep;
import proto.game.plans.StepType;
import proto.util.*;



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
  
  
  
  protected PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Element.TYPE_ITEM) {
      return new PlanStep(this, by.plan).setGives(needed);
    }
    return null;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    final Item bought = (Item) step.need(Gives.BOUGHT);
    return (0 - bought.kind().buildCost) / 10f;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Buy "+step.give(Gives.BOUGHT)+" from "+step.need(Needs.VENDOR);
  }
  
}



