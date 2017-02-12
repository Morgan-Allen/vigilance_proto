

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.util.TileConstants.*;



public class Tile implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public Scene scene;
  final public int x, y;
  
  private Stack <Element> inside  = new Stack();
  private Stack <Person > persons = new Stack();
  private boolean blocked, opaque;
  
  Object flag;
  
  
  Tile(Scene s, int x, int y) {
    this.scene = s;
    this.x = x;
    this.y = y;
  }
  
  
  public Tile(Session s) throws Exception {
    s.cacheInstance(this);
    scene = (Scene) s.loadObject();
    x     = s.loadInt();
    y     = s.loadInt();
    s.loadObjects(inside );
    s.loadObjects(persons);
    blocked = s.loadBool();
    opaque  = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(scene);
    s.saveInt(x);
    s.saveInt(y);
    s.saveObjects(inside );
    s.saveObjects(persons);
    s.saveBool(blocked);
    s.saveBool(opaque );
  }
  
  
  
  /**  Public query methods-
    */
  public Series <Element> inside() {
    return inside;
  }
  
  
  public Series <Person> persons() {
    return persons;
  }
  
  
  public Element topInside() {
    return inside.last();
  }
  
  
  
  /**  Methods related to wall-blockage and opacity-
    */
  //  TODO:  Try and put this code somewhere closer to where the wall and
  //  opacity arrays are actually declared within the Scene class, so it's
  //  easier to relate the math?
  
  static int wallMaskX(int x, int dir) {
    return (x * 2) + 1 + T_X[dir];
  }
  static int wallMaskY(int y, int dir) {
    return (y * 2) + 1 + T_Y[dir];
  }
  
  final static int CHECK_DIRS[] = { N, E, S, W, CENTRE };
  
  int wallMaskVal(int dir) {
    return scene.wallM[wallMaskX(x, dir)][wallMaskY(y, dir)];
  }
  
  int opacityVal(int dir) {
    return scene.opacM[wallMaskX(x, dir)][wallMaskY(y, dir)];
  }
  
  void setWallMask(int dir, byte val) {
    scene.wallM[wallMaskX(x, dir)][wallMaskY(y, dir)] = val;
  }
  
  void setOpacity(int dir, byte val) {
    scene.opacM[wallMaskX(x, dir)][wallMaskY(y, dir)] = val;
  }

  
  void wipePathing() {
    for (int dir : T_ADJACENT) {
      setWallMask(dir, (byte) 0);
      setOpacity (dir, (byte) 0);
    }
  }
  
  
  void updatePathing() {
    blocked = false;
    opaque  = false;
    
    for (Element e : inside()) if (e.isProp()) {
      final Prop prop = (Prop) e;
      final Tile o = prop.origin();
      
      for (int dir : CHECK_DIRS) {
        final boolean occupies = prop.occupies(x - o.x, y - o.y, dir);
        final int
          blocks  = occupies ? prop.blockLevel() : 0,
          opacity = (prop.blockSight() && occupies) ? 1 : 0
        ;
        
        if (dir == CENTRE) {
          if (blocks == Kind.BLOCK_FULL) blocked = true;
          if (opacity > 0              ) opaque  = true;
        }
        else {
          setWallMask(dir, (byte) Nums.max(blocks , wallMaskVal(dir)));
          setOpacity (dir, (byte) Nums.max(opacity, opacityVal (dir)));
        }
      }
    }
  }
  
  
  public Tile[] tilesAdjacent(Tile temp[]) {
    for (int n : T_ADJACENT) {
      if (wallMaskVal(n) == Kind.BLOCK_FULL) temp[n] = null;
      else temp[n] = scene.tileAt(x + T_X[n], y + T_Y[n]);
    }
    for (int n : T_DIAGONAL) {
      Tile a = temp[(n + 1) % 8], b = temp[(n + 7) % 8];
      if (a == null || b == null) temp[n] = null;
      else temp[n] = scene.tileAt(x + T_X[n], y + T_Y[n]);
    }
    return temp;
  }
  
  
  public int coverLevel(int dir) {
    int block = wallMaskVal(dir), opaque = opacityVal(dir);
    if (opaque == 0) block--;
    return block;
  }
  
  
  public boolean blocksSight(Vec2D origin, Vec2D line, boolean report) {
    //
    //  TODO:  North/south/east/west values aren't being handled consistently
    //         here.  Address this in the TileConstants class and follow
    //         through to all affected code.
    //  
    //  TODO:  Move this into the Scene class?  Or move the LoS methods in
    //  here?
    float nortX = solveX(origin, line, y + 0);
    if (nortX >= 0 && (opaque || opacityVal(W) > 0)) {
      if (report) I.say("    Intersects Y="+(y + 0)+" at "+nortX);
      return true;
    }
    float soutX = solveX(origin, line, y + 1);
    if (soutX >= 0 && (opaque || opacityVal(E) > 0)) {
      if (report) I.say("    Intersects Y="+(y + 1)+" at "+soutX);
      return true;
    }
    float eastY = solveY(origin, line, x + 0);
    if (eastY >= 0 && (opaque || opacityVal(S) > 0)) {
      if (report) I.say("    Intersects X="+(x + 0)+" at "+eastY);
      return true;
    }
    float westY = solveY(origin, line, x + 1);
    if (westY >= 0 && (opaque || opacityVal(N) > 0)) {
      if (report) I.say("    Intersects X="+(x + 1)+" at "+westY);
      return true;
    }
    return false;
  }
  
  
  private float solveX(Vec2D o, Vec2D l, float atY) {
    if (! checkRange(o.y, l.y, atY)) return Float.NEGATIVE_INFINITY;
    atY -= o.y;
    atY /= l.y;
    float solveX = o.x + (l.x * atY);
    if (solveX < x || solveX > x + 1) return Float.NEGATIVE_INFINITY;
    return solveX;
  }
  
  
  private float solveY(Vec2D o, Vec2D l, float atX) {
    if (! checkRange(o.x, l.x, atX)) return Float.NEGATIVE_INFINITY;
    atX -= o.x;
    atX /= l.x;
    float solveY = o.y + (l.y * atX);
    if (solveY < y || solveY > y + 1) return Float.NEGATIVE_INFINITY;
    return solveY;
  }
  
  
  private boolean checkRange(float o, float l, float at) {
    if (l < 0) return o + l <= at && at <= o;
    else return o <= at && at <= o + l;
  }
  
  
  public boolean blocked() {
    return blocked;
  }
  
  
  public boolean opaque() {
    return opaque;
  }
  
  
  public boolean occupied() {
    return persons.size() > 0;
  }
  
  
  
  /**  Modifying occupancy-
    */
  public void setInside(Element p, boolean is) {
    if (is) inside.include(p);
    else    inside.remove (p);
    if (p.isPerson()) {
      if (is) persons.include((Person) p);
      else    persons.remove ((Person) p);
    }
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Tile at "+x+"|"+y;
  }
  
  
  public static void printWallsMask(Scene scene) {
    int size = scene.size(), span = (size * 2) + 1;
    I.say("\nPRINTING WALLS FOR SCENE");

    for (int y = 0; y < span; y++) {
      I.add("\n  ");
      boolean centY = y % 2 == 0;
      
      for (int x = 0; x < span; x++) {
        boolean centX = x % 2 == 0;
        byte val = scene.wallM[x][y];
        
        if (val > 0) {
          if (centY) I.add("_");
          else I.add("|");
        }
        else {
          if (centX && centY) I.add(".");
          else I.add(" ");
        }
      }
    }
  }
}
















