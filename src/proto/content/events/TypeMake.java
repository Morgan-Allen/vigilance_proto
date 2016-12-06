

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
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
    if (needed.type == Kind.TYPE_ITEM) {
      return new PlanStep(this, by.plan).setGive(Gives.MADE, needed);
    }
    return null;
  }
  
  
  protected float urgency(Object needType, PlanStep step) {
    final Item made = (Item) step.give(Gives.MADE);
    if (made == null) return 0;
    
    if (needType == Needs.MAKES) {
      return 1;
    }
    if (needType == Needs.MATERIALS) {
      return made.kind().isCustom() ? 0 : 0.5f;
    }
    return 0;
  }
  
  
  protected float calcFitness(
    Element used, Object needType, PlanStep step
  ) {
    if (needType == Needs.MAKES) {
      if (used.type != Kind.TYPE_PERSON) return 0;
      return makesChance((Person) used, (Item) step.give(Gives.MADE));
    }
    if (needType == Needs.MATERIALS) {
      return 0;
    }
    return 0;
  }
  
  
  private float makesChance(Person makes, Item made) {
    if (makes == null || made == null) return 0;
    //  TODO:  Adapt this to a wider array of potential skills, and unify with
    //  similar methods in the Task or Crafting class.
    float skill = 0;
    skill += makes.stats.levelFor(PersonStats.ENGINEERING);
    skill -= made.kind().craftDC (PersonStats.ENGINEERING);
    return Nums.clamp(skill / 5f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Make "+step.give(Gives.MADE);
  }
}






