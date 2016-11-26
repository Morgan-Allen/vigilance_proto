

package proto.game.plans;
import proto.game.world.*;
import proto.util.*;



public class PlanStep {
  
  
  final public StepType type;
  int uniqueID = -1;
  
  final public Plan plan;
  private Element needs[], gives[];
  private float rating;
  
  private PlanStep needSteps[];
  PlanStep parent;
  Object parentNeedType;
  
  
  public PlanStep(StepType type, Plan plan) {
    this.type = type;
    this.plan = plan;
    final Object needT[] = needTypes(), giveT[] = giveTypes();
    this.gives     = new Element   [giveT.length];
    this.needs     = new Element   [needT.length];
    this.needSteps = new PlanStep[needT.length];
  }
  
  
  public PlanStep setGives(Element... gives) {
    for (int g = gives.length; g-- > 0;) this.gives[g] = gives[g];
    return this;
  }
  
  
  public PlanStep setNeeds(Element... needs) {
    for (int n = needs.length; n-- > 0;) this.needs[n] = needs[n];
    return this;
  }
  
  
  public PlanStep setParent(PlanStep parent, Object needType) {
    this.parent         = parent  ;
    this.parentNeedType = needType;
    return this;
  }
  
  
  public Object givesToParent() {
    if (parent == null) return null;
    return parent.need(parentNeedType);
  }
  
  
  public Object[] needTypes() {
    return type.needTypes;
  }
  
  
  public Object[] giveTypes() {
    return type.giveTypes;
  }
  
  
  public Element[] needs() {
    return needs;
  }
  
  
  public Element[] gives() {
    return gives;
  }
  
  
  public void setNeed(Object needType, Element value) {
    needs[Visit.indexOf(needType, needTypes())] = value;
  }
  
  
  public void setGive(Object giveType, Element value) {
    gives[Visit.indexOf(giveType, giveTypes())] = value;
  }
  
  
  public Element need(Object needType) {
    return needs[Visit.indexOf(needType, needTypes())];
  }
  
  
  public Element give(Object giveType) {
    return gives[Visit.indexOf(giveType, giveTypes())];
  }
  
  
  public PlanStep stepForNeed(Object needType) {
    return needSteps[Visit.indexOf(needType, needTypes())];
  }
  
  
  public void setStepForNeed(Object needType, PlanStep step) {
    needSteps[Visit.indexOf(needType, needTypes())] = step;
  }
  
  
  public boolean doesGive(Element needs) {
    return Visit.arrayIncludes(gives, needs);
  }
  
  
  public boolean doesNeed(Element gives) {
    return Visit.arrayIncludes(needs, gives);
  }
  
  
  
  /**
    */
  float baseSuccessChance() {
    return type.baseSuccessChance(this);
  }
  
  
  float baseAppeal() {
    return type.baseAppeal(this);
  }
  
  
  float baseFailRisk() {
    return type.baseFailRisk(this);
  }
  
  
  float calcSuitability(Element used, Object needType) {
    return type.calcSuitability(used, needType, this);
  }
  
  
  
  /**  Utility methods for action-execution-
    */
  void assignRating(float rating) {
    this.rating = rating;
  }
  
  
  void calcStepRatingFromParent(PlanStep parent) {
    float chance = calcSuccessChance();
    rating = chance * parent.rating;
    rating += chance * baseAppeal();
    rating -= (1 - chance) * baseFailRisk();
  }
  
  
  float calcSuccessChance() {
    float chance = 1.0f;
    
    //  TODO:  You need to have a better method for modifying this estimate
    //  when essential pre-reqs aren't yet met (while still giving a decent
    //  rating for the raw step until expanded.)
    
    for (int r = needs.length; r-- > 0;) {
      PlanStep getStep = needSteps[r];
      if (getStep != null) chance *= getStep.calcSuccessChance();
    }
    
    chance *= baseSuccessChance();
    return chance;
  }
  
  
  PlanStep[] actionsToObtain(Element used, Object needType) {
    final Batch <PlanStep> actions = new Batch();
    for (StepType type : plan.stepTypes) {
      PlanStep provides = type.toProvide(used, this);
      if (provides != null) actions.add(provides);
    }
    return actions.toArray(PlanStep.class);
  }
  
  
  boolean activeDuring(PlanStep other) {
    return type.activeDuring(this, other);
  }
  
  
  public float rating() {
    return rating;
  }
  
  
  
  /**  Rendering, debug and feedback methods-
    */
  public String toString() {
    return langDescription();
    //return type.name+" #"+uniqueID;
  }
  
  
  public String langDescription() {
    String desc = "";
    if (uniqueID != -1) desc+="#"+uniqueID+": ";
    desc+=type.langDescription(this);
    if (parent != null) desc+=" ("+parentNeedType+" for #"+parent.uniqueID+")";
    
    String needs = "";
    for (Object type : needTypes()) {
      if (need(type) != null) continue;
      needs+="["+type+"]";
    }
    if (needs.length() > 0) desc+=" (Need "+needs+")";
    
    return desc;
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





