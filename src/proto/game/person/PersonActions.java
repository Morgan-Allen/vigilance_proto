

package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;
import proto.game.person.PersonStats.Condition;



public class PersonActions {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final Person person;
  
  int actionPoints;
  boolean turnDone;
  
  Action nextAction;
  Object lastTarget;
  float turnMoveRoll;
  
  int timePoints;
  boolean timeDone;
  
  
  PersonActions(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    actionPoints = s.loadInt   ();
    turnDone     = s.loadBool  ();
    lastTarget   = s.loadObject();
    nextAction   = (Action) s.loadObject();
    turnMoveRoll = s.loadFloat();
    timePoints   = s.loadInt();
    timeDone     = s.loadBool();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt   (actionPoints);
    s.saveBool  (turnDone    );
    s.saveObject(lastTarget  );
    s.saveObject(nextAction  );
    s.saveFloat (turnMoveRoll);
    s.saveInt   (timePoints  );
    s.saveBool  (timeDone    );
  }
  
  
  
  /**  Overall status queries-
    */
  public int currentAP() {
    return actionPoints;
  }
  
  
  public Series <Ability> listAbilities() {
    List <Ability> all = new List();
    all.add(Common.MOVE);
    if (person.gear.weaponType().melee()) {
      all.add(Common.STRIKE);
      all.add(Common.GUARD );
    }
    if (person.gear.weaponType().ranged()) {
      all.add(Common.FIRE     );
      all.add(Common.OVERWATCH);
    }
    all.add(Common.SWITCH);
    all.add(Common.HIDE  );
    
    for (Ability a : person.stats.abilities) {
      if (! a.allowsUse(person)) continue;
      all.add(a);
    }
    for (Item i : person.gear.equipped()) if (i.charges > 0) {
      for (Ability a : i.kind().abilities) {
        if (! a.allowsUse(person)) continue;
        all.include(a);
      }
    }
    for (Condition c : person.stats.conditions) for (Ability a : all) {
      if (! c.basis.conditionAllowsAbility(a)) {
        all.remove(a);
        break;
      }
    }
    return all;
  }
  
  
  
  /**  Action economy and per-turn update calls-
    */
  public void assignAction(Action action) {
    this.nextAction = action;
    if (action == null) return;
    lastTarget = action.target;
    action.used.applyOnActionAssigned(action);
    this.actionPoints -= action.used.costAP(action);
    if (action.used.delayed()) onTurnEnd();
  }
  
  
  public void cancelAction() {
    assignAction(null);
  }
  
  
  public void modifyAP(int modifier) {
    actionPoints += modifier;
  }
  
  
  public void setActionPoints(int AP) {
    actionPoints = AP;
  }
  
  
  public Action nextAction() {
    return nextAction;
  }
  
  
  public boolean canTakeAction() {
    if (person.currentScene() == null || person.isCaptive()) return false;
    if (nextAction != null && nextAction.used.delayed()) return false;
    return person.health.conscious() && actionPoints > 0 && ! turnDone;
  }
  
  
  public boolean turnDone() {
    return ! canTakeAction();
  }
  
  
  boolean onTurnStart() {
    I.say("  "+person+" started action turn...");
    
    actionPoints = person.stats.maxActionPoints();
    nextAction   = null;
    turnDone     = false;
    turnMoveRoll = Rand.avgNums(2);
    person.stats.updateStats(0);
    person.mind.assessConfidence();
    return true;
  }
  
  
  boolean onTurnEnd() {
    if (nextAction == null || ! nextAction.used.delayed()) {
      nextAction = null;
    }
    turnDone = true;
    //
    //  TODO:  Apply any conditions with stat effects, et cetera!
    return true;
  }
  
  
  boolean updateDuringTurn(Action a) {
    Scene scene = person.currentScene();
    if (scene == null || a == null) return false;
    
    float timeSteps = a.timeSteps();
    float alpha = timeSteps % 1;
    Tile path[] = a.path();
    Tile l = path[Nums.clamp((int)  timeSteps     , path.length)];
    Tile n = path[Nums.clamp((int) (timeSteps + 1), path.length)];
    
    Tile oldLoc = person.currentTile();
    person.setExactPosition(
      scene,
      (alpha * n.x) + ((1 - alpha) * l.x),
      (alpha * n.y) + ((1 - alpha) * l.y),
      0
    );
    if (person.currentTile() != oldLoc) {
      scene.vision.updateFog();
      scene.vision.checkForEnemySightEntry(person, nextAction);
      
      for (Element e : person.currentTile().inside()) if (e.isProp()) {
        Prop p = (Prop) e;
        p.kind().onPersonEntry(person, scene, p);
      }
    }
    
    if (timeSteps > path.length) {
      if (! a.started()) {
        I.say("Started action: "+a);
        a.used.applyOnActionStart(a);
        a.used.applyTriggerEffects(a, true, false, false);
      }
      float extraTime = a.used.animDuration();
      a.setProgress((timeSteps - path.length) / (extraTime * a.moveRate()));
    }
    if (a.complete()) {
      a.used.applyTriggerEffects(a, false, true, false);
      a.used.applyOnActionEnd(a);
      I.say("Ended action: "+a);
    }
    return true;
  }
  
  
  
  /**  Sight and stealth methods-
    */
  public boolean hasSight(Tile point) {
    Scene scene = person.currentScene();
    if (scene == null) return false;
    return scene.vision.fogAt(point, person.side()) > 0;
  }
  
  
  public boolean checkToNotice(Object point) {
    Scene scene = person.currentScene();
    if (scene == null) return false;
    boolean report = false;
    
    Tile  under      = scene.tileUnder(point);
    float visibility = scene.vision.fogAt(under, person.side());
    if (visibility <= 0) return false;
    
    if (point instanceof Person) {
      Person other = (Person) point;
      if (other.isAlly(person)) return true;
      
      boolean focused = lastTarget == other;
      float sighting  = Nums.max(1, person.stats.sightRange());
      float aware     = person.mind.wariness() + (focused ? 0.5f : 0);
      float stealth   = other.stats.hidingRange();
      float moveRoll  = other.actions.turnMoveRoll * 2;
      float hideLevel = (stealth * moveRoll) / (sighting * aware);
      
      if (report) {
        I.say("\n"+person+" checking to notice "+other);
        I.say("  Sighting: "+sighting+" Stealth:  "+stealth);
        I.say("  Awareness: "+aware+" Move Roll: "+moveRoll);
        I.say("  Visibility: "+visibility+" vs. Hide Level: "+hideLevel);
        I.say("  Will notice: "+(visibility >= hideLevel)+"\n");
      }
      
      if (hideLevel > visibility) return false;
    }
    
    return true;
  }
  
  
  public void onNoticing(Person seen, Action doing) {
    if (nextAction == null || ! nextAction.used.triggerOnNotice()) return;
    if (! nextAction.used.triggerOnNoticing(person, seen, doing) ) return;
    nextAction.used.applyOnNoticing(person, seen, doing);
  }
  
  
  
  /**  Time points on the strategic map-
    */
  public int currentTP() {
    return timePoints;
  }
  
  
  public void modifyTP(int modifier) {
    timePoints += modifier;
  }
  
  
  public void setTimePoints(int TP) {
    timePoints = TP;
  }
  
}










