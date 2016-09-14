

package proto.game.plans;
import proto.util.*;


public class TypeMake extends StepType {
  
  
  
  TypeMake() { super(
    "Make",
    rolesFor("making"),
    rolesFor("makes"),
    rolesFor("made")
  ); }
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_ITEM) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  float calcSuccessChance(PlanStep action) {
    Thing makes = action.needs[roleID("makes")];
    Thing made  = action.gives[roleID("made" )];
    
    float skill = makes.statValue(Thing.STAT_WIRING);
    skill -= made.statValue(Thing.STAT_MAKE_DC);
    return Nums.clamp(skill / 10f, 0, 1);
  }
  
}