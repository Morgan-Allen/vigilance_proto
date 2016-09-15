

package proto.game.plans;
import proto.util.*;



public class PlanStep {
  
  
  final StepType type;
  int uniqueID;
  
  Plan plan;
  int stageID;
  private Thing needs[], gives[];
  float rating;
  
  private PlanStep needSteps[];
  PlanStep parent;
  Object parentNeedType;
  
  
  PlanStep(StepType type, Plan plan) {
    this.type = type;
    this.plan = plan;
    final Object needT[] = needTypes(), giveT[] = giveTypes();
    this.gives     = new Thing   [giveT.length];
    this.needs     = new Thing   [needT.length];
    this.needSteps = new PlanStep[needT.length];
  }
  
  
  PlanStep bindGives(Thing... gives) {
    for (int g = gives.length; g-- > 0;) this.gives[g] = gives[g];
    return this;
  }
  
  
  PlanStep bindNeeds(Thing... needs) {
    for (int n = needs.length; n-- > 0;) this.needs[n] = needs[n];
    return this;
  }
  
  
  PlanStep bindParent(PlanStep parent, Object needType) {
    this.parent         = parent  ;
    this.parentNeedType = needType;
    return this;
  }
  
  
  Object[] needTypes() {
    return type.needTypes;
  }
  
  
  Object[] giveTypes() {
    return type.giveTypes;
  }
  
  
  Thing[] needs() {
    return needs;
  }
  
  
  Thing[] gives() {
    return gives;
  }
  
  
  void setNeed(Object needType, Thing value) {
    needs[Visit.indexOf(needType, needTypes())] = value;
  }
  
  
  void setGive(Object giveType, Thing value) {
    gives[Visit.indexOf(giveType, giveTypes())] = value;
  }
  
  
  Thing need(Object needType) {
    return needs[Visit.indexOf(needType, needTypes())];
  }
  
  
  Thing give(Object giveType) {
    return gives[Visit.indexOf(giveType, giveTypes())];
  }
  
  
  PlanStep stepForNeed(Object needType) {
    return needSteps[Visit.indexOf(needType, needTypes())];
  }
  
  
  void setStepForNeed(Object needType, PlanStep step) {
    needSteps[Visit.indexOf(needType, needTypes())] = step;
  }
  
  
  boolean doesGive(Thing needs) {
    return Visit.arrayIncludes(gives, needs);
  }
  
  
  boolean doesNeed(Thing gives) {
    return Visit.arrayIncludes(needs, gives);
  }
  
  
  
  /**
    */
  float baseSuccessChance() {
    return type.baseSuccessChance(this);
  }
  
  
  float baseAppeal() {
    return type.baseSuccessChance(this);
  }
  
  
  float calcSuitability(Thing used, Object needType) {
    return type.calcSuitability(used, needType, this);
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
  
  
  PlanStep[] actionsToObtain(Thing used, Object needType) {
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
    
    final Object needR[] = needTypes(), giveR[] = giveTypes();
    
    s.append("\n  Needs:");
    for (int n = 0; n < needR.length; n++) {
      s.append("\n    "+needR[n]+"- "+needs[n]);
      if (needSteps[n] != null) s.append(" ("+needSteps[n]+")");
    }
    s.append("\n  Gives:");
    for (int g = 0; g < giveR.length; g++) {
      s.append("\n    "+giveR[g]+"- "+gives[g]);
    }
    
    s.append("\n");
    return s.toString();
  }
}












