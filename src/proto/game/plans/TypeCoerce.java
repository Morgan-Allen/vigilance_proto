

package proto.game.plans;
import proto.util.*;



public class TypeCoerce extends ActionType {
  
  TypeCoerce() { super(
    "Coerce",
    rolesFor("coercing"),
    rolesFor(),
    rolesFor("coerced")
  ); }
  
  
  
  Action toProvide(Thing needed, Action by) {
    if (needed.type == Thing.TYPE_PERSON) {
      return new Action(this, by.plan).bindGives(needed);
    }
    return super.toProvide(needed, by);
  }
  
  
  float calcSuccessChance(Action action) {
    Thing coerced = action.gives[roleID("coerced")];
    float talk = action.plan.agent.statValue(Thing.STAT_CHARM);
    talk += 5 - coerced.statValue(Thing.STAT_CHARM);
    return Nums.max(0, talk / 10f);
  }
  
  
  
  
}