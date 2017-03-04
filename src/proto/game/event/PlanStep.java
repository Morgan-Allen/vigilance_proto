

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



/*
public class PlanStep implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  /*
  final public StepType type;
  final public Plan plan;
  
  PlanStep parent;
  int parentNeedID;
  private Element needs[], gives[];
  private PlanStep needSteps[];
  
  int uniqueID = -1;
  private float rating;
  private Event event;
  
  
  public PlanStep(StepType type, Plan plan) {
    this.type = type;
    this.plan = plan;
    final Object needT[] = needTypes(), giveT[] = giveTypes();
    this.gives     = new Element [giveT.length];
    this.needs     = new Element [needT.length];
    this.needSteps = new PlanStep[needT.length];
  }
  
  
  public PlanStep(Session s) throws Exception {
    s.cacheInstance(this);
    type = (StepType) s.loadObject();
    plan = (Plan) s.loadObject();
    
    parent = (PlanStep) s.loadObject();
    parentNeedID = s.loadInt();
    needs     = (Element []) s.loadObjectArray(Element.class);
    gives     = (Element []) s.loadObjectArray(Element.class);
    needSteps = (PlanStep[]) s.loadObjectArray(PlanStep.class);
    
    uniqueID  = s.loadInt();
    rating    = s.loadFloat();
    event     = (Event) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(type);
    s.saveObject(plan);
    
    s.saveObject(parent);
    s.saveInt(parentNeedID);
    s.saveObjectArray(needs);
    s.saveObjectArray(gives);
    s.saveObjectArray(needSteps);
    
    s.saveInt(uniqueID);
    s.saveFloat(rating);
    s.saveObject(event);
  }
  
  
  
  /**  Supplementary query/iteration methods-
    */
  /*
  public PlanStep setParent(PlanStep parent, Object needType) {
    this.parent       = parent;
    this.parentNeedID = Visit.indexOf(needType, parent.needTypes());
    return this;
  }
  
  
  public PlanStep parent() {
    return parent;
  }
  
  
  public Object roleForParent() {
    if (parent == null) return null;
    return parent.type.needTypes[parentNeedID];
  }
  
  
  public Element givesToParent() {
    if (parent == null) return null;
    return parent.needs()[parentNeedID];
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
  
  
  public PlanStep setNeed(Object needType, Element value, PlanStep gives) {
    final int index = Visit.indexOf(needType, needTypes());
    needs    [index] = value;
    needSteps[index] = gives;
    return this;
  }
  
  
  public PlanStep setGive(Object giveType, Element value) {
    gives[Visit.indexOf(giveType, giveTypes())] = value;
    return this;
  }
  
  
  public Element need(Object needType) {
    final int index = Visit.indexOf(needType, needTypes());
    return index == -1 ? null : needs[index];
  }
  
  
  public Element give(Object giveType) {
    final int index = Visit.indexOf(giveType, giveTypes());
    return index == -1 ? null : gives[index];
  }
  
  
  public PlanStep stepForNeed(Object needType) {
    return needSteps[Visit.indexOf(needType, needTypes())];
  }
  
  
  public void setStepForNeed(int needID, PlanStep step) {
    needSteps[needID] = step;
  }
  
  
  public boolean doesGive(Element needed) {
    return Visit.arrayIncludes(gives, needed);
  }
  
  
  public boolean doesNeed(Element given) {
    return Visit.arrayIncludes(needs, given);
  }
  
  
  
  /**
    */
  /*
  float baseSuccessChance() {
    return type.baseSuccessChance(this);
  }
  
  
  float baseAppeal() {
    return type.baseAppeal(this);
  }
  
  
  float baseFailCost() {
    return type.baseFailCost(this);
  }
  
  
  float calcFitness(Element used, Object needType) {
    return type.calcFitness(used, needType, this);
  }
  
  
  
  /**  Utility methods for plan-evaluation-
    */
  /*
  void assignRating(float rating) {
    this.rating = rating;
  }
  
  
  void calcRatingFromBase(float parentRating) {
    float chance = calcSuccessChance();
    float urgency = 0;
    if (parent != null) urgency = parent.type.urgency(roleForParent(), parent);
    rating = 0;
    rating += chance * (parentRating * Nums.clamp(urgency, 0, 1));
    rating += (chance * baseAppeal()) - ((1 - chance) * baseFailCost());
  }
  
  
  float calcSuccessChance() {
    //
    //  TODO:  You need to have a better method for modifying this estimate
    //  when essential pre-reqs aren't yet met (while still giving a decent
    //  rating for the raw step until expanded.)
    return type.baseSuccessChance(this);
    float chance = 1.0f;
    
    for (int r = needs.length; r-- > 0;) {
      PlanStep getStep = needSteps[r];
      if (getStep != null && getStep != this) {
        chance *= getStep.calcSuccessChance();
      }
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
  
  
  
  /**  Translating into concrete events:
    */
  /*
  public boolean currentlyPossible() {
    final Base base = plan.agent.base();
    for (Object typeN : needTypes()) {
      final Element need = need(typeN);
      if (type.urgency(typeN, this) < 1) continue;
      if (need == null) return false;
      if (plan.obtainedBefore(this, need)) continue;
      if (need == null || need.attachedTo() != base) return false;
    }
    return true;
  }
  
  
  public Event matchedEvent() {
    if (event != null) return event;
    event = new Event(type, plan.world);
    
    Place place = null;
    if (place == null) for (Element e : needs) if (e != null) {
      if ((place = e.place()) != null) break;
    }
    if (place == null) for (Element e : gives) if (e != null) {
      if ((place = e.place()) != null) break;
    }
    
    event.assignParameters(place, Task.TIME_SHORT);
    return event;
  }
  
  
  
  /**  Rendering, debug and feedback methods-
    */
  /*
  public String toString() {
    return langDescription();
    //return type.name+" #"+uniqueID;
  }
  
  
  public String langDescription() {
    String desc = "";
    if (uniqueID != -1) {
      desc+="#"+uniqueID+": ";
    }
    desc+=type.langDescription(this);
    
    if (parent != null) {
      Object role = parent.needTypes()[parentNeedID];
      desc+=" ("+role+" for #"+parent.uniqueID+")";
    }
    
    String needs = "";
    for (Object type : needTypes()) {
      if (this.type.urgency(type, this) <= 0) continue;
      Element needed = need(type);
      PlanStep getStep = stepForNeed(type);
      if (needed != null && getStep != null) continue;
      needs+="["+type+(needed == null ? "" : " = "+needed)+"]";
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
      if (type.urgency(needR[n], this) <= 0) continue;
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
//*/





