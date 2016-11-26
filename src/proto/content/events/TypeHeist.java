

package proto.content.events;
import proto.game.plans.PlanStep;
import proto.game.world.*;
import proto.util.*;



public class TypeHeist extends TypeMajorCrime {

  static enum Gives {
    LOOT
  };
  
  
  TypeHeist() {
    super("Heist", Needs.values(), Gives.values());
  }
  
  
  protected PlanStep toProvide(Element needed, PlanStep by) {
    
    //  TODO:  Include bulk cash as a form of item!
    if (needed.type == Element.TYPE_ITEM) {
      PlanStep step = new PlanStep(this, by.plan);
      step.setGives(needed);
      return step;
    }
    return null;
  }
  
  
  protected float calcSuitability(
    Element used, Object needType, PlanStep step
  ) {
    if (needType == Needs.VENUE) {
      //  TODO:  You need to ensure that the venue in question can produce this
      //  item!
    }
    return super.calcSuitability(used, needType, step);
  }


  protected String langDescription(PlanStep step) {
    return "Steal "+step.give(Gives.LOOT)+" from "+step.need(Needs.VENUE);
  }
}













