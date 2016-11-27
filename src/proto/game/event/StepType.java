

package proto.game.event;
import proto.game.world.*;
import proto.util.*;



public abstract class StepType extends EventType {
  
  
  /**  Data fields, construction and state restoration-
    */
  final Object needTypes[], giveTypes[];
  
  
  protected StepType(
    String name, String ID, Object needTypes[], Object giveTypes[]
  ) {
    super(name, ID);
    this.needTypes = needTypes;
    this.giveTypes = giveTypes;
  }
  
  
  static Object[] rolesFor(Object... roleTypes) {
    return roleTypes;
  }
  
  
  
  /**  Evaluating suitability of nominees for various roles, providing viable
    *  targets, generating the initial step, and evaluating the odds and appeal
    *  of such an action.
    */
  public PlanStep toProvide(Element needed, PlanStep by) {
    return null;
  }
  
  
  protected Series <Element> availableTargets(Object needType, World world) {
    return world.inside();
  }
  
  
  protected boolean activeDuring(PlanStep step, PlanStep other) {
    return step == other;
  }
  
  
  protected boolean isNeeded(Object needType, PlanStep step) {
    return true;
  }
  
  
  protected float calcSuitability(Element used, Object needType, PlanStep step) {
    return 0;
  }
  
  
  protected float baseSuccessChance(PlanStep step) {
    return 0.5f;
  }
  
  
  protected float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
  protected float baseFailRisk(PlanStep step) {
    return 0;
  }
  
  
  
  /**  Rendering and interface methods-
    */
  protected abstract String langDescription(PlanStep step);
  

  protected String nameFor(Event event) {
    PlanStep step = event.step;
    return langDescription(step);
  }
  
  
  protected String infoFor(Event event) {
    PlanStep step = event.step;
    return step.longDescription();
  }
}










