

package proto;
import util.*;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;



public class Scene {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static int
    TILE_SIZE = 64;
  final static int
    STATE_INIT  = -1,
    STATE_SETUP =  0,
    STATE_BEGUN =  1,
    STATE_WON   =  2,
    STATE_LOST  =  3;
  
  
  String name;
  int expireTime;
  World world;
  Nation site;
  List <Person> playerTeam = new List();
  List <Person> othersTeam  = new List();
  int state = STATE_INIT;
  
  int size;
  Tile tiles[][];
  byte fog[][];
  List <Prop> props = new List();
  List <Person> persons = new List();
  int time;
  
  boolean playerTurn;
  Person selected;
  Ability activeAbility;
  Action currentAction;
  
  
  Scene(World world, int size) {
    this.world = world;
    this.size = size;
  }
  
  
  
  /**  Supplemental query and setup methods-
    */
  Tile tileAt(int x, int y) {
    try { return tiles[x][y]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  Object topObjectAt(Tile at) {
    if (at == null) return null;
    for (Person p : persons) if (p.location == at) return p;
    if (at.prop != null) return at.prop;
    return at;
  }
  
  
  Tile tileUnder(Object object) {
    if (object instanceof Tile  ) return (Tile) object;
    if (object instanceof Person) return ((Person) object).location;
    if (object instanceof Prop  ) return ((Prop) object).origin;
    return null;
  }
  
  
  float distance(Tile a, Tile b) {
    final int xd = a.x - b.x, yd = a.y - b.y;
    return Nums.sqrt((xd * xd) + (yd * yd));
  }
  
  
  float fogAt(Tile at) {
    return fog[at.x][at.y] / 100f;
  }
  
  
  boolean blockedAt(Tile at) {
    return at.prop != null && at.prop.kind.blockPath;
  }
  
  
  float degreeOfSight(Tile orig, Tile dest, Person p) {
    
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
  
  
  boolean begun() {
    return state >= STATE_BEGUN;
  }
  
  
  boolean finished() {
    return state == STATE_WON || state == STATE_LOST;
  }
  
  
  boolean wasWon() {
    return state == STATE_WON;
  }
  
  
  boolean wasLost() {
    return state == STATE_LOST;
  }
  
  
  boolean isExitPoint(Object point, Person exits) {
    Tile under = tileUnder(point);
    if (under == null || exits == null || ! exits.retreating()) return false;
    if (under.x == 0 || under.x == size - 1) return true;
    if (under.y == 0 || under.y == size - 1) return true;
    return false;
  }
  
  
  
  /**  Supplementary population methods for use during initial setup-
    */
  boolean addProp(Kind type, int x, int y) {
    Prop prop = new Prop();
    for (Coord c : Visit.grid(x, y, type.wide, type.high, 1)) {
      Tile under = tileAt(c.x, c.y);
      if (under == null) return false;
      else under.prop = prop;
    }
    prop.kind = type;
    prop.origin = tileAt(x, y);
    prop.bounds = new Box2D(x, y, type.wide, type.high);
    props.add(prop);
    return true;
  }
  
  
  void addToTeam(Person p) {
    playerTeam.include(p);
    p.scene = this;
  }
  
  
  boolean addPerson(Person p, int x, int y) {
    Tile location = tileAt(x, y);
    if (location == null) return false;
    p.scene    = this;
    p.location = location;
    p.posX     = x;
    p.posY     = y;
    p.location.standing = p;
    p.actionPoints = p.maxAP();
    persons.add(p);
    if (! playerTeam.includes(p)) othersTeam.add(p);
    return true;
  }
  
  
  boolean removePerson(Person p) {
    if (p.scene != this) return false;
    p.scene    = null;
    p.location = null;
    playerTeam.remove(p);
    othersTeam.remove(p);
    persons.remove(p);
    return true;
  }
  
  
  
  /**  Regular updates and activity cycle:
    */
  void setupScene() {
    this.state = STATE_SETUP;
    
    tiles = new Tile[size][size];
    fog   = new byte[size][size];
    
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = tiles[c.x][c.y] = new Tile();
      t.scene = this;
      t.x = c.x;
      t.y = c.y;
    }
  }
  
  
  void beginScene() {
    this.state = STATE_BEGUN;
    
    int numT = playerTeam.size();
    int x = (size - numT) / 2, y = 0;
    zoomTile = tileAt(x, y);
    for (Person p : playerTeam) {
      addPerson(p, x, y);
      x += 1;
    }
    selected = playerTeam.first();
    playerTurn = true;
    
    updateFog();
  }
  
  
  void beginSelection(Person acting, Ability ability) {
    this.selected = acting;
    this.activeAbility = ability;
  }
  
  
  void queueNextAction(Action action) {
    currentAction = action;
    Person acting = action.acting;
    acting.actionPoints -= action.used.costAP(action);
    acting.location.standing = null;
    I.say(acting+" using "+action.used+" on "+action.target);
  }
  
  
  void onCompletion(Action action) {
    I.say("Action completed: "+action.used);
    I.say("  Player's turn? "+playerTurn);
    this.activeAbility = null;
    this.currentAction = null;
    
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
  
  
  void moveToNextPersonsTurn() {
    
    for (int maxTries = 2; maxTries-- > 0;) {
      Series <Person> team = playerTurn ? playerTeam : othersTeam;
      selected = null;
      
      I.say("\n  Trying to find active person from ");
      if (playerTurn) I.add("player team"); else I.add("enemy team");
      I.say("  Active: "+selected);
      
      for (Person p : team) {
        if (p.currentAP() == 0 || ! p.conscious()) continue;
        
        if (playerTurn) {
          I.say("  ACTIVE PC: "+p);
          selected = p;
          break;
        }
        else {
          I.say("  FOUND NPC: "+p);
          Action taken = p.selectActionAsAI();
          if (taken != null) {
            I.say(p+" will take action: "+taken.used);
            queueNextAction(taken);
            selected = p;
            break;
          }
          else {
            I.say(p+" could not decide on action.");
            p.actionPoints = 0;
            continue;
          }
        }
      }
      
      if (selected == null) {
        I.say("  Will refresh AP and try other team...");
        for (Person p : team) {
          p.updateOnTurn();
          p.actionPoints = p.maxAP();
        }
        playerTurn = ! playerTurn;
      }
      else {
        I.say("  Will zoom to "+selected);
        zoomTile = selected.location;
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
  
  
  void endScene() {
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
  
  
  float assessCollateral() {
    float sum = 0;
    for (Person p : othersTeam) {
      float damage = p.injury / p.maxHealth();
      if (! p.alive()) damage += 3;
      if (! p.isCriminal()) damage *= 2;
      sum += damage;
    }
    return sum * 0.25f / othersTeam.size();
  }
  
  
  float assessGetaways() {
    float sum = 0, numC = 0;
    for (Person p : othersTeam) if (p.isCriminal()) {
      numC++;
      if (p.conscious() || p.scene != this) sum++;
    }
    if (numC == 0) return 0;
    return sum / numC;
  }
  
  
  void applySiteEffects() {
    boolean success = wasWon();
    float collateral = assessCollateral();
    float getaways   = assessGetaways  ();
    
    site.trust += (1 - getaways) / 10;
    site.crime += getaways       / 10;
    site.trust -= collateral / 10f;
    site.crime -= collateral / 10f;
    
    if (success) {
      site.trust += 0.5f / 10;
      site.crime -= 0.5f / 10;
    }
    else {
      site.trust -= 0.5f / 10;
      site.crime += 0.5f / 10;
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
  Tile zoomTile;
  int zoomX, zoomY;
  
  void renderTo(Surface surface, Graphics2D g) {
    
    //
    //  Update camera information first-
    Person next = playerTeam.first();
    if (next != null && next.location != null) {
      if (zoomTile == null) zoomTile = next.location;
    }
    if (zoomTile != null) {
      zoomX = zoomTile.x * TILE_SIZE;
      zoomY = zoomTile.y * TILE_SIZE;
    }
    zoomX -= surface.getWidth () / 2;
    zoomY -= surface.getHeight() / 2;
    
    //
    //  Then, render any props, persons, or special FX-
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = tiles[c.x][c.y];
      if (t.prop != null && t.prop.origin == t) {
        renderAt(t.x, t.y, t.prop.kind, g);
      }
    }
    for (Person p : persons) if (fogAt(p.location) > 0) {
      renderAt(p.posX, p.posY, p.kind, g);
    }
    for (Person p : persons) if (fogAt(p.location) > 0) {
      
      Color teamColor = Color.GREEN;
      if (p.isHero()) teamColor = Color.BLUE;
      if (p.isCriminal()) teamColor = Color.RED;
      
      float maxHealth = p.maxHealth();
      float tireLevel = 1f - (p.injury / maxHealth);
      float healthLevel = 1f - ((p.injury + p.fatigue) / maxHealth);
      renderAt(p.posX, p.posY + 0.9f, 1, 0.1f, null, Color.BLACK, g);
      renderAt(p.posX, p.posY + 0.9f, tireLevel, 0.1f, null, Color.WHITE, g);
      renderAt(p.posX, p.posY + 0.9f, healthLevel, 0.1f, null, teamColor, g);
      renderString(p.posX, p.posY + 0.5f, p.name, Color.WHITE, g);
    }
    final Action a = currentAction;
    if (a != null && a.progress >= 0) a.used.renderMissile(a, this, g);
    
    //
    //  Then render fog on top of all objects-
    final Color SCALE[] = new Color[10];
    for (int n = 10; n-- > 0;) {
      SCALE[n] = new Color(0, 0, 0, n / 20f);
    }
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      float fogAlpha = 1f - (fog[c.x][c.y] / 100f);
      Color black = SCALE[Nums.clamp((int) (fogAlpha * 10), 10)];
      renderAt(c.x, c.y, 1, 1, null, black, g);
    }
    
    //
    //  If complete, display a summary of the results!
    if (finished()) {
      return;
    }
    
    //
    //  Then determine what tile (and any objects above) are being selected-
    int HT = TILE_SIZE / 2;
    int hoverX = (surface.mouseX + zoomX + HT) / TILE_SIZE;
    int hoverY = (surface.mouseY + zoomY + HT) / TILE_SIZE;
    Tile hoverT = tileAt(hoverX, hoverY);
    
    if (hoverT != null) {
      renderAt(hoverT.x, hoverT.y, 1, 1, world.selectCircle, null, g);
      Object hovered = topObjectAt(hoverT);
      
      Action action = null;
      if (activeAbility != null) action = activeAbility.configAction(
        selected, hoverT, hovered, this, null
      );
      
      if (activeAbility != null && action != null) {
        int costAP = action.used.costAP(action);
        renderString(hoverT.x, hoverT.y - 0.5f, "AP: "+costAP, Color.GREEN, g);
      }
      if (activeAbility != null && action == null) {
        renderString(hoverT.x, hoverT.y - 0.5f, "X", Color.RED, g);
      }
      if (surface.mouseClicked && currentAction == null && action != null) {
        queueNextAction(action);
      }
      else if (surface.mouseClicked) {
        Person pickP = null;
        if (hovered instanceof Person) pickP = (Person) hovered;
        if (pickP != null) selected = pickP;
        else zoomTile = hoverT;
      }
    }
  }
  
  
  String description() {
    final StringBuffer s = new StringBuffer();
    final Person  p = selected;
    final Ability a = activeAbility;
    
    if (finished()) {
      describeEndSummary(s);
      return s.toString();
    }
    
    if (p != null) {
      s.append("\nSelection: "+p.name);
      
      int HP = (int) (p.maxHealth() - (p.injury + p.fatigue));
      s.append("\n  Health: "+HP+"/"+p.maxHealth());
      s.append("\n  AP: "+p.currentAP()+"/"+p.maxAP());
      if (! p.alive) s.append("\n  Dead");
      else if (! p.conscious) s.append("\n  Unconscious");
      
      boolean canCommand =
        currentAction == null && p.actionPoints > 0 && playerTeam.includes(p)
      ;
      if (canCommand) {
        s.append("\n\n  Abilities (Press 1-9):");
        char key = '1';
        for (Ability r : p.abilities) if (! r.passive()) {
          s.append("\n    "+r.name);
          
          boolean canUse = r.minCostAP() <= p.currentAP();
          if (canUse) s.append(" ("+key+") AP: "+r.minCostAP());
          if (world.game.description.isPressed(key) && canUse) {
            beginSelection(p, r);
          }
          key++;
        }
        if (a == null) {
          s.append("\n  Pass Turn (X)");
          if (world.game.description.isPressed('x')) {
            p.actionPoints = 0;
            moveToNextPersonsTurn();
          }
        }
        //  TODO:  Allow zooming to and tabbing through party members.
        else {
          s.append("\n\n"+a.description);
          s.append("\n  Select target");
          s.append("\n  Cancel (X)");
          if (world.game.description.isPressed('x')) {
            beginSelection(p, null);
          }
        }
      }
    }
    
    return s.toString();
  }
  
  
  void describeEndSummary(StringBuffer s) {
    boolean success = wasWon();
    s.append("\nMission ");
    if (success) s.append(" Successful.");
    else s.append(" Failed.");
    
    s.append("\nTeam Status:");
    for (Person p : playerTeam) {
      s.append("\n  "+p.name);
      if (p.scene != this) {
        s.append(" (escaped)");
      }
      else if (! p.alive()) {
        s.append(" (dead)");
      }
      else if (! p.conscious()) {
        s.append(success ? " (unconscious)" : " (captive)");
      }
      else s.append(" (okay)");
    }
    s.append("\nOther Forces:");
    for (Person p : othersTeam) {
      s.append("\n  "+p.name);
      if (p.scene != this) {
        s.append(" (escaped)");
      }
      else if (! p.alive()) {
        s.append(" (dead)");
      }
      else if (! p.conscious()) {
        s.append(success ? " (captive)" : " (unconscious)");
      }
      else s.append(" (okay)");
    }
    
    final String DESC_C[] = {
      "None", "Minimal", "Medium", "Heavy", "Total"
    };
    final String DESC_G[] = {
      "None", "Few", "Some", "Many", "All"
    };
    int colIndex = Nums.clamp((int) (assessCollateral() * 5), 5);
    int getIndex = Nums.clamp((int) (assessGetaways  () * 5), 5);
    s.append("\nCollateral: "+DESC_C[colIndex]);
    s.append("\nGetaways: "  +DESC_G[getIndex]);
    int trustPercent = (int) (site.trust * 100);
    int crimePercent = (int) (site.crime * 100);
    s.append("\nRegional Trust: "+trustPercent+"%");
    s.append("\nRegional Crime: "+crimePercent+"%");
    
    s.append("\n\n  Press X to exit.");
    if (world.game.description.isPressed('x')) {
      endScene();
    }
  }
  
  
  void renderString(float px, float py, String s, Color c, Graphics2D g) {
    int x, y;
    x = (int) ((px - 0.50f) * TILE_SIZE);
    y = (int) ((py + 0.15f) * TILE_SIZE);
    g.setColor(c);
    g.drawString(s, x - zoomX, y - zoomY);
  }
  

  void renderAt(float px, float py, Kind kind, Graphics2D g) {
    renderAt(px, py, kind.wide, kind.high, kind.sprite, null, g);
  }
  
  
  void renderAt(
    float px, float py, float w, float h,
    Image sprite, Color fill, Graphics2D g
  ) {
    int x, y;
    x = (int) ((px - 0.5f) * TILE_SIZE);
    y = (int) ((py - 0.5f) * TILE_SIZE);
    w *= TILE_SIZE;
    h *= TILE_SIZE;
    
    if (sprite != null) {
      g.drawImage(sprite, x - zoomX, y - zoomY, (int) w, (int) h, null);
    }
    if (fill != null) {
      g.setColor(fill);
      g.fillRect(x - zoomX, y - zoomY, (int) w, (int) h);
    }
  }
}


class Tile {
  Scene scene;
  int x, y;
  
  Prop prop;
  Person standing;
  
  Object flag;
  
  
  public String toString() {
    return "Tile at "+x+"|"+y;
  }
}


class Prop {
  Kind kind;
  Tile origin;
  Box2D bounds;
  
  
  public String toString() {
    return kind.name;
  }
}


class Action {
  
  Person acting;
  Tile path[];
  Ability used;
  Object target;
  int timeStart;
  float progress;
}


class MoveSearch extends Search <Tile> {
  
  Person moves;
  Tile dest;
  Tile temp[] = new Tile[8];
  boolean getNear = false;
  
  
  public MoveSearch(Person moves, Tile init, Tile dest) {
    super(init, -1);
    this.moves = moves;
    this.dest = dest;
    getNear = ! canEnter(dest);
  }
  
  
  protected Tile[] adjacent(Tile spot) {
    for (int n : TileConstants.T_INDEX) {
      temp[n] = dest.scene.tileAt(
        spot.x + TileConstants.T_X[n],
        spot.y + TileConstants.T_Y[n]
      );
    }
    return temp;
  }
  
  
  protected boolean endSearch(Tile best) {
    if (getNear) return dest.scene.distance(best, dest) < 2;
    return best == dest;
  }
  
  
  protected boolean canEnter(Tile spot) {
    if (spot.scene.blockedAt(spot)) return false;
    if (spot.standing != null && spot.standing != moves) return false;
    return true;
  }
  
  
  protected float cost(Tile prior, Tile spot) {
    return spot.scene.distance(prior, spot);
  }
  
  
  protected float estimate(Tile spot) {
    return dest.scene.distance(spot, dest);
  }
  
  
  protected void setEntry(Tile spot, Entry flag) {
    spot.flag = flag;
  }
  
  
  protected Entry entryFor(Tile spot) {
    return (Entry) spot.flag;
  }
}









