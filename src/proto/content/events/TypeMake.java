

package proto.content.events;
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
      return makesChance((Person) used, (Item) step.give(Gives.MADE));
    }
    if (needType == Needs.MATERIALS) {
      return 0;
    }
    return 0;
  }
  
  
  protected float baseSuccessChance(PlanStep step) {
    Person makes = (Person) step.need(Needs.MAKES);
    Item   made  = (Item  ) step.give(Gives.MADE );
    return makesChance(makes, made);
  }
  
  
  private float makesChance(Person makes, Item made) {
    if (makes == null || made == null) return 0.1f;
    //  TODO:  Adapt this to a wider array of potential skills, and unify with
    //  similar methods in the Task or Crafting class.
    float skill = 0;
    skill += makes.stats.levelFor(PersonStats.ENGINEERING);
    skill -= made.kind().craftDC(PersonStats.ENGINEERING);
    return Nums.clamp(skill / 10f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Make "+step.give(Gives.MADE);
  }
}






