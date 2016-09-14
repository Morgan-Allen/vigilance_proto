

package proto.game.plans;
import proto.util.*;



public class TypeCoerce extends StepType {
  
  TypeCoerce() { super(
    "Coerce",
    rolesFor("talking", "under coercion"),
    rolesFor(),
    rolesFor("coerced")
  ); }
  
  
  
  PlanStep toProvide(Thing needed, PlanStep by) {
    if (needed.type == Thing.TYPE_PERSON) {
      return new PlanStep(this, by.plan).bindGives(needed);
    }
    return null;
  }
  
  
  float calcSuccessChance(PlanStep action) {
    Thing coerced = action.gives[roleID("coerced")];
    float talk = action.plan.agent.statValue(Thing.STAT_CHARM);
    talk += 5 - coerced.statValue(Thing.STAT_CHARM);
    return Nums.clamp(talk / 10f, 0, 1);
  }
  
}