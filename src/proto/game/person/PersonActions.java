

package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class PersonActions {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    STATE_INIT    = -1,
    STATE_AS_PC   =  0,
    STATE_UNAWARE =  1,
    STATE_ACTIVE  =  2,
    STATE_RETREAT =  3;
  
  
  final Person person;
  
  int   AIstate    = STATE_INIT;
  float confidence = 1.0f;
  float wariness   = 0.0f;
  
  int actionPoints;
  boolean turnDone;
  Object lastTarget;
  Action nextAction;
  
  
  PersonActions(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    AIstate      = s.loadInt   ();
    confidence   = s.loadFloat ();
    wariness     = s.loadFloat ();
    actionPoints = s.loadInt   ();
    turnDone     = s.loadBool  ();
    lastTarget   = s.loadObject();
    nextAction   = (Action) s.loadObject();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt   (AIstate     );
    s.saveFloat (confidence  );
    s.saveFloat (wariness    );
    s.saveInt   (actionPoints);
    s.saveBool  (turnDone    );
    s.saveObject(lastTarget  );
    s.saveObject(nextAction  );
  }
  
  
  
  /**  Overall status queries-
    */
  public int maxAP() {
    return 4;
  }
  
  
  public int currentAP() {
    return actionPoints;
  }
  
  
  public float confidence() {
    return confidence;
  }
  
  
  public float wariness() {
    return wariness;
  }
  
  
  public boolean isDoing(int AIstate) {
    return this.AIstate == AIstate;
  }
  
  
  public boolean retreating() {
    return isDoing(STATE_RETREAT);
  }
  
  
  public boolean captive() {
    return false;
  }
  
  
  
  /**  Action economy and per-turn update calls-
    */
  public void assignAction(Action action) {
    this.nextAction = action;
    if (action.path().length > 0) action.setMoveRoll(Rand.avgNums(2));
    if (action.target != this   ) lastTarget = action.target;
    this.actionPoints -= action.used.costAP(action);
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
    return actionPoints >= 0 && person.health.conscious();
  }
  
  
  public boolean turnDone() {
    return ! canTakeAction();
  }
  
  
  public boolean onTurnStart() {
    actionPoints = (int) maxAP();
    nextAction   = null;
    turnDone     = false;
    person.stats.updateStats();
    assessConfidence();
    return true;
  }
  
  
  public boolean onTurnEnd() {
    if (nextAction == null || ! nextAction.used.delayed()) {
      nextAction = null;
    }
    turnDone = true;
    //
    //  TODO:  Apply any conditions with stat effects, et cetera!
    return true;
  }
  
  
  public boolean updateDuringTurn() {
    Scene scene = person.currentScene();
    Action a = nextAction;
    if (scene == null || a == null) return false;
    
    int elapsed = a.timeElapsed();
    float moveRate = 4;
    float timeSteps = elapsed * moveRate / RunGame.FRAME_RATE;
    
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
    if (person.currentTile() != oldLoc) scene.updateFog();
    
    if (timeSteps > path.length) {
      if (! a.started()) {
        a.used.applyOnActionStart(a);
        a.used.checkForTriggers(a, true, false);
      }
      float extraTime = a.used.animDuration();
      a.setProgress((timeSteps - path.length) / (extraTime * moveRate));
    }
    if (a.complete()) {
      a.used.checkForTriggers(a, false, true);
      a.used.applyOnActionEnd(a);
      nextAction = null;
    }
    return true;
  }
  
  
  public Action selectAIAction() {
    //  TODO:  FILL THIS IN!
    return null;
  }
  
  
  
  /**  Confidence and power assessment-
    */
  private void assessConfidence() {
    Scene scene = person.currentScene();
    float teamHealth = 0, teamPower = 0, enemySight = 0;
    
    I.say("Assessing confidence for "+person);
    
    for (Person p : scene.persons()) {
      if (p.isAlly(person)) {
        teamPower  += p.actions.powerLevel();
        teamHealth += p.actions.powerLevel() * p.health.healthLevel();
      }
      else if (p.isEnemy(person) && hasSight(p.currentTile())) {
        enemySight++;
        if (canNotice(p)) enemySight++;
      }
    }
    
    //  TODO:  Refine these, and use constants to define the math.
    
    float courage = 0.2f, minAlert = (
      person.stats.levelFor(REFLEX  ) +
      person.stats.levelFor(STRENGTH)
    ) / 100f;
    if (enemySight > 0) {
      wariness += enemySight / 4f;
    }
    else {
      wariness -= 0.25f;
    }
    wariness = Nums.clamp(wariness, minAlert, 1);
    
    if (person.isHero    ()) courage = 1.5f;
    if (person.isCriminal()) courage = 0.5f;
    
    if (teamPower <= 0) {
      confidence = 0;
    }
    else {
      confidence = teamHealth / teamPower;
      confidence = (confidence + person.health.healthLevel()) / 2;
      if (! retreating()) confidence += courage;
      
      I.say("Confidence for "+this+": "+confidence);
    }
  }
  
  
  private float powerLevel() {
    //  TODO:  Refine this!
    if (person.isHero    ()) return 4;
    if (person.isCriminal()) return 1;
    return 0;
  }
  
  
  
  /**  Sight and stealth methods-
    */
  public float sightRange() {
    return (person.stats.levelFor(SURVEILLANCE) / 2.5f) + 2;
  }
  
  
  public float hidingRange() {
    return 0 + (person.stats.levelFor(STEALTH) / 4f);
  }
  
  
  public boolean hasSight(Tile point) {
    Scene scene = person.currentScene();
    if (scene == null) return false;
    return scene.fogAt(point, person.side()) > 0;
  }
  
  
  public boolean canNotice(Object point) {
    Scene scene = person.currentScene();
    if (scene == null) return false;
    
    Tile  under      = scene.tileUnder(point);
    float visibility = scene.fogAt(under, person.side());
    if (visibility <= 0) return false;
    
    if (point instanceof Person) {
      Person other = (Person) point;
      if (other.isAlly(person)) return true;
      
      float   sighting = Nums.max(1, sightRange());
      float   stealth  = other.actions.hidingRange();
      Action  action   = other.actions.nextAction();
      boolean focused  = lastTarget == other;
      
      sighting *= wariness + (focused ? 0.5f : 0);
      stealth  *= action == null ? 0.5f : action.moveRoll();
      stealth  /= sighting;
      if (stealth > visibility) return false;
    }
    
    return true;
  }
  
  
  
}




