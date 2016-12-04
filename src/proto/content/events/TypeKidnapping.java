

package proto.content.events;
import proto.game.event.PlanStep;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;




public class TypeKidnapping extends TypeMajorCrime {
  
  static enum Gives {
    VICTIM
  };
  
  
  TypeKidnapping() {
    super("Kidnapping", "step_type_kidnap", Needs.values(), Gives.values());
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Element.TYPE_PERSON) {
      PlanStep step = new PlanStep(this, by.plan);
      Person victim = (Person) needed;
      step.setGive(Gives.VICTIM, victim);
      step.setNeed(Needs.VENUE, victim.resides(), step);
      return step;
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Kidnap "+step.give(Gives.VICTIM);
  }
}



