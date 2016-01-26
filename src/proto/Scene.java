

package proto;
import util.*;
import view.*;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;



public class Scene implements Session.Saveable, Assignment {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static int
    STATE_INIT  = -1,
    STATE_SETUP =  0,
    STATE_BEGUN =  1,
    STATE_WON   =  2,
    STATE_LOST  =  3;
  
  SceneView view = new SceneView(this);
  
  String name;
  float dangerLevel;
  int expireTime;
  World world;
  Nation site;
  List <Person> playerTeam = new List();
  List <Person> othersTeam  = new List();
  int state = STATE_INIT;
  
  int size;
  int time;
  Tile tiles[][] = new Tile[0][0];
  byte fog  [][] = new byte[0][0];
  List <Prop> props = new List();
  List <Person> persons = new List();
  
  boolean playerTurn;
  Person nextActing;
  Action currentAction;
  
  
  Scene(World world, int size) {
    this.world = world;
    this.size = size;
  }
  
  
  public Scene(Session s) throws Exception {
    s.cacheInstance(this);
    view.loadState(s);
    
    name        = s.loadString();
    dangerLevel = s.loadFloat();
    expireTime  = s.loadInt();
    world       = (World) s.loadObject();
    site        = (Nation) s.loadObject();
    s.loadObjects(playerTeam);
    s.loadObjects(othersTeam);
    state = s.loadInt();
    
    size   = s.loadInt();
    time   = s.loadInt();
    int tS = s.loadInt();
    tiles  = new Tile[tS][tS];
    fog    = new byte[tS][tS];
    for (Coord c : Visit.grid(0, 0, tS, tS, 1)) {
      tiles[c.x][c.y] = (Tile) s.loadObject();
    }
    s.loadByteArray(fog);
    s.loadObjects(props);
    s.loadObjects(persons);
    
    playerTurn    = s.loadBool();
    nextActing    = (Person) s.loadObject();
    currentAction = (Action) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    view.saveState(s);
    
    s.saveString (name       );
    s.saveFloat  (dangerLevel);
    s.saveInt    (expireTime );
    s.saveObject (world      );
    s.saveObject (site       );
    s.saveObjects(playerTeam );
    s.saveObjects(othersTeam );
    s.saveInt    (state      );
    
    s.saveInt(size);
    s.saveInt(time);
    int tS = tiles.length;
    s.saveInt(tS);
    for (Coord c : Visit.grid(0, 0, tS, tS, 1)) {
      s.saveObject(tiles[c.x][c.y]);
    }
    s.saveByteArray(fog);
    s.saveObjects(props);
    s.saveObjects(persons);
    
    s.saveBool(playerTurn);
    s.saveObject(nextActing);
    s.saveObject(currentAction);
  }
  
  
  
  /**  Supplemental query and setup methods-
    */
  public Nation site() {
    return site;
  }
  
  
  public World world() {
    return world;
  }
  
  
  public boolean begun() {
    return state >= STATE_BEGUN;
  }
  
  
  public boolean finished() {
    return state == STATE_WON || state == STATE_LOST;
  }
  
  
  public boolean wasWon() {
    return state == STATE_WON;
  }
  
  
  public boolean wasLost() {
    return state == STATE_LOST;
  }
  
  
  public float dangerLevel() {
    return dangerLevel;
  }
  

  public int size() {
    return size;
  }
  
  
  public Tile tileAt(int x, int y) {
    try { return tiles[x][y]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  public Object topObjectAt(Tile at) {
    if (at == null) return null;
    for (Person p : persons) if (p.location == at) return p;
    if (at.prop != null) return at.prop;
    return at;
  }
  
  
  public Tile tileUnder(Object object) {
    if (object instanceof Tile  ) return (Tile) object;
    if (object instanceof Person) return ((Person) object).location;
    if (object instanceof Prop  ) return ((Prop) object).origin;
    return null;
  }
  
  
  public float distance(Tile a, Tile b) {
    final int xd = a.x - b.x, yd = a.y - b.y;
    return Nums.sqrt((xd * xd) + (yd * yd));
  }
  
  
  public float fogAt(Tile at) {
    return fog[at.x][at.y] / 100f;
  }
  
  
  public boolean blockedAt(Tile at) {
    return at.prop != null && at.prop.kind.blockPath;
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
      if (t != orig && t.prop != null && t.prop.kind.blockSight) {
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
    if (under == null || exits == null || ! exits.retreating()) return false;
    if (under.x == 0 || under.x == size - 1) return true;
    if (under.y == 0 || under.y == size - 1) return true;
    return false;
  }
  
  
  public Action currentAction() {
    return currentAction;
  }
  
  
  
  /**  Supplementary population methods for use during initial setup-
    */
  boolean addProp(Kind type, int x, int y) {
    Prop prop = new Prop(type);
    for (Coord c : Visit.grid(x, y, type.wide, type.high, 1)) {
      Tile under = tileAt(c.x, c.y);
      if (under == null) return false;
      else under.prop = prop;
    }
    prop.origin = tileAt(x, y);
    props.add(prop);
    return true;
  }
  
  
  public Series <Prop> props() {
    return props;
  }
  
  
  public void addToTeam(Person p) {
    playerTeam.include(p);
    p.setAssignment(this);
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
  }
  
  
  public boolean addPerson(Person p, int x, int y) {
    Tile location = tileAt(x, y);
    if (location == null) return false;
    p.setAssignment(this);
    p.location = location;
    p.posX     = x;
    p.posY     = y;
    p.location.standing = p;
    p.actionPoints = p.maxAP();
    persons.add(p);
    if (! playerTeam.includes(p)) othersTeam.add(p);
    return true;
  }
  
  
  public boolean removePerson(Person p) {
    if (p.currentScene() != this) return false;
    p.setAssignment(null);
    p.location = null;
    playerTeam.remove(p);
    othersTeam.remove(p);
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
  
  
  
  /**  Regular updates and activity cycle:
    */
  void setupScene() {
    this.state = STATE_SETUP;
    
    tiles = new Tile[size][size];
    fog   = new byte[size][size];
    
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      tiles[c.x][c.y] = new Tile(this, c.x, c.y);
    }
  }
  
  
  void beginScene() {
    this.state = STATE_BEGUN;
    
    int numT = playerTeam.size();
    int x = (size - numT) / 2, y = 0;
    view.setViewpoint(tileAt(x, y));
    for (Person p : playerTeam) {
      addPerson(p, x, y);
      x += 1;
    }
    nextActing = playerTeam.first();
    playerTurn = true;
    
    updateFog();
  }
  
  
  public void queueNextAction(Action action) {
    currentAction = action;
    Person acting = action.acting;
    acting.actionPoints -= action.used.costAP(action);
    acting.location.standing = null;
    I.say(acting+" using "+action.used+" on "+action.target);
  }
  
  
  void onCompletion(Action action) {
    I.say("Action completed: "+action.used);
    I.say("  Player's turn? "+playerTurn);
    
    this.currentAction = null;
    view.setSelection(nextActing, null);
    
    if (action != null) {
      Person acting = action.acting;
      action.used.applyEffect(action);
      acting.location.standing = action.acting;
      
      //  TODO- have a 'wait for next action' phase instead, built into the
      //  scene's update method.
      if (acting.currentAP() > 0 && playerTurn) return;
    }
    
    moveToNextPersonsTurn();
  }
  
  
  public void moveToNextPersonsTurn() {
    
    for (int maxTries = 2; maxTries-- > 0;) {
      Series <Person> team = playerTurn ? playerTeam : othersTeam;
      nextActing = null;
      
      I.say("\n  Trying to find active person from ");
      if (playerTurn) I.add("player team"); else I.add("enemy team");
      I.say("  Active: "+nextActing);
      
      for (Person p : team) {
        if (! p.canTakeAction()) continue;
        
        if (playerTurn) {
          I.say("  ACTIVE PC: "+p);
          nextActing = p;
          break;
        }
        else {
          I.say("  FOUND NPC: "+p);
          Action taken = p.selectActionAsAI();
          if (taken != null) {
            I.say(p+" will take action: "+taken.used);
            queueNextAction(taken);
            nextActing = p;
            break;
          }
          else {
            I.say(p+" could not decide on action.");
            p.actionPoints = 0;
            continue;
          }
        }
      }
      
      if (nextActing == null) {
        I.say("  Will refresh AP and try other team...");
        for (Person p : team) {
          p.updateOnTurn();
          p.actionPoints = p.maxAP();
        }
        playerTurn = ! playerTurn;
      }
      else {
        I.say("  Will zoom to "+nextActing);
        view.setSelection(nextActing, null);
        return;
      }
    }
    I.say("\nWARNING- COULD NOT FIND NEXT ACTIVE PERSON!");
  }
  
  
  void updateFog() {
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      fog[c.x][c.y] = 0;
    }
    for (Person p : playerTeam) {
      final int radius = p.sightRange();
      
      for (Coord c : Visit.grid(
        p.location.x - radius,
        p.location.y - radius,
        radius * 2, radius * 2, 1
      )) {
        Tile t = tileAt(c.x, c.y);
        if (t == null) continue;
        float dist = distance(t, p.location);
        if (dist >= radius) continue;
        byte val = (byte) (100 * Nums.clamp(1.5f - (dist / radius), 0, 1));
        val *= degreeOfSight(p.location, t, p);
        fog[t.x][t.y] = (byte) Nums.max(val, fog[t.x][t.y]);
      }
    }
  }
  
  
  void updateScene() {
    
    final Action a = currentAction;
    if (a != null) {
      final Person p = a.acting;
      time++;
      
      int elapsed = time - a.timeStart;
      float timeSteps = elapsed * 2f / RunGame.FRAME_RATE;
      
      float alpha = timeSteps % 1, extraTime = a.used.animDuration();
      Tile l = a.path[Nums.clamp((int) timeSteps      , a.path.length)];
      Tile n = a.path[Nums.clamp((int) (timeSteps + 1), a.path.length)];
      
      p.posX = (alpha * n.x) + ((1 - alpha) * l.x);
      p.posY = (alpha * n.y) + ((1 - alpha) * l.y);
      p.location = this.tileAt((int) (p.posX + 0.5f), (int) (p.posY + 0.5f));
      
      if (timeSteps > a.path.length) {
        a.progress = (timeSteps - a.path.length) / extraTime;
      }
      if (timeSteps > a.path.length + extraTime) {
        onCompletion(a);
      }
      updateFog();
    }
    
    int nextState = checkCompletionStatus();
    if (nextState != STATE_BEGUN && ! finished()) {
      this.state = nextState;
      applySiteEffects();
    }
  }
  
  
  public void endScene() {
    for (Person p : persons) removePerson(p);
    world.exitFromMission(this);
  }
  
  
  
  /**  End-mission resolution-FX on the larger world:
    */
  int checkCompletionStatus() {
    
    boolean heroUp = false, criminalUp = false;
    for (Person p : playerTeam) if (p.isHero()) {
      if (p.conscious()) heroUp = true;
    }
    for (Person p : othersTeam) if (p.isCriminal()) {
      if (p.conscious()) criminalUp = true;
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
      float damage = p.injury / p.maxHealth();
      if (! p.alive()) damage += 3;
      if (! p.isCriminal()) damage *= 2;
      sum += damage;
    }
    return sum * 0.25f / othersTeam.size();
  }
  
  
  public float assessGetaways() {
    float sum = 0, numC = 0;
    for (Person p : othersTeam) if (p.isCriminal()) {
      numC++;
      if (p.conscious() || p.currentScene() != this) sum++;
    }
    if (numC == 0) return 0;
    return sum / numC;
  }
  
  
  void applySiteEffects() {
    boolean success = wasWon();
    float collateral = assessCollateral();
    float getaways   = assessGetaways  ();
    
    site.trust += (1 - getaways) * dangerLevel / 10;
    site.crime += getaways       * dangerLevel / 10;
    site.trust -= collateral / 10f;
    site.crime -= collateral / 10f;
    
    if (success) {
      site.trust += 0.5f / 10;
      site.crime -= 0.5f / 10;
    }
    else {
      site.trust -= 0.5f * dangerLevel / 10;
      site.crime += 0.5f * dangerLevel / 10;
    }
    site.trust = Nums.clamp(site.trust, -1, 1);
    site.crime = Nums.clamp(site.crime,  0, 2);
  }
  
  
  void resolveAsIgnored() {
    this.state = STATE_LOST;
    applySiteEffects();
  }
  
  
  
  /**  Graphical/display routines:
    */
  public SceneView view() {
    return view;
  }
  
  
  public String toString() {
    return name;
  }
  
  
  public String name() {
    return name;
  }
}





