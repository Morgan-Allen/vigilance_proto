

package proto.content.events;
import proto.game.world.*;
import proto.game.event.PlanStep;
import proto.game.event.StepType;
import proto.game.person.*;
import proto.content.items.*;
import proto.util.*;



public class TypeMake extends StepType {
  
  
  static enum Needs {
    MAKES, MATERIALS
  };
  static enum Gives {
    MADE
  };
  
  TypeMake() {
    super("Make", "step_type_make", Needs.values(), Gives.values());
  }
  
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Element.TYPE_ITEM) {
      return new PlanStep(this, by.plan).setGives(needed);
    }
    return null;
  }
  
  
  protected float calcSuitability(
    Element used, Object needType, PlanStep step
  ) {
    if (needType == Needs.MAKES) {
      if (used.type != Element.TYPE_PERSON) return 0;
      Person makes = (Person) used;
      Item   made  = (Item  ) step.need(Gives.MADE);
      float skill = makes.stats.levelFor(PersonStats.ENGINEERING);
      skill       -= (Integer) made.kind().craftArgs[1];
      return skill / 10f;
    }
    if (needType == Needs.MATERIALS) {
      return 0;
    }
    return 0;
  }
  
  
  protected float calcSuccessChance(PlanStep step) {
    Person makes = (Person) step.need(Needs.MAKES);
    Item   made  = (Item  ) step.need(Gives.MADE );
    
    float skill = makes.stats.levelFor(PersonStats.ENGINEERING);
    skill       -= (Integer) made.kind().craftArgs[1];
    return Nums.clamp(skill / 10f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Make "+step.give(Gives.MADE);
  }
}






