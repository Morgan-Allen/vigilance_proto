

package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;




public class TypeKidnap extends TypeMajorCrime {
  
  static enum Gives {
    VICTIM
  };
  
  
  TypeKidnap() {
    super("Kidnapping", "step_type_kidnap", Needs.values(), Gives.values());
  }
  
  
  public PlanStep asGoal(Element target, Plan plan) {
    if (target.isPerson()) {
      PlanStep step = new PlanStep(this, plan);
      Person victim = (Person) target;
      step.setGive(Gives.VICTIM, victim);
      step.setNeed(Needs.VENUE, victim.resides(), step);
      return step;
    }
    return null;
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.isPerson()) {
      PlanStep step = new PlanStep(this, by.plan);
      Person victim = (Person) needed;
      step.setGive(Gives.VICTIM, victim);
      step.setNeed(Needs.VENUE, victim.resides(), step);
      return step;
    }
    return null;
  }
  
  
  protected String langDescription(PlanStep step) {
    return "kidnapping "+step.give(Gives.VICTIM);
  }
}







