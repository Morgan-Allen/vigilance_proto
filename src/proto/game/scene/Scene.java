

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.scene.*;

import java.awt.Image;



public class Scene implements Session.Saveable, Assignment, TileConstants {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    STATE_TEST   = -2,
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
  
  int size;
  int time;
  Tile tiles[][] = new Tile[0][0];
  byte wallM[][] = new byte[1][2];
  byte opacM[][] = new byte[1][2];
  List <Prop> props = new List();
  
  final public SceneEntry  entry  = new SceneEntry (this);
  final public SceneVision vision = new SceneVision(this);
  
  boolean playerTurn;
  Person nextActing;
  
  
  public Scene(World world, int size) {
    this.world = world;
    this.size  = size;
  }
  
  
  public Scene(Session s) throws Exception {
    s.cacheInstance(this);
    
    world        = (World) s.loadObject();
    site         = (Place) s.loadObject();
    playerTask   = (Task ) s.loadObject();
    triggerEvent = (Event) s.loadObject();
    state = s.loadInt();
    
    s.loadObjects(playerTeam);
    s.loadObjects(othersTeam);
    s.loadObjects(allPersons);
    s.loadObjects(didEnter  );
    
    size   = s.loadInt();
    time   = s.loadInt();
    int tS = s.loadInt();
    initArrays(tS);
    for (Coord c : Visit.grid(0, 0, tS, tS, 1)) {
      tiles[c.x][c.y] = (Tile) s.loadObject();
    }
    s.loadByteArray(wallM);
    s.loadByteArray(opacM);
    s.loadObjects(props);
    
    entry .loadState(s);
    vision.loadState(s);
    
    playerTurn = s.loadBool();
    nextActing = (Person) s.loadObject();
    
    view().loadState(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject (world       );
    s.saveObject (site        );
    s.saveObject (playerTask  );
    s.saveObject (triggerEvent);
    s.saveInt    (state       );
    
    s.saveObjects(playerTeam);
    s.saveObjects(othersTeam);
    s.saveObjects(allPersons);
    s.saveObjects(didEnter  );
    
    s.saveInt(size);
    s.saveInt(time);
    int tS = tiles.length;
    s.saveInt(tS);
    for (Coord c : Visit.grid(0, 0, tS, tS, 1)) {
      s.saveObject(tiles[c.x][c.y]);
    }
    s.saveByteArray(wallM);
    s.saveByteArray(opacM);
    s.saveObjects(props);
    
    entry .saveState(s);
    vision.saveState(s);
    
    s.saveBool(playerTurn);
    s.saveObject(nextActing);
    
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
  
  
  public boolean complete() {
    return state == STATE_WON || state == STATE_LOST;
  }
  
  
  public Place targetLocation() {
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
  
  
  public PlanStep triggerEventPlanStep() {
    return triggerEvent == null ? null : triggerEvent.planStep();
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
  
  
  public int size() {
    return size;
  }
  
  
  public int time() {
    return time;
  }
  
  
  public Series <Prop> props() {
    return props;
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
  
  
  public Tile tileAt(int x, int y) {
    try { return tiles[x][y]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  public Tile tileUnder(Object object) {
    if (object instanceof Tile  ) return  (Tile  ) object;
    if (object instanceof Person) return ((Person) object).currentTile();
    if (object instanceof Prop  ) return ((Prop  ) object).origin;
    return null;
  }
  
  
  public float distance(Object a, Object b) {
    return distance(tileUnder(a), tileUnder(b));
  }
  
  
  public float distance(Tile a, Tile b) {
    final int xd = a.x - b.x, yd = a.y - b.y;
    return Nums.sqrt((xd * xd) + (yd * yd));
  }
  
  
  public int direction(Tile from, Tile to) {
    float angle = new Vec2D(to.x - from.x, to.y - from.y).toAngle();
    angle = (angle + 360 + 45) % 360;
    int dir = 2 * (int) (angle / 90);
    return (N + dir + 8) % 8;
  }
  
  
  public Action currentAction() {
    if (nextActing == null) return null;
    return nextActing.actions.nextAction();
  }
  
  
  public Visit <Tile> tilesInArea(Box2D area) {
    final Visit <Coord> base = Visit.grid(area);
    return new Visit <Tile> () {
      
      public Tile next() {
        Coord c = base.next();
        return tileAt(c.x, c.y);
      }
      
      public boolean hasNext() {
        return base.hasNext();
      }
    };
  }
  
  
  
  /**  Supplementary population methods for use during initial setup-
    */
  private void initArrays(int size) {
    tiles = new Tile[size][size];
    int wallS = (size * 2) + 1;
    wallM = new byte[wallS][wallS];
    opacM = new byte[wallS][wallS];
    vision.setupFog(size);
  }
  
  
  public void setupScene(boolean forTesting) {
    this.state = forTesting ? STATE_TEST : STATE_SETUP;
    initArrays(size);
    
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      tiles[c.x][c.y] = new Tile(this, c.x, c.y);
    }
  }
  
  
  public Prop addProp(PropType type, int x, int y, int facing) {
    final Prop prop = new Prop(type, world);
    return prop.enterScene(this, x, y, facing) ? prop : null;
  }
  
  
  public boolean enterScene(Person p, int x, int y) {
    Tile location = tileAt(x, y);
    if (location == null) return false;
    
    p.gear.refreshCharges();
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
    this.state = STATE_BEGUN;
    
    I.say("\nBEGINNING SCENE...");
    int numT = playerTeam.size();
    int x = (size - numT) / 2, y = 0;
    view().setZoomPoint(tileAt(x, y));
    for (Person p : playerTeam) {
      enterScene(p, x, y);
      x += 1;
    }
    nextActing = playerTeam.first();
    view().setSelection(nextActing, false);
    playerTurn = true;
    
    vision.updateFog();
    for (Person p : playerTeam) p.onTurnStart();
    for (Person p : othersTeam) p.onTurnStart();
  }
  
  
  public void updateScene() {
    if (GameSettings.pauseScene) return;

    for (Person p : playerTeam) if (p != nextActing) {
      p.updateInScene(false);
    }
    for (Person p : othersTeam) if (p != nextActing) {
      p.updateInScene(false);
    }
    if (nextActing != null) {
      final PersonActions nextPA = nextActing.actions;
      Action last = nextPA.nextAction();
      nextActing.updateInScene(true);
      Action taken = nextPA.nextAction();
      
      if (taken != null) {
        time += 1;
      }
      else {
        if (last != null) view().setSelection(nextActing, false);
        
        if (! (nextPA.canTakeAction() && playerTurn)) {
          moveToNextPersonsTurn();
        }
      }
    }
    else moveToNextPersonsTurn();
    
    int nextState = checkCompletionStatus();
    if (nextState > STATE_BEGUN && ! complete()) {
      this.state = nextState;
      onSceneCompletion();
    }
  }
  
  
  public void setNextActing(Person acting) {
    nextActing = acting;
  }
  
  
  public void moveToNextPersonsTurn() {
    I.say("\n  Trying to find next active person...");
    I.say("  Active: "+nextActing);
    
    PersonActions NA = nextActing == null ? null : nextActing.actions;
    if ((NA != null) && (! NA.turnDone()) && (! NA.canTakeAction())) {
      I.say("\n    Ending turn for "+nextActing);
      nextActing.onTurnEnd();
    }
    nextActing = null;
    final int numTeams = 2;
    
    for (int maxTries = numTeams + 1; maxTries-- > 0;) {
      Series <Person> team     = playerTurn ? playerTeam : othersTeam;
      Series <Person> nextTeam = playerTurn ? othersTeam : playerTeam;
      
      I.say("\n    Trying to find active person from ");
      if (playerTurn) I.add("player team"); else I.add("enemy team");
      
      for (Person p : team) {
        final PersonActions PA = p.actions;
        if (! PA.canTakeAction()) continue;
        
        if (playerTurn) {
          I.say("    ACTIVE PC: "+p+", AP: "+PA.currentAP());
          nextActing = p;
          break;
        }
        else {
          I.say("    FOUND NPC: "+p+", AP: "+PA.currentAP());
          Action taken = p.mind.selectActionAsAI();
          if (taken != null) {
            I.say("    "+p+" will take action: "+taken.used);
            nextActing = p;
            PA.assignAction(taken);
            break;
          }
          else {
            I.say("    "+p+" could not decide on action.");
            PA.setActionPoints(0);
            continue;
          }
        }
      }
      
      if (nextActing == null) {
        I.say("    Will refresh AP and try other team...");
        for (Person p : nextTeam) if (p.currentScene() == this) {
          p.onTurnStart();
        }
        playerTurn = ! playerTurn;
      }
      else {
        I.talkAbout = nextActing;
        //
        //  Zoom to the unit in question (if they're visible...)
        //  TODO:  Ideally this should be handled within the UI!
        if (vision.fogAt(nextActing.currentTile(), Person.Side.HEROES) > 0) {
          I.say("    Will zoom to "+nextActing);
          view().setSelection(nextActing, false);
          view().setZoomPoint(nextActing.currentTile());
        }
        return;
      }
    }
    I.say("\nWARNING- COULD NOT FIND NEXT ACTIVE PERSON!");
  }
  
  
  
  /**  End-mission resolution-FX on the larger world:
    */
  int checkCompletionStatus() {
    if (state == STATE_TEST) return state;
    
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
  
  
  public void onSceneCompletion() {
    
    final EventReport report = new EventReport();
    report.composeFromScene(this);
    
    if (triggerEvent != null) {
      triggerEvent.completeAfterScene(this, report);
    }
    if (playerTask != null) {
      playerTask.onSceneExit(this, report);
    }
    
    report.applyOutcomeEffects(site);
    report.presentMessageForScene(this);
    
    //  TODO:  Decide whether personnel are left injured, hospitalised or dead.
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
    return playerTask.toString();
  }
  
  
  public String activeInfo() {
    if (playerTask == null) return "On Scene";
    return "On mission: "+playerTask.activeInfo();
  }
  
  
  public String helpInfo() {
    if (playerTask == null) return "On Scene";
    return playerTask.helpInfo();
  }
  
  
  public Image icon() {
    if (playerTask == null) return null;
    return playerTask.icon();
  }
  
  
}





