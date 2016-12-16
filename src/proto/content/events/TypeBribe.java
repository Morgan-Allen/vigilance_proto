

package proto.content.events;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.util.Nums;



public class TypeBribe extends StepType {
  
  
  static enum Needs {
    BRIBER
  };
  static enum Gives {
    BRIBED
  };
  
  TypeBribe() {
    super("Bribe", "step_type_bribe", null, Needs.values(), Gives.values());
  }
  
  
  public PlanStep toProvide(Element needed, PlanStep by) {
    if (needed.type == Kind.TYPE_PERSON) {
      final Person bribed = (Person) needed;
      if (bribed.isHero()) return null;
      return new PlanStep(this, by.plan).setGive(Gives.BRIBED, bribed);
    }
    return null;
  }
  
  
  protected float calcFitness(Element used, Object needType, PlanStep step) {
    if (needType == Needs.BRIBER) {
      if (used.type != Kind.TYPE_PERSON) return 0;
      final Person briber = (Person) used;
      if (briber.base() != step.plan.agent.base()) return 0;
      return briber.stats.levelFor(PersonStats.SUASION) / 10f;
    }
    return 0;
  }
  
  
  protected float baseSuccessChance(PlanStep step) {
    //  TODO:  Incorporate how much the actor likes/dislikes the idea of the
    //  plan they're contributing to!
    Person briber = (Person) step.need(Needs.BRIBER);
    Person bribed = (Person) step.give(Gives.BRIBED);
    
    float skill = 0;
    skill += briber.stats.levelFor(PersonStats.SUASION );
    skill -= bribed.stats.levelFor(PersonStats.QUESTION);
    return Nums.clamp(skill / 5f, 0, 1);
  }
  
  
  protected float baseAppeal(PlanStep step) {
    float cashCost = 10f, reserves = step.plan.agent.base().currentFunds();
    if (cashCost > reserves) return -100;
    return -10 * cashCost / reserves;
  }




  /**  Rendering, debug and interface methods-
    */
  protected String langDescription(PlanStep step) {
    return "bribing "+step.give(Gives.BRIBED);
  }
}








