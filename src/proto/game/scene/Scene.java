

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.scene.*;

import java.awt.Image;



public class Scene extends Scenery implements Assignment {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    STATE_INIT   = -1,
    STATE_SETUP  =  0,
    STATE_BEGUN  =  1,
    STATE_WON    =  2,
    STATE_LOST   =  3,
    STATE_ABSENT =  4;
  
  final World world;
  Place site;
  Task playerTask;
  Event triggerEvent;
  int state = STATE_INIT;
  
  List <Person> playerTeam = new List();
  List <Person> othersTeam = new List();
  List <Person> allPersons = new List();
  List <Person> didEnter   = new List();
  
  int time;
  List <PropEffect> effectProps = new List();
  
  final public SceneEntry  entry  = new SceneEntry (this);
  final public SceneVision vision = new SceneVision(this);
  
  boolean playerTurn, forTesting;
  Stack <Action> actionStack = new Stack();
  
  
  public Scene(
    SceneType type, World world, int wide, int high, boolean testing
  ) {
    super(type, wide, high, testing);
    this.world = world;
    vision.setupFog(wide, high);
  }
  
  
  protected void setupScene(boolean testing) {
    this.state      = STATE_SETUP;
    this.forTesting = testing;
    super.setupScene(testing);
  }
  
  
  public Scene(Session s) throws Exception {
    super(s);
    vision.setupFog(wide, high);
    
    world        = (World) s.loadObject();
    site         = (Place) s.loadObject();
    playerTask   = (Task ) s.loadObject();
    triggerEvent = (Event) s.loadObject();
    state        = s.loadInt();
    time         = s.loadInt();
    
    s.loadObjects(playerTeam );
    s.loadObjects(othersTeam );
    s.loadObjects(allPersons );
    s.loadObjects(didEnter   );
    s.loadObjects(effectProps);
    
    entry .loadState(s);
    vision.loadState(s);
    playerTurn = s.loadBool();
    forTesting = s.loadBool();
    s.loadObjects(actionStack);
    
    view().loadState(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject (world       );
    s.saveObject (site        );
    s.saveObject (playerTask  );
    s.saveObject (triggerEvent);
    s.saveInt    (state       );
    s.saveInt    (time        );
    
    s.saveObjects(playerTeam );
    s.saveObjects(othersTeam );
    s.saveObjects(allPersons );
    s.saveObjects(didEnter   );
    s.saveObjects(effectProps);
    
    entry .saveState(s);
    vision.saveState(s);
    s.saveBool(playerTurn);
    s.saveBool(forTesting);
    s.saveObjects(actionStack);
    
    view().saveState(s);
  }
  
  
  
  /**  Assignment methods-
    */
  public void setAssigned(Person p, boolean is) {
    if (p.isPlayerOwned()) playerTeam.toggleMember(p, is);
    else                   othersTeam.toggleMember(p, is);
  }
  
  
  public Series <Person> assigned() {
    return playerTeam;
  }
  
  
  public Base assigningBase() {
    return world.playerBase();
  }
  
  
  public boolean complete() {
    return state == STATE_WON || state == STATE_LOST;
  }
  
  
  public Element targetElement(Person p) {
    return site;
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_ON_SCENE;
  }
  
  
  
  /**  Supplemental query methods-
    */
  public World world() {
    return world;
  }
  
  
  public Place site() {
    return site;
  }
  
  
  public Task playerTask() {
    return playerTask;
  }
  
  
  public Event triggerEvent() {
    return triggerEvent;
  }
  
  
  public boolean begun() {
    return state >= STATE_BEGUN;
  }
  
  
  public boolean wasWon() {
    return state == STATE_WON;
  }
  
  
  public boolean wasLost() {
    return state == STATE_LOST;
  }
  
  
  public Series <Person> playerTeam() {
    return playerTeam;
  }
  
  
  public Series <Person> othersTeam() {
    return othersTeam;
  }
  
  
  public Series <Person> allPersons() {
    return allPersons;
  }
  
  
  public Series <Person> didEnter() {
    return didEnter;
  }
  
  
  public Action currentAction() {
    return actionStack.first();
  }
  
  
  public Person currentActing() {
    if (actionStack.empty()) return null;
    return actionStack.first().acting;
  }
  
  
  public Series <Action> actionStack() {
    return actionStack;
  }
  
  
  
  /**  Supplementary population methods for use during initial setup-
    */
  public boolean enterScene(Person p, int x, int y) {
    Tile location = tileAt(x, y);
    if (location == null) return false;
    
    p.gear .refreshCharges  ();
    p.stats.refreshCooldowns();
    
    p.setExactPosition(this, x, y, 0);
    allPersons.include(p);
    didEnter.include(p);
    vision.liftFogInSight(p);
    return true;
  }
  
  
  public boolean removePerson(Person p) {
    if (p.currentScene() != this) return false;
    p.setExactPosition(null, 0, 0, 0);
    allPersons.remove(p);
    p.removeAssignment(this);
    return true;
  }
  
  
  public Series <Prop> propsOfKind(Kind type) {
    Batch ofKind = new Batch();
    for (Prop p : props) if (p.kind == type) ofKind.add(p);
    return ofKind;
  }
  
  
  public void assignMissionParameters(
    Place site, Task playerTask, Event triggerEvent
  ) {
    this.site         = site        ;
    this.playerTask   = playerTask  ;
    this.triggerEvent = triggerEvent;
  }
  
  
  
  /**  Regular updates and life cycle:
    */
  public void beginScene() {
    if (state >= STATE_BEGUN) return;
    this.state = STATE_BEGUN;
    
    I.say("\nBEGINNING SCENE...");
    playerTurn = true;
    vision.updateFog();
    for (Person p : playerTeam) p.onTurnStart();
    for (Person p : othersTeam) p.onTurnStart();
  }
  
  
  public void updateScene() {
    if (state < STATE_BEGUN) beginScene();
    
    boolean skipUpdate = playerTeam.empty() && othersTeam.empty();
    if (skipUpdate || GameSettings.pauseScene || complete()) return;
    
    Action nextAction = currentAction();
    Person nextActing = currentActing();
    
    if (nextActing != null && ! nextAction.complete()) {
      
      for (Person p : playerTeam) if (p != nextActing) {
        p.updateInScene(false, null);
      }
      for (Person p : othersTeam) if (p != nextActing) {
        p.updateInScene(false, null);
      }
      
      nextAction.incTimeSpent(1);
      nextActing.updateInScene(true, nextAction);
      time += 1;
    }
    else moveToNextPersonsTurn();
    
    int nextState = GameSettings.debugScene ? state : checkCompletionStatus();
    if (nextState > STATE_BEGUN && ! complete()) {
      onSceneCompletion(nextState);
    }
  }
  
  
  public void pushNextAction(Action action) {
    actionStack.addFirst(action);
  }
  
  
  public void moveToNextPersonsTurn() {
    //
    //  Remove any expired actions from the action-stack first (if any remain,
    //  return and let those execute first.)
    for (Action a : actionStack) {
      if (a.complete()) actionStack.remove(a);
    }
    if (! actionStack.empty()) return;
    //
    //  If it's the player's turn, simply check that that all action-points
    //  haven't been exhausted yet (we wait for input from the human player.)
    //  If they have, refresh AP and switch to the other team.
    if (playerTurn) {
      boolean noOneLeft = true;
      for (Person p : playerTeam) {
        if (p.actions.canTakeAction()) noOneLeft = false;
      }
      
      if (noOneLeft) {
        I.say("\nNo action-points left on player team...");
        playerTurn = false;
        for (Person p : playerTeam) if (p.currentScene() == this) {
          p.onTurnEnd();
        }
        for (Person p : othersTeam) if (p.currentScene() == this) {
          p.onTurnStart();
        }
        for (PropEffect p : effectProps) {
          p.onTurnStart();
        }
      }
    }
    //
    //  If it's the AI turn, we iterate across all non-player agents, see if
    //  they can settle on a suitable action, and assign the first that gets
    //  returned.
    //  In the event that no actions can be assigned, refresh AP and switch
    //  back to the player.
    else {
      I.say("\nSelecting next action from AI team...");
      
      boolean noOneLeft = true;
      for (Person p : othersTeam) {
        if (! p.actions.canTakeAction()) continue;
        
        Action taken = p.mind.selectActionAsAI();
        if (taken != null) {
          I.say("  "+p+" will take action: "+taken);
          p.actions.assignAction(taken);
          pushNextAction(taken);
          noOneLeft = false;
          break;
        }
        else {
          I.say("  "+p+" could not decide on action.");
          p.actions.setActionPoints(0);
        }
      }
      
      if (noOneLeft) {
        I.say("\n  No action-points left on others team...");
        playerTurn = true;
        for (Person p : othersTeam) if (p.currentScene() == this) {
          p.onTurnEnd();
        }
        for (Person p : playerTeam) if (p.currentScene() == this) {
          p.onTurnStart();
        }
        for (PropEffect p : effectProps) {
          p.onTurnEnd();
        }
      }
    }
  }
  
  
  
  /**  End-mission resolution-FX on the larger world:
    */
  public int checkCompletionStatus() {
    if (forTesting) return STATE_BEGUN;
    
    boolean heroUp = false, criminalUp = false;
    for (Person p : playerTeam) if (p.isHero()) {
      if (p.health.conscious() && p.currentScene() == this) heroUp = true;
    }
    for (Person p : othersTeam) if (p.isCriminal()) {
      if (p.health.conscious() && p.currentScene() == this) criminalUp = true;
    }
    if (! criminalUp) {
      return STATE_WON;
    }
    if (! heroUp) {
      return STATE_LOST;
    }
    return STATE_BEGUN;
  }
  
  
  public void onSceneCompletion(int endState) {
    this.state = endState;
    
    if (site == null || triggerEvent == null || playerTask == null) {
      I.say("MISSION PARAMETERS NOT SPECIFIED FOR SCENE, WILL EXIT DIRECTLY");
      performSceneExit();
    }
    else {
      final EventEffects effects = triggerEvent.generateEffects(this);
      triggerEvent.completeAfterScene(this, effects);
      playerTask.onSceneExit(this, effects);
      effects.presentMessageForScene(this, triggerEvent);
    }
  }
  
  
  public void performSceneExit() {
    for (Person p : didEnter) {
      removePerson(p);
    }
    world.exitFromScene(this);
  }
  
  
  
  /**  Graphical/display routines:
    */
  public SceneView view() {
    return world.view().sceneView();
  }
  
  
  public String toString() {
    if (playerTask == null) return "Scene";
    return playerTask+" during "+triggerEvent;
  }
  
  
  public String activeInfo() {
    if (playerTask == null) return "On Scene";
    return "On mission: "+playerTask.activeInfo();
  }
  
  
  public Image icon() {
    if (playerTask == null) return null;
    return playerTask.icon();
  }
  
}





