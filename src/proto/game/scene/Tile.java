

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
  final static int OP_VAL = 0, BL_VAL = 1, CV_VAL = 2;
  final static int CHECK_DIRS[] = { N, E, S, W, CENTRE };
  
  static int wallMaskX(int x, int dir) {
    return (x * 2) + 1 + T_X[dir];
  }
  
  static int wallMaskY(int y, int dir) {
    return (y * 2) + 1 + T_Y[dir];
  }
  
  void setBlockage(int dir, byte val) {
    scene.wallM[wallMaskX(x, dir)][wallMaskY(y, dir)] = val;
  }
  
  void setOpacity(int dir, byte val) {
    scene.opacM[wallMaskX(x, dir)][wallMaskY(y, dir)] = val;
  }
  
  public int blockageVal(int dir) {
    return scene.wallM[wallMaskX(x, dir)][wallMaskY(y, dir)];
  }
  
  public int opacityVal(int dir) {
    return scene.opacM[wallMaskX(x, dir)][wallMaskY(y, dir)];
  }
  
  public int coverVal(int dir) {
    int block = blockageVal(dir), opaque = opacityVal(dir);
    if (opaque == 0 && block > 0) block--;
    return block;
  }
  
  int wallVal(int dir, int valID) {
    switch (valID) {
      case(OP_VAL): return opacityVal (dir);
      case(BL_VAL): return blockageVal(dir);
      case(CV_VAL): return coverVal   (dir);
    }
    return 0;
  }

  
  void wipePathing() {
    for (int dir : T_ADJACENT) {
      setBlockage(dir, (byte) 0);
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
          setBlockage(dir, (byte) Nums.max(blocks , blockageVal(dir)));
          setOpacity (dir, (byte) Nums.max(opacity, opacityVal (dir)));
        }
      }
    }
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
      block = Nums.max(blockageVal(dirA), blockageVal(dirB));
      if (block == Kind.BLOCK_FULL) continue;
      
      dirA = (n + 3) % 8;
      dirB = (n + 5) % 8;
      block = Nums.max(d.blockageVal(dirA), d.blockageVal(dirB));
      if (block == Kind.BLOCK_FULL) continue;
      
      temp[n] = d;
    }
    return temp;
  }
  
  
  public int coverLevel(Vec2D origin, Vec2D line, boolean report) {
    return wallVal(origin, line, CV_VAL, report);
  }
  
  
  public boolean blocksSight(Vec2D origin, Vec2D line, boolean report) {
    return wallVal(origin, line, OP_VAL, report) > 0;
  }
  
  
  private int wallVal(
    Vec2D origin, Vec2D line, int valID, boolean report
  ) {
    int maxVal = 0, fillVal = 0;
    switch (valID) {
      case(OP_VAL): fillVal = blocked ? Kind.BLOCK_FULL : 0;
      case(BL_VAL): fillVal = opaque ? 1 : 0;
      case(CV_VAL): fillVal = 0;
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
      if (report) I.say("    Intersects Y="+(y + 0)+" at "+nortX);
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(W, valID));
    }
    if (soutX >= 0) {
      if (report) I.say("    Intersects Y="+(y + 1)+" at "+soutX);
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(E, valID));
    }
    if (westY >= 0) {
      if (report) I.say("    Intersects X="+(x + 0)+" at "+westY);
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(S, valID));
    }
    if (eastY >= 0) {
      if (report) I.say("    Intersects X="+(x + 1)+" at "+eastY);
      maxVal = Nums.max(maxVal, fillVal);
      maxVal = Nums.max(maxVal, wallVal(N, valID));
    }
    
    return maxVal;
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
















