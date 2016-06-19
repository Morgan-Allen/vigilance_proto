

package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class PersonActions {
  

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
  

  
  
  public int currentAP() {
    return actionPoints;
  }
  
  
  public float confidence() {
    return confidence;
  }
  
  
  public float wariness() {
    return wariness;
  }
  
  
  public void modifyAP(int modifier) {
    actionPoints += modifier;
  }
  
  
  public void setActionPoints(int AP) {
    actionPoints = AP;
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
  
  
  public void assignAction(Action nextAction) {
    this.nextAction = nextAction;
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
    return true;
  }
  
  
  public boolean onTurnEnd() {
    return true;
  }
  
  
  public boolean updateDuringTurn() {
    //  TODO:  FILL THIS IN!
    return true;
  }
  
  
  public Action selectAIAction() {
    //  TODO:  FILL THIS IN!
    return null;
  }
  
  
  
  

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




