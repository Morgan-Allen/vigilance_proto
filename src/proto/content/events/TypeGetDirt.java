

package proto.content.events;
import proto.game.event.PlanStep;
import proto.game.event.StepType;
import proto.game.world.*;
import proto.util.*;



public class TypeGetDirt extends StepType {
  
  
  static enum Needs {
  };
  static enum Gives {
    DIRT
  };
  
  
  
  TypeGetDirt() {
    super("Get Dirt", "step_type_get_dirt", Needs.values(), Gives.values());
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    //  TODO:  You need to be able to look for dirt without a definite idea of
    //  what you're looking for.  (i.e, you specify some property of the thing
    //  needed.
    
    /*
    if (needed.type == Element.TYPE_CLUE) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    //*/
    return null;
  }
  
  
  protected float calcSuccessChance(PlanStep step) {
    
    return 0;
    /*
    Element dirt = step.give(Gives.COERCED);
    float talk = step.plan.agent.statValue(Element.STAT_CHARM);
    talk += 5 - coerced.statValue(Element.STAT_CHARM);
    return Nums.clamp(talk / 10f, 0, 1);
    //*/
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Get Dirt on subject";
  }
  
  
}








