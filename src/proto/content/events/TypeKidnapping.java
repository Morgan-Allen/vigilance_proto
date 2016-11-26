

package proto.content.events;
import proto.game.person.*;
import proto.game.plans.PlanStep;
import proto.game.world.*;
import proto.util.*;




public class TypeKidnapping extends TypeMajorCrime {
  
  static enum Gives {
    VICTIM
  };
  
  
  TypeKidnapping() {
    super("Kidnapping", Needs.values(), Gives.values());
  }
  
  
  protected PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Element.TYPE_PERSON) {
      PlanStep step = new PlanStep(this, by.plan);
      Person victim = (Person) needed;
      step.setGives(victim);
      step.setNeed(Needs.VENUE, victim.resides());
      return step;
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Kidnap "+step.give(Gives.VICTIM);
  }
}



