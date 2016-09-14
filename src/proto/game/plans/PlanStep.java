
package proto.game.plans;
import proto.util.*;
import static proto.game.plans.StepType.*;



public class PlanStep {
  
  final private StepType type;
  int uniqueID;
  
  Plan plan;
  int stageID;
  Thing needs[], gives[];
  float rating;
  
  PlanStep needSteps[];
  PlanStep parent;
  int parentNeedID;
  
  
  PlanStep(StepType type, Plan plan) {
    this.type = type;
    this.plan = plan;
    this.gives     = new Thing   [type.gives.length];
    this.needs     = new Thing   [type.needs.length];
    this.needSteps = new PlanStep[type.needs.length];
  }
  
  
  PlanStep bindGives(Thing... gives) {
    for (int g = gives.length; g-- > 0;) this.gives[g] = gives[g];
    return this;
  }
  
  
  PlanStep bindNeeds(Thing... needs) {
    for (int n = needs.length; n-- > 0;) this.needs[n] = needs[n];
    return this;
  }
  
  
  PlanStep bindParent(PlanStep parent, Role need) {
    this.parent       = parent ;
    this.parentNeedID = need.ID;
    return this;
  }
  
  
  Role[] needsRoles() {
    return type.needs;
  }
  
  
  Role[] givesRoles() {
    return type.gives;
  }
  
  
  Thing[] needs() {
    return needs;
  }
  
  
  Thing[] gives() {
    return gives;
  }
  
  
  boolean doesGive(Thing needs) {
    return Visit.arrayIncludes(gives, needs);
  }
  
  
  boolean doesNeed(Thing gives) {
    return Visit.arrayIncludes(needs, gives);
  }
  
  
  float baseSuccessChance() {
    return type.baseSuccessChance(this);
  }
  
  
  float baseAppeal() {
    return type.baseSuccessChance(this);
  }
  
  
  float calcSuitability(Thing used, Role role) {
    return type.calcSuitability(used, role, this);
  }
  
  
  
  /**  Utility methods for action-execution-
    */
  float calcSuccessChance() {
    float chance = 1.0f;
    for (int r = needs.length; r-- > 0;) {
      PlanStep getStep = needSteps[r];
      if (getStep != null) chance *= getStep.calcSuccessChance();
    }
    chance *= baseSuccessChance();
    return chance;
  }
  
  
  PlanStep[] actionsToObtain(Thing used, Role role) {
    final Batch <PlanStep> actions = new Batch();
    for (StepType type : StepTypes.ALL_TYPES) {
      PlanStep provides = type.toProvide(used, this);
      if (provides != null) actions.add(provides);
    }
    return actions.toArray(PlanStep.class);
  }
  
  
  boolean activeDuring(PlanStep other) {
    if (other == this) return true;
    return false;
  }
  
  
  
  /**  Rendering, debug and feedback methods-
    */
  public String toString() {
    return type.name+" #"+uniqueID;
  }
  
  
  public String longDescription() {
    final StringBuffer s = new StringBuffer();
    s.append("\nStep ID: "+this);
    s.append("\n  Parent: "+parent);
    s.append("\n  Chance: "+calcSuccessChance());
    s.append("\n  Needs:");
    for (int r = type.needs.length; r-- > 0;) {
      s.append("\n    "+type.needs[r].name+"- "+needs[r]);
      if (needSteps[r] != null) s.append(" ("+needSteps[r]+")");
    }
    s.append("\n  Gives:");
    for (int r = type.gives.length; r-- > 0;) {
      s.append("\n    "+type.gives[r].name+"- "+gives[r]);
    }
    s.append("\n");
    return s.toString();
  }
}








