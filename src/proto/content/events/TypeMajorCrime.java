


package proto.content.events;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.content.items.*;
import proto.content.places.*;
import proto.util.*;



public abstract class TypeMajorCrime extends StepType {
  
  
  public static enum Needs {
    VENUE, ENFORCER, EXPLOSIVE, BLUEPRINT, MOLE, ALARM_CRACKER
  };
  
  
  TypeMajorCrime(
    String name, String ID, Object needTypes[], Object giveTypes[]
  ) {
    super(name, ID, needTypes, giveTypes);
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
    
    boolean reinforced = venue.kind().hasFurnitureType(Facilities.REINFORCED);
    boolean alarmed    = venue.kind().hasFurnitureType(Facilities.ALARMED   );
    
    if (needType == Needs.EXPLOSIVE) {
      return reinforced;
    }
    if (needType == Needs.ENFORCER) {
      return true;
    }
    if (needType == Needs.BLUEPRINT) {
      return true;
    }
    if (needType == Needs.MOLE) {
      return true;
    }
    if (needType == Needs.ALARM_CRACKER) {
      return alarmed;
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
    
    if (needType == Needs.ENFORCER) {
      if (used.type != Element.TYPE_PERSON) return 0;
      Person enforcer = (Person) used;
      if (step.plan.agent.base() != enforcer.base()) return 0;
      return enforcer.stats.powerLevel();
    }
    if (needType == Needs.EXPLOSIVE) {
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
    
    //  TODO:  Update this!
    
    float mookRatings = 1.5f;
    chance *= mookRatings / 3;
    
    return chance;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
  
  /**  Scene-specific AI directions-
    */
  public Action specialAction(Person onTeam, PlanStep step, Scene scene) {
    
    //  TODO:  Implement suitable auxiliary steps here!
    
    /*
    if (onTeam == step.need(Needs.ALARM_CRACKER)) {
      for (Prop match : scene.propsWithTrait(Facilities.REINFORCED)) {
        Action action = new Action(Common.SPECIAL_ACTION, onTeam, match);
        return action;
      }
    }
    if (onTeam == step.need(Needs.MOLE)) {
      
    }
    //*/
    
    //  TODO:  Include the use of explosives and blueprints as well.
    return null;
  }
  
  
  public boolean onSpecialActionEnd(Action action) {
    
    //  TODO:  Implement suitable auxiliary actions here!
    
    //Prop safe = (Prop) action.target;
    //action.scene().removeProp(safe);
    return true;
  }
  
  
}









