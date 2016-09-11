
package proto.game.plans;
import proto.util.*;
import static proto.game.plans.ActionType.*;



public class Action {
  
  ActionType type;
  int uniqueID;
  
  Plan plan;
  int stageID;
  Thing needs[], gives[];
  float rating;
  
  Action needSteps[];
  Action parent;
  int parentNeedID;
  
  
  Action(ActionType type, Plan plan) {
    this.type = type;
    this.plan = plan;
    this.gives     = new Thing [type.gives.length];
    this.needs     = new Thing [type.needs.length];
    this.needSteps = new Action[type.needs.length];
  }
  
  
  Action bindGives(Thing... gives) {
    for (int g = gives.length; g-- > 0;) this.gives[g] = gives[g];
    return this;
  }
  
  
  Action bindNeeds(Thing... needs) {
    for (int n = needs.length; n-- > 0;) this.needs[n] = needs[n];
    return this;
  }
  
  
  Action bindParent(Action parent, Role need) {
    this.parent       = parent ;
    this.parentNeedID = need.ID;
    return this;
  }
  
  
  
  /**  Utility methods for action-execution-
    */
  
  
  
  
  /**  Rendering, debug and feedback methods-
    */
  public String toString() {
    return type.name+" #"+uniqueID;
  }
  
  
  public String longDescription() {
    final StringBuffer s = new StringBuffer();
    s.append("\nStep ID: "+this);
    s.append("\n  Parent: "+parent);
    s.append("\n  Chance: "+type.calcSuccessChance(this));
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








