

package proto.game.plans;
import proto.util.*;



public abstract class StepType {
  
  
  /**  Data fields, construction and state restoration-
    */
  final String name;
  final Object stages[], needTypes[], giveTypes[];
  
  
  StepType(
    String name, Object stages[], Object needTypes[], Object giveTypes[]
  ) {
    this.name      = name     ;
    this.stages    = stages   ;
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
  PlanStep toProvide(Thing needed, PlanStep by) {
    return null;
  }
  

  Series <Thing> availableTargets(Object needType, Thing world) {
    return world.inside;
  }
  
  
  float calcSuitability(Thing used, Object needType, PlanStep step) {
    return 0;
  }
  
  
  float baseSuccessChance(PlanStep step) {
    return 1;
  }
  
  
  float baseAppeal(PlanStep step) {
    return 0;
  }
  
  
  float baseFailRisk(PlanStep step) {
    return 0;
  }
  
  
  
  /**  Other utility/helper methods-
    */
  float rateStatLevel(Thing used, String stat) {
    if (used.type != Thing.TYPE_PERSON) return 0;
    return Nums.clamp(used.statValue(stat) / 10f, 0, 1);
  }
}








