

package proto.content.events;
import proto.game.world.*;
import proto.game.event.PlanStep;
import proto.game.event.StepType;
import proto.game.person.*;
import proto.common.*;
import proto.content.items.*;
import proto.util.*;



public class TypeCoerce extends StepType {
  
  
  static enum Needs {
    HOSTAGE, DIRT
  };
  static enum Gives {
    COERCED
  };
  
  TypeCoerce() {
    super("Coerce", "step_type_coerce", null, Needs.values(), Gives.values());
  }
  
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Kind.TYPE_PERSON) {
      return new PlanStep(this, by.plan).setGive(Gives.COERCED, needed);
    }
    return null;
  }
  
  
  protected Series <Element> availableTargets(Object needType, World world) {
    if (needType == Needs.DIRT) {
      Item dirt = new Item(Clues.EVIDENCE, world);
      return new Batch(dirt);
    }
    return super.availableTargets(needType, world);
  }
  
  
  protected float calcFitness(
    Element used, Object needType, PlanStep step
  ) {
    if (needType == Needs.DIRT) {
      
    }
    if (needType == Needs.HOSTAGE) {
      
    }
    return 0;
  }
  
  
  protected float calcSuccessChance(PlanStep step) {
    Person coerced = (Person) step.give(Gives.COERCED);
    float talk = step.plan.agent.stats.levelFor(PersonStats.SOCIAL);
    talk += 5 - coerced.stats.levelFor(PersonStats.SOCIAL);
    
    //  TODO:  Factor in the value of leverage- either dirt or a hostage.
    return Nums.clamp(talk / 10f, 0, 1);
  }
  
  
  protected String langDescription(PlanStep step) {
    return "Coerce "+step.give(Gives.COERCED);
  }
}









