

package proto.game.plans;
import proto.util.*;



public class TypeGetDirt extends StepType {
  
  
  static enum Needs {
  };
  static enum Gives {
    DIRT
  };
  
  
  
  TypeGetDirt() { super(
    "Get Dirt",
    Needs.values(), Gives.values()
  ); }
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    //  TODO:  You need to be able to look for dirt without a definite idea of
    //  what you're looking for.  (i.e, you specify some property of the thing
    //  needed.
    
    /*
    if (needed.type == Thing.TYPE_CLUE) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    //*/
    return null;
  }
  
  
  float calcSuccessChance(PlanStep step) {
    
    return 0;
    /*
    Thing dirt = step.give(Gives.COERCED);
    float talk = step.plan.agent.statValue(Thing.STAT_CHARM);
    talk += 5 - coerced.statValue(Thing.STAT_CHARM);
    return Nums.clamp(talk / 10f, 0, 1);
    //*/
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Get Dirt on subject";
  }
  
  
}








