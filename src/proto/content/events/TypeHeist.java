

package proto.content.events;
import proto.game.event.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.content.items.*;
import proto.util.*;



public class TypeHeist extends TypeMajorCrime {

  static enum Gives {
    LOOT
  };
  
  
  TypeHeist() {
    super("Heist", "step_type_heist", Needs.values(), Gives.values());
  }
  
  
  public PlanStep asGoal(Element target, Plan plan) {
    if (target.type == Element.TYPE_PLACE) {
      final Place place = (Place) target;
      if (place.kind().incomeFrom(place.region()) <= 0) return null;
      
      PlanStep step = new PlanStep(this, plan);
      Item cash = new Item(Clues.CASH, plan.world);
      step.setGives(cash);
      step.setNeed(Needs.VENUE, place);
      return step;
    }
    return null;
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Element.TYPE_ITEM) {
      PlanStep step = new PlanStep(this, by.plan);
      step.setGives(needed);
      return step;
    }
    return null;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    Item loot = (Item) step.give(Gives.LOOT);
    final Place place = (Place) step.need(Needs.VENUE);
    if (loot.kind() == Clues.CASH) {
      return 5.0f * place.kind().incomeFrom(place.region());
    }
    else return super.baseAppeal(step);
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













