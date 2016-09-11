

package proto.game.plans;
import proto.util.*;



public abstract class ActionType {
  
  
  /**  Data fields, construction and state restoration-
    */
  final String name;
  
  static class Role {
    final String name;
    final int ID;
    Role(String name, int ID) { this.name = name; this.ID = ID; }
  }
  
  final Role stages[], needs[], gives[];
  
  
  ActionType(String name, Role stages[], Role needs[], Role gives[]) {
    this.name   = name  ;
    this.stages = stages;
    this.needs  = needs ;
    this.gives  = gives ;
  }
  
  
  static Role[] rolesFor(String... roleNames) {
    final Role roles[] = new Role[roleNames.length];
    for (int i = roleNames.length; i-- > 0;) {
      roles[i] = new Role(roleNames[i], i);
    }
    return roles;
  }
  
  
  int roleID(String roleName) {
    for (Role s : stages) if (s.name.equals(roleName)) return s.ID;
    for (Role n : needs ) if (n.name.equals(roleName)) return n.ID;
    for (Role g : gives ) if (g.name.equals(roleName)) return g.ID;
    return -1;
  }
  
  
  
  /**  Evaluating suitability of nominees for various roles
    */
  float calcSuitability(Thing used, Role role, Action action) {
    return 0;
  }
  
  
  Action[] actionsToObtain(Thing used, Role role, Action action) {
    final Batch <Action> actions = new Batch();
    
    for (ActionType type : ActionTypes.ALL_TYPES) {
      Action provides = type.toProvide(used, action);
      if (provides != null) actions.add(provides);
    }
    
    return actions.toArray(Action.class);
  }
  
  
  Action toProvide(Thing needed, Action by) {
    return null;
  }
  
  
  boolean provides(Action action, Thing needed) {
    if (action.parent == null) return false;
    return action.parent.needs[action.parentNeedID] == needed;
  }
  
  
  boolean activeDuring(Action action, Action other) {
    if (other == action) return true;
    return false;
  }
  
  
  
  /**  Rating and success-chance:
    */
  float calcSuccessChance(Action action) {
    float chance = 1.0f;
    for (int r = needs.length; r-- > 0;) {
      Action getStep = action.needSteps[r];
      if (getStep != null) chance *= getStep.type.calcSuccessChance(getStep);
    }
    return chance;
  }
  
  
  float baseAppeal(Action action, Plan plan) {
    return 0;
  }
  
  
  
  /**  Leaving evidence after the fact-
    */
  Clue[] possibleClues(Action action, Object perp) {
    return new Clue[0];
  }
}







