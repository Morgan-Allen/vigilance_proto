

package proto.game.plans;
import proto.util.*;



public class TypeHeist extends StepType {
  
  
  static enum Stages {
    PLAN, TRAVEL, SCENE, GETAWAY
  };
  static enum Needs {
    VENUE, EXPLOSIVE, MOLE, DRIVER, LOOKOUT, MUSCLE
  };
  static enum Gives {
    LOOT
  };
  
  TypeHeist() { super(
    "Heist",
    Stages.values(), Needs.values(), Gives.values()
  ); }
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    //  TODO:  Use this step-type to acquire items or cash...
    return null;
  }
  

  Series <Thing> availableTargets(Object needType, Thing world) {
    if (needType == Needs.EXPLOSIVE) {
      Thing bomb = new Thing(Thing.TYPE_ITEM, "bomb");
      bomb.setValue(Thing.STAT_MAKE_DC, 5);
      bomb.setValue(Thing.PROP_BOMB, true);
      return new Batch(bomb);
    }
    return super.availableTargets(needType, world);
  }
  
  
  float calcSuitability(Thing used, Object needType, PlanStep step) {
    
    Thing venue = step.need(Needs.VENUE);
    if (needType == Needs.VENUE) {
      if (used.type != Thing.TYPE_PLACE) return 0;
      return 1;
    }
    if (venue == null) return -1;
    
    if (needType == Needs.EXPLOSIVE) {
      if (used.type != Thing.TYPE_ITEM      ) return 0;
      if (! venue.propValue(Thing.PROP_SAFE)) return 0;
      if (! used .propValue(Thing.PROP_BOMB)) return 0;
      return 1;
    }
    
    if (needType == Needs.MOLE) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      if (used.owner != venue           ) return 0;
      return 1;
    }
    
    if (needType == Needs.DRIVER) {
      return rateStatLevel(used, Thing.STAT_DRIVING);
    }
    if (needType == Needs.LOOKOUT) {
      return rateStatLevel(used, Thing.STAT_SMARTS);
    }
    if (needType == Needs.MUSCLE) {
      return rateStatLevel(used, Thing.STAT_BRAWL);
    }
    return 0;
  }
  
  
  float baseSuccessChance(PlanStep step) {
    float chance = 1.0f;
    
    float mookRatings = 0;
    mookRatings += rateStatLevel(step.need(Needs.DRIVER ), Thing.STAT_DRIVING);
    mookRatings += rateStatLevel(step.need(Needs.LOOKOUT), Thing.STAT_SMARTS );
    mookRatings += rateStatLevel(step.need(Needs.MUSCLE ), Thing.STAT_BRAWL  );
    chance *= mookRatings / 3;
    
    return chance;
  }
  
  
  float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
  
}










