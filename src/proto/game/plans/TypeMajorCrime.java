


package proto.game.plans;
import proto.util.*;



public abstract class TypeMajorCrime extends StepType {
  
  
  static enum Needs {
    VENUE, EXPLOSIVE, BLUEPRINT, MOLE, ALARM_CRACKER
  };
  
  
  TypeMajorCrime(
    String name, Object needTypes[], Object giveTypes[]
  ) {
    super(name, needTypes, giveTypes);
  }
  

  Series <Thing> availableTargets(Object needType, Thing world) {
    if (needType == Needs.EXPLOSIVE) {
      Thing bomb = new Thing(Thing.TYPE_ITEM, "bomb");
      bomb.setValue(Thing.STAT_MAKE_DC, 5);
      bomb.setValue(Thing.PROP_BOMB, true);
      return new Batch(bomb);
    }
    if (needType == Needs.BLUEPRINT) {
      Thing print = new Thing(Thing.TYPE_ITEM, "print");
      print.setValue(Thing.STAT_MAKE_DC, 5);
      print.setValue(Thing.PROP_BLUEPRINT, true);
      return new Batch(print);
    }
    return super.availableTargets(needType, world);
  }
  
  
  boolean isNeeded(Object needType, PlanStep step) {
    Thing venue = step.need(Needs.VENUE);
    if (venue == null) return needType == Needs.VENUE;
    
    if (needType == Needs.EXPLOSIVE) {
      return venue.propValue(Thing.PROP_SAFE);
    }
    if (needType == Needs.BLUEPRINT) {
      return true;
    }
    if (needType == Needs.MOLE) {
      return true;
    }
    if (needType == Needs.ALARM_CRACKER) {
      return venue.propValue(Thing.PROP_SAFE);
    }
    return true;
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
    
    if (needType == Needs.BLUEPRINT) {
      if (used.type != Thing.TYPE_ITEM           ) return 0;
      if (! used .propValue(Thing.PROP_BLUEPRINT)) return 0;
      return 1;
    }
    
    if (needType == Needs.MOLE) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      if (used.owner != venue           ) return 0;
      return 1;
    }

    if (needType == Needs.ALARM_CRACKER) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      return used.statValue(Thing.STAT_WIRING) / 10f;
    }
    
    return 0;
  }
  
  
  float baseSuccessChance(PlanStep step) {
    float chance = 1.0f;
    
    float mookRatings = 1.5f;
    chance *= mookRatings / 3;
    
    return chance;
  }
  
  
  float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
}



