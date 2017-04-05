

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
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(scene);
    s.saveInt(x);
    s.saveInt(y);
    s.saveObjects(inside );
    s.saveObjects(persons);
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
  final static int OP_VAL = 0, BL_VAL = 1;
  final static int CHECK_DIRS[] = { N, E, S, W, CENTRE };
  final static float NO_LINE = Float.NEGATIVE_INFINITY;
  
  static int wallMaskX(int x, int dir) {
    return (x * 2) + 1 + T_X[dir];
  }
  
  static int wallMaskY(int y, int dir) {
    return (y * 2) + 1 + T_Y[dir];
  }
  
  void setFills(int dir, Prop prop) {
    scene.fills[wallMaskX(x, dir)][wallMaskY(y, dir)] = prop;
  }
  
  Prop filling(int dir) {
    return scene.fills[wallMaskX(x, dir)][wallMaskY(y, dir)];
  }
  
  public int blockageVal(int dir) {
    Prop fills = filling(dir);
    return fills == null ? Kind.BLOCK_NONE : fills.kind().blockLevel();
  }
  
  public int opacityVal(int dir) {
    Prop fills = filling(dir);
    return fills == null ? 0 : (fills.kind().blockSight() ? 1 : 0);
  }
  
  private int wallVal(int dir, int valID) {
    switch (valID) {
      case(OP_VAL): return opacityVal (dir);
      case(BL_VAL): return blockageVal(dir);
    }
    return 0;
  }
  
  
  
  public Tile[] tilesAdjacent(Tile temp[]) {
    for (int n : T_ADJACENT) {
      if (blockageVal(n) == Kind.BLOCK_FULL) temp[n] = null;
      else temp[n] = scene.tileAt(x + T_X[n], y + T_Y[n]);
    }
    
    int dirA, dirB, block;
    for (int n : T_DIAGONAL) {
      temp[n] = null;
      Tile d = scene.tileAt(x + T_X[n], y + T_Y[n]);
      if (d == null) continue;
      
      dirA = (n + 1) % 8;
      dirB = (n + 7) % 8;
      block = Nums.max(pathBlockVal(dirA), pathBlockVal(dirB));
      if (block == Kind.BLOCK_FULL) continue;
      
      dirA = (n + 3) % 8;
      dirB = (n + 5) % 8;
      block = Nums.max(d.pathBlockVal(dirA), d.pathBlockVal(dirB));
      if (block == Kind.BLOCK_FULL) continue;
      
      temp[n] = d;
    }
    return temp;
  }
  
  
  private int pathBlockVal(int dir) {
    Tile d = scene.tileAt(x + T_X[dir], y + T_Y[dir]);
    int block = blockageVal(dir);
    if (d == null) return block;
    return Nums.max(block, d.blockageVal(CENTRE));
  }
  
  
  public int coverLevel(int dir) {
    Tile other = scene.tileAt(x + T_X[dir], y + T_Y[dir]);
    int block  = blockageVal(dir);
    int opaque = opacityVal (dir);
    if (other != null) {
      block  = Nums.max(block , other.blockageVal(CENTRE));
      opaque = Nums.max(opaque, other.opacityVal (CENTRE));
    }
    if (opaque == 0 && block > 0) block--;
    return block;
  }
  
  
  public int coverLevel(Vec2D origin, Vec2D line, boolean report) {
    int block  = wallVal(origin, line, BL_VAL, report);
    int opaque = wallVal(origin, line, OP_VAL, report);
    if (opaque == 0 && block > 0) block--;
    return block;
  }
  
  
  public boolean blocksSight(Vec2D origin, Vec2D line, boolean report) {
    return wallVal(origin, line, OP_VAL, report) > 0;
  }
  
  
  private int wallVal(
    Vec2D origin, Vec2D line, int valID, boolean report
  ) {
    int maxVal = 0, fillVal = 0;
    switch (valID) {
      case(OP_VAL): fillVal = blocked() ? Kind.BLOCK_FULL : 0;
      case(BL_VAL): fillVal = opaque() ? 1 : 0;
    }
    //
    //  TODO:  North/south/east/west values aren't being handled consistently
    //         here.  Address this in the TileConstants class and follow
    //         through to all affected code.
    float nortX = solveX(origin, line, y + 0);
    float soutX = solveX(origin, line, y + 1);
    float westY = solveY(origin, line, x + 0);
    float eastY = solveY(origin, line, x + 1);
    
    if (nortX >= 0) {
      if (report) {
        I.say("    Intersects Y="+(y + 0)+" at "+nortX);
        I.add("  Filling is: "+filling(W));
      }
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(W, valID));
    }
    if (soutX >= 0) {
      if (report) {
        I.say("    Intersects Y="+(y + 1)+" at "+soutX);
        I.add("  Filling is: "+filling(E));
      }
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(E, valID));
    }
    if (westY >= 0) {
      if (report) {
        I.say("    Intersects X="+(x + 0)+" at "+westY);
        I.add("  Filling is: "+filling(S));
      }
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(S, valID));
    }
    if (eastY >= 0) {
      if (report) {
        I.say("    Intersects X="+(x + 1)+" at "+eastY);
        I.add("  Filling is: "+filling(N));
      }
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(N, valID));
    }
    
    return maxVal;
  }
  
  
  private float solveX(Vec2D o, Vec2D l, float atY) {
    if (! checkRange(o.y, l.y, atY)) return NO_LINE;
    atY -= o.y;
    atY /= l.y;
    float solveX = o.x + (l.x * atY);
    if (solveX < x || solveX > x + 1) return NO_LINE;
    return solveX;
  }
  
  
  private float solveY(Vec2D o, Vec2D l, float atX) {
    if (! checkRange(o.x, l.x, atX)) return NO_LINE;
    atX -= o.x;
    atX /= l.x;
    float solveY = o.y + (l.y * atX);
    if (solveY < y || solveY > y + 1) return NO_LINE;
    return solveY;
  }
  
  
  private boolean checkRange(float o, float l, float at) {
    if (l < 0) return o + l <= at && at <= o;
    else return o <= at && at <= o + l;
  }
  
  
  public boolean blocked() {
    return blockageVal(CENTRE) == Kind.BLOCK_FULL;
  }
  
  
  public boolean opaque() {
    return opacityVal(CENTRE) > 0;
  }
  
  
  public boolean occupied() {
    if (persons.size() == 0) return false;
    for (Person p : persons) if (p.health.conscious()) return true;
    return false;
  }
  
  
  public boolean hasWall(int dir) {
    int facing = (dir + 2) % 8;
    for (Element e : inside()) if (e.isProp()) {
      Prop p = (Prop) e;
      if (p.kind().thin() && p.facing() == facing) return true;
    }
    return false;
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
    return "T:"+x+"|"+y;
  }
  
  
  public static void printWallsMask(Scene scene) {
    int size = scene.size(), span = (size * 2) + 1;
    I.say("\nPRINTING WALLS FOR SCENE");
    
    for (int y = 0; y < span; y++) {
      I.add("\n  ");
      
      for (int x = 0; x < span; x++) {
        boolean bordY = y % 2 == 0;
        boolean bordX = x % 2 == 0;
        Prop fills = scene.fills[x][y];
        int val = fills == null ? 0 : fills.blockLevel();
        
        if (val > 0) {
          if (! (bordY || bordX)) I.add("*");
          else if (bordY) I.add("_");
          else I.add("|");
        }
        else {
          if (bordX && bordY) I.add(".");
          else I.add(" ");
        }
      }
    }
  }
}







