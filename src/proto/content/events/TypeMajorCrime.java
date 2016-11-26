


package proto.content.events;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.plans.PlanStep;
import proto.game.plans.StepType;
import proto.content.items.*;
import proto.content.techs.*;
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
  

  protected Series <Element> availableTargets(Object needType, World world) {
    if (needType == Needs.EXPLOSIVE) {
      Item bomb = new Item(Clues.BOMB, world);
      return new Batch(bomb);
    }
    if (needType == Needs.BLUEPRINT) {
      Item print = new Item(Clues.BLUEPRINT, world);
      return new Batch(print);
    }
    return super.availableTargets(needType, world);
  }
  
  
  protected boolean isNeeded(Object needType, PlanStep step) {
    Place venue = (Place) step.need(Needs.VENUE);
    if (venue == null) return needType == Needs.VENUE;
    
    if (needType == Needs.EXPLOSIVE) {
      return venue.hasProperty(Facilities.REINFORCED);
    }
    if (needType == Needs.BLUEPRINT) {
      return true;
    }
    if (needType == Needs.MOLE) {
      return true;
    }
    if (needType == Needs.ALARM_CRACKER) {
      return venue.hasProperty(Facilities.REINFORCED);
    }
    return true;
  }
  
  
  protected float calcSuitability(
    Element used, Object needType, PlanStep step
  ) {
    Place venue = (Place) step.need(Needs.VENUE);
    if (needType == Needs.VENUE) {
      if (used.type != Element.TYPE_PLACE) return 0;
      return 1;
    }
    if (venue == null) return -1;
    
    if (needType == Needs.EXPLOSIVE) {
      if (! venue.hasProperty(Facilities.REINFORCED)) return 0;
      if (used.kind != Clues.BOMB) return 0;
      return 1;
    }
    
    if (needType == Needs.BLUEPRINT) {
      if (used.kind != Clues.BLUEPRINT) return 0;
      return 1;
    }
    
    if (needType == Needs.MOLE) {
      if (used.type != Element.TYPE_PERSON) return 0;
      Person mole = (Person) used;
      if (mole.resides() != venue) return 0;
      return 1;
    }

    if (needType == Needs.ALARM_CRACKER) {
      if (used.type != Element.TYPE_PERSON) return 0;
      final Person agent = (Person) used;
      return agent.stats.levelFor(PersonStats.ENGINEERING);
    }
    
    return 0;
  }
  
  
  protected float baseSuccessChance(PlanStep step) {
    float chance = 1.0f;
    
    float mookRatings = 1.5f;
    chance *= mookRatings / 3;
    
    return chance;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
}



