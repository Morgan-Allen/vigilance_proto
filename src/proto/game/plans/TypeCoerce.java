

package proto.game.plans;
import proto.util.*;



public class TypeCoerce extends StepType {
  
  
  static enum Needs {
    HOSTAGE, DIRT
  };
  static enum Gives {
    COERCED
  };
  
  TypeCoerce() { super(
    "Coerce",
    Needs.values(), Gives.values()
  ); }
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_PERSON) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  Series <Thing> availableTargets(Object needType, Thing world) {
    if (needType == Needs.DIRT) {
      Thing dirt = new Thing(Thing.TYPE_ITEM, "dirt");
      dirt.setValue(Thing.PROP_DIRT, true);
      return new Batch(dirt);
    }
    return super.availableTargets(needType, world);
  }
  
  
  float calcSuitability(Thing used, Object needType, PlanStep step) {
    if (needType == Needs.DIRT) {
      
    }
    if (needType == Needs.HOSTAGE) {
      
    }
    return 0;
  }
  
  
  float calcSuccessChance(PlanStep step) {
    Thing coerced = step.give(Gives.COERCED);
    float talk = step.plan.agent.statValue(Thing.STAT_CHARM);
    talk += 5 - coerced.statValue(Thing.STAT_CHARM);
    
    //  TODO:  Factor in the value of leverage- either dirt or a hostage!
    return Nums.clamp(talk / 10f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Coerce "+step.give(Gives.COERCED)+" as "+step.parentNeedType;
  }
}









