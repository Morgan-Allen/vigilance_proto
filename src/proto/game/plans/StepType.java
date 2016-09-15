

package proto.game.plans;
import proto.util.*;



public abstract class StepType {
  
  
  /**  Data fields, construction and state restoration-
    */
  //  TODO:  You'll need to get rid of this in favour of arbitrary object
  //  keys.  e.g:
  /*
  StepType sample = new StepType(
    "sample",
    STAGE_A, STAGE,
    STAGE_B, STAGE,
    NEED_A , NEED ,
    NEED_B , NEED ,
    GIVE_A , GIVE ,
    GIVE_B , GIVE 
  );
  
  followed by-
  
  PlanStep step = new PlanStep(sample, plan);
  step.bindRoles(
    NEED_A, bob,
    GIVE_A, platinum_dollars,
    GIVE_B, metal_shavings
  );
  
  //*/
  //*
  static class Role {
    final String name;
    final int ID;
    Role(String name, int ID) { this.name = name; this.ID = ID; }
    public String toString() { return name; }
  }
  //*/
  
  
  final String name;
  
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
  Series <Thing> availableTargets(Role role, Thing world) {
    return world.inside;
  }
  
  
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




