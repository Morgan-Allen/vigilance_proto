

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.*;
import proto.view.scene.SceneView;

import java.awt.Image;


//  TODO:  This doesn't need to implement Assignment.

public class Scene implements Session.Saveable, Assignment, TileConstants {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static int
    STATE_INIT  = -1,
    STATE_SETUP =  0,
    STATE_BEGUN =  1,
    STATE_WON   =  2,
    STATE_LOST  =  3;
  
  World world;
  Place site;
  Task playerTask;
  Event triggerEvent;
  List <Person> playerTeam = new List();
  List <Person> othersTeam = new List();
  int state = STATE_INIT;
  
  int size;
  int time;
  Tile tiles[][] = new Tile[0][0];
  byte fogP [][] = new byte[0][0];
  byte fogO [][] = new byte[0][0];
  List <Prop> props = new List();
  List <Person> persons = new List();
  
  boolean playerTurn;
  Person nextActing;
  
  
  public Scene(Place place, int size) {
    this.world = place.world();
    this.site  = place;
    this.size  = size;
  }
  
  
  public Scene(Session s) throws Exception {
    s.cacheInstance(this);
    
    world        = (World) s.loadObject();
    site         = (Place) s.loadObject();
    playerTask   = (Task ) s.loadObject();
    triggerEvent = (Event) s.loadObject();
    s.loadObjects(playerTeam);
    s.loadObjects(othersTeam);
    state = s.loadInt();
    
    size   = s.loadInt();
    time   = s.loadInt();
    int tS = s.loadInt();
    tiles  = new Tile[tS][tS];
    fogP   = new byte[tS][tS];
    fogO   = new byte[tS][tS];
    for (Coord c : Visit.grid(0, 0, tS, tS, 1)) {
      tiles[c.x][c.y] = (Tile) s.loadObject();
    }
    s.loadByteArray(fogP);
    s.loadByteArray(fogO);
    s.loadObjects(props);
    s.loadObjects(persons);
    
    playerTurn = s.loadBool();
    nextActing = (Person) s.loadObject();
    
    view().loadState(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject (world       );
    s.saveObject (site        );
    s.saveObject (playerTask  );
    s.saveObject (triggerEvent);
    s.saveObjects(playerTeam  );
    s.saveObjects(othersTeam  );
    s.saveInt    (state       );
    
    s.saveInt(size);
    s.saveInt(time);
    int tS = tiles.length;
    s.saveInt(tS);
    for (Coord c : Visit.grid(0, 0, tS, tS, 1)) {
      s.saveObject(tiles[c.x][c.y]);
    }
    s.saveByteArray(fogP);
    s.saveByteArray(fogO);
    s.saveObjects(props);
    s.saveObjects(persons);
    
    s.saveBool(playerTurn);
    s.saveObject(nextActing);
    
    view().saveState(s);
  }
  
  
  
  /**  Assignment methods-
    */
  public void setAssigned(Person p, boolean is) {
    if (is) addToTeam(p);
    else removePerson(p);
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
  
  
  
  /**  Supplemental query and setup methods-
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
  
  
  public float distance(Tile a, Tile b) {
    final int xd = a.x - b.x, yd = a.y - b.y;
    return Nums.sqrt((xd * xd) + (yd * yd));
  }
  
  
  public float fogAt(Tile at, Person.Side side) {
    if (side == Person.Side.HEROES  ) return fogP[at.x][at.y] / 100f;
    if (side == Person.Side.VILLAINS) return fogO[at.x][at.y] / 100f;
    return 1;
  }
  
  
  public float degreeOfSight(Tile orig, Tile dest, Person p) {
    
    int spanX = dest.x - orig.x, spanY = dest.y - orig.y;
    float ratioY = Nums.abs(spanY * 1f / spanX);
    float maxSteps = distance(orig, dest) * 2;
    
    float sight = 1f;
    int x = orig.x, y = orig.y;
    float scanY = ratioY;
    
    while (true) {
      
      Tile t = tiles[x][y];
      if (t == dest) break;
      if (t != orig && t.prop() != null && t.prop().kind.blockSight()) {
        sight *= 0;
      }
      if (sight == 0) break;
      if (maxSteps-- < 0) { I.complain("TOO MANY STEPS!"); break; }
      
      if (spanX == 0) { y += spanY > 0 ? 1 : -1; continue; }
      if (spanY == 0) { x += spanX > 0 ? 1 : -1; continue; }
      
      boolean incY = scanY >= 1, incX = scanY <= 1;
      if (incX) {
        x += spanX > 0 ? 1 : -1;
        scanY += ratioY;
      }
      if (incY) {
        y += spanY > 0 ? 1 : -1;
        scanY--;
      }
    }
    return sight;
  }
  
  
  public boolean isExitPoint(Object point, Person exits) {
    Tile under = tileUnder(point);
    if (under == null || exits == null || ! exits.mind.retreating()) {
      return false;
    }
    if (under.x == 0 || under.x == size - 1) return true;
    if (under.y == 0 || under.y == size - 1) return true;
    return false;
  }
  
  
  public Tile findEntryPoint(int x, int y, Person enters) {
    Tile under = tileAt(x, y);
    int dir = T_INDEX[Rand.index(T_INDEX.length)], size = size();
    
    while (under != null) {
      if (x != Nums.clamp(x, size) || y != Nums.clamp(y, size)) break;
      if (! under.blocked()) return under;
      x += T_X[dir];
      y += T_Y[dir];
      under = tileAt(x, y);
    }
    return under;
  }
  
  
  public Action currentAction() {
    if (nextActing == null) return null;
    return nextActing.actions.nextAction();
  }
  
  
  
  /**  Supplementary population methods for use during initial setup-
    */
  public boolean addProp(Kind type, int x, int y) {
    Prop prop = new Prop(type, world);
    for (Coord c : Visit.grid(x, y, type.wide(), type.high(), 1)) {
      Tile under = tileAt(c.x, c.y);
      if (under == null) return false;
      else under.setProp(prop);
    }
    prop.origin = tileAt(x, y);
    props.add(prop);
    return true;
  }
  
  
  public boolean hasSpace(Kind type, int x, int y) {
    for (Coord c : Visit.grid(x, y, type.wide(), type.high(), 1)) {
      Tile under = tileAt(c.x, c.y);
      if (under == null) return false;
      if (under.prop() == null) continue;
      final Kind uK = under.prop().kind();
      if (uK.blockPath() || uK.blockSight()) return false;
    }
    return true;
  }
  
  
  public boolean removeProp(Prop prop) {
    prop.origin.setProp(null);
    props.remove(prop);
    return true;
  }
  
  
  public Series <Prop> props() {
    return props;
  }
  
  
  public void addToTeam(Person p) {
    if (p.isPlayerOwned()) playerTeam.include(p);
    else                   othersTeam.include(p);
    p.setAssignment(this);
  }
  
  
  public boolean enterScene(Person p, int x, int y) {
    Tile location = tileAt(x, y);
    if (location == null) return false;
    p.setAssignment(this);
    p.setExactPosition(this, x, y, 0);
    persons.add(p);
    liftFogInSight(p);
    return true;
  }
  
  
  public boolean removePerson(Person p) {
    if (p.currentScene() != this) return false;
    
    playerTeam.remove(p);
    othersTeam.remove(p);
    
    p.setExactPosition(null, 0, 0, 0);
    p.setAssignment(null);
    persons.remove(p);
    return true;
  }
  
  
  public Series <Person> persons() {
    return persons;
  }
  
  
  public Series <Person> playerTeam() {
    return playerTeam;
  }
  
  
  public Series <Person> othersTeam() {
    return othersTeam;
  }
  
  
  public Series <Prop> propsOfKind(Kind type) {
    Batch ofKind = new Batch();
    for (Prop p : props) if (p.kind == type) ofKind.add(p);
    return ofKind;
  }
  
  
  
  /**  Regular updates and activity cycle:
    */
  public void setupScene() {
    this.state = STATE_SETUP;
    
    tiles = new Tile[size][size];
    fogP  = new byte[size][size];
    fogO  = new byte[size][size];
    
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      tiles[c.x][c.y] = new Tile(this, c.x, c.y);
    }
  }
  
  
  public void assignMissionParameters(
    Place site, Task playerTask, Event triggerEvent
  ) {
    this.site         = site        ;
    this.playerTask   = playerTask  ;
    this.triggerEvent = triggerEvent;
  }
  
  
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
    
    updateFog();
    for (Person p : persons) p.onTurnStart();
  }
  
  
  public void updateScene() {

    for (Person p : persons) if (p != nextActing) {
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
        //
        //  Zoom to the unit in question (if they're visible...)
        if (fogAt(nextActing.currentTile(), Person.Side.HEROES) > 0) {
          I.say("    Will zoom to "+nextActing);
          view().setSelection(nextActing, false);
          view().setZoomPoint(nextActing.currentTile());
        }
        return;
      }
    }
    I.say("\nWARNING- COULD NOT FIND NEXT ACTIVE PERSON!");
  }
  
  
  public void endScene() {
    for (Person p : persons) removePerson(p);
    world.exitFromScene(this);
  }
  

  
  /**  Fog and visibility updates-
    */
  public void updateFog() {
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      fogP[c.x][c.y] = 0;
      fogO[c.x][c.y] = 0;
    }
    for (Person p : playerTeam) if (p.health.conscious()) {
      liftFogInSight(p);
    }
    for (Person p : othersTeam) if (p.health.conscious())  {
      liftFogInSight(p);
    }
  }
  
  
  public void liftFogInSight(Person p) {
    final float radius = p.actions.sightRange();
    liftFogAround(p.currentTile(), radius, p, true);
  }
  
  
  public void liftFogAround(
    Tile point, float radius, Person looks, boolean checkSight
  ) {
    byte fog[][] = null;
    if (looks.side() == Person.Side.HEROES  ) fog = fogP;
    if (looks.side() == Person.Side.VILLAINS) fog = fogO;
    if (fog == null) return;
    
    final int lim = Nums.ceil(radius);
    for (Coord c : Visit.grid(
      point.x - lim, point.y - lim, lim * 2, lim * 2, 1
    )) {
      Tile t = tileAt(c.x, c.y);
      if (t == null) continue;
      float dist = distance(t, point);
      if (dist >= radius) continue;
      byte val = (byte) (100 * Nums.clamp(1.5f - (dist / radius), 0, 1));
      if (checkSight) val *= degreeOfSight(point, t, looks);
      fog[t.x][t.y] = (byte) Nums.max(val, fog[t.x][t.y]);
    }
  }
  
  
  
  /**  End-mission resolution-FX on the larger world:
    */
  int checkCompletionStatus() {
    
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
  
  
  public float assessCollateral() {
    float sum = 0;
    for (Person p : othersTeam) {
      float damage = p.health.injury() / p.health.maxHealth();
      if (! p.health.alive()) damage += 3;
      if (! p.isCriminal()  ) damage *= 2;
      sum += Nums.max(0, damage - 0.5f) * 2;
    }
    return sum * 0.5f / othersTeam.size();
  }
  
  
  public float assessGetaways() {
    float sum = 0, numC = 0;
    for (Person p : othersTeam) if (p.isCriminal()) {
      numC++;
      if (p.health.conscious() || p.currentScene() != this) sum++;
    }
    if (numC == 0) return 0;
    return sum / numC;
  }
  
  
  public void onSceneCompletion() {
    boolean success    = wasWon();
    float   collateral = assessCollateral();
    float   getaways   = assessGetaways();
    
    world.exitFromScene(this);
    
    if (triggerEvent != null) {
      triggerEvent.completeWithEffects(success, collateral, getaways);
    }
    if (playerTask != null) {
      playerTask.setCompleted(success);
    }
  }
  
  
  
  /**  Graphical/display routines:
    */
  public SceneView view() {
    return world.view().sceneView();
  }
  
  
  public String toString() {
    if (playerTask == null) return site.name();
    return playerTask.toString();
  }
  
  
  public String activeInfo() {
    if (playerTask == null) return site.name();
    return "On mission: "+playerTask.activeInfo();
  }
  
  
  public String helpInfo() {
    if (playerTask == null) return site.name();
    return playerTask.helpInfo();
  }
  
  
  public Image icon() {
    if (playerTask == null) return site.kind().icon();
    return playerTask.icon();
  }
  
  
}





