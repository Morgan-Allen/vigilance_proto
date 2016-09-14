

package proto.game.plans;
import proto.util.*;



public abstract class StepType {
  
  
  /**  Data fields, construction and state restoration-
    */
  final String name;
  
  //  TODO:  You'll need to get rid of this in favour of arbitrary object
  //  keys.
  //*
  static class Role {
    final String name;
    final int ID;
    Role(String name, int ID) { this.name = name; this.ID = ID; }
  }
  //*/
  
  final Role stages[], needs[], gives[];
  
  
  StepType(String name, Role stages[], Role needs[], Role gives[]) {
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
  
  
  
  /**  Evaluating suitability of nominees for various roles, plus 
    */
  PlanStep toProvide(Thing needed, PlanStep by) {
    return null;
  }
  
  
  float calcSuitability(Thing used, Role role, PlanStep step) {
    return 0;
  }
  
  
  float baseSuccessChance(PlanStep step) {
    return 1;
  }
  
  
  float baseAppeal(PlanStep step) {
    return 0;
  }
}




