


package proto.content.events;
import proto.common.*;
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
  
  
  protected float urgency(Object needType, PlanStep step) {
    if (needType == Needs.VENUE) return 2;
    
    Place venue = (Place) step.need(Needs.VENUE);
    if (venue == null) return 0;
    
    boolean reinforced = venue.kind().hasFurnitureType(Facilities.REINFORCED);
    boolean alarmed    = venue.kind().hasFurnitureType(Facilities.ALARMED   );
    
    //  TODO:  Don't bother with the mutual exclusion- just have the needs-set
    //  tweaked by the rigors of individual sub-classes and/or the properties
    //  of the mark during object setup.
    
    if (needType == Needs.ENFORCER) {
      return 1;
    }
    if (needType == Needs.BLUEPRINT) {
      if (step.satisfied(Needs.MOLE)) return 0;
      return 0.5f;
    }
    if (needType == Needs.MOLE) {
      if (step.satisfied(Needs.BLUEPRINT)) return 0;
      return 0.5f;
    }
    if (needType == Needs.EXPLOSIVE) {
      return reinforced ? 1 : 0;
    }
    if (needType == Needs.ALARM_CRACKER) {
      return alarmed ? 0.5f : 0;
    }
    return 0;
  }
  
  
  protected float calcFitness(
    Element used, Object needType, PlanStep step
  ) {
    Place venue = (Place) step.need(Needs.VENUE);
    if (needType == Needs.VENUE) {
      if (used.type != Kind.TYPE_PLACE) return 0;
      return 1;
    }
    if (venue == null) return -1;
    
    if (needType == Needs.ENFORCER) {
      if (used.type != Kind.TYPE_PERSON) return 0;
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
      if (used.type != Kind.TYPE_PERSON) return 0;
      Person mole = (Person) used;
      if (mole.resides() != venue) return 0;
      return 1;
    }
    if (needType == Needs.ALARM_CRACKER) {
      if (used.type != Kind.TYPE_PERSON) return 0;
      final Person agent = (Person) used;
      return agent.stats.levelFor(PersonStats.ENGINEERING);
    }
    return 0;
  }
  
  
  protected float baseFailCost(PlanStep step) {
    //
    //  There's always an element of risk involved in flouting the law this
    //  blatantly.  TODO:  Modify based on local/global conviction stats?
    return 5;
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




