

package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class TypeResearch extends StepType {
  
  
  static enum Needs {
    MAKES
  };
  static enum Gives {
    FINDS
  };
  
  
  
  TypeResearch() {
    super("Get Dirt", "step_type_get_dirt", null, Needs.values(), Gives.values());
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    //  TODO:  You need to be able to look for dirt without a definite idea of
    //  what you're looking for.  (i.e, you specify some property of the thing
    //  needed.
    
    /*
    if (needed.type == Kind.TYPE_CLUE) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    //*/
    return null;
  }
  
  
  protected float calcFitness(
    Element used, Object needType, PlanStep step
  ) {
    if (needType == Needs.MAKES) {
      if (used.type != Kind.TYPE_PERSON) return 0;
      return findsChance((Person) used, (Item) step.give(Gives.FINDS));
    }
    return 0;
  }
  
  
  
  protected float baseSuccessChance(PlanStep step) {
    Person makes = (Person) step.need(Needs.MAKES);
    Item   made  = (Item  ) step.give(Gives.FINDS);
    return findsChance(makes, made);
  }
  
  
  private float findsChance(Person makes, Item made) {
    //  TODO:  Adapt this to a wider array of potential skills, and unify with
    //  similar methods in the Task or Crafting class.
    float skill = 0;
    skill += makes.stats.levelFor(PersonStats.INFORMATICS);
    skill -= made.kind().craftDC(PersonStats.INFORMATICS);
    return Nums.clamp(skill / 5f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Get Dirt on subject";
  }
  
  
}








