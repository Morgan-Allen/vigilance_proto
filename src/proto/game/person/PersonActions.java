

package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class PersonActions {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final Person person;
  
  int actionPoints;
  boolean turnDone;
  Object lastTarget;
  Action nextAction;
  
  
  PersonActions(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    actionPoints = s.loadInt   ();
    turnDone     = s.loadBool  ();
    lastTarget   = s.loadObject();
    nextAction   = (Action) s.loadObject();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt   (actionPoints);
    s.saveBool  (turnDone    );
    s.saveObject(lastTarget  );
    s.saveObject(nextAction  );
  }
  
  
  
  /**  Overall status queries-
    */
  public int currentAP() {
    return actionPoints;
  }
  
  
  public boolean captive() {
    //  TODO:  Add some nuance here!
    return person.isCivilian();// && side == Side.VILLAINS;
  }
  
  
  
  /**  Action economy and per-turn update calls-
    */
  public void assignAction(Action action) {
    this.nextAction = action;
    if (action == null          ) return;
    if (action.path().length > 0) action.setMoveRoll(Rand.avgNums(2));
    if (action.target != this   ) lastTarget = action.target;
    action.used.applyOnActionAssigned(action);
    this.actionPoints -= action.used.costAP(action);
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
    if (person.currentScene() == null) return false;
    if (nextAction != null && nextAction.used.delayed()) return false;
    return person.health.conscious() && actionPoints > 0 && ! turnDone;
  }
  
  
  public boolean turnDone() {
    return ! canTakeAction();
  }
  
  
  boolean onTurnStart() {
    actionPoints = person.stats.maxActionPoints();
    nextAction   = null;
    turnDone     = false;
    person.stats.updateStats();
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
    }
    
    if (timeSteps > path.length) {
      if (! a.started()) {
        a.used.applyOnActionStart(a);
        a.used.checkForTriggers(a, true, false);
      }
      float extraTime = a.used.animDuration();
      a.setProgress((timeSteps - path.length) / (extraTime * a.moveRate()));
    }
    if (a.complete()) {
      a.used.checkForTriggers(a, false, true);
      a.used.applyOnActionEnd(a);
      nextAction = null;
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
  
  
  public boolean canNotice(Object point) {
    Scene scene = person.currentScene();
    if (scene == null) return false;
    
    Tile  under      = scene.tileUnder(point);
    float visibility = scene.vision.fogAt(under, person.side());
    if (visibility <= 0) return false;
    
    if (point instanceof Person) {
      Person other = (Person) point;
      if (other.isAlly(person)) return true;
      
      float   sighting = Nums.max(1, person.stats.sightRange());
      float   stealth  = other.stats.hidingRange();
      Action  action   = other.actions.nextAction();
      boolean focused  = lastTarget == other;
      
      sighting *= person.mind.wariness() + (focused ? 0.5f : 0);
      stealth  *= action == null ? 0.5f : action.moveRoll();
      stealth  /= sighting;
      if (stealth > visibility) return false;
    }
    
    return true;
  }
  
  
  public void onNoticing(Person seen, Action doing) {
    if (nextAction == null || ! nextAction.used.triggerOnNotice()) return;
    if (! nextAction.used.triggerOnNoticing(person, seen, doing) ) return;
    nextAction.used.applyOnNoticing(person, seen, doing);
  }
  
}







