

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
  
  final static Coord WALL_MASK_COORDS[] = {
    new Coord(0, 1),
    new Coord(1, 1),
    new Coord(0, 0),
    new Coord(1, 0)
  };
  int wallMaskX(int dir) { return (x * 1) + WALL_MASK_COORDS[dir / 2].x; }
  int wallMaskY(int dir) { return (y * 2) + WALL_MASK_COORDS[dir / 2].y; }
  
  int wallMaskVal(int dir) {
    return scene.wallM[wallMaskX(dir)][wallMaskY(dir)];
  }
  
  int opacityVal(int dir) {
    return scene.opacM[wallMaskX(dir)][wallMaskY(dir)];
  }
  
  void setWallMask(int dir, byte val) {
    scene.wallM[wallMaskX(dir)][wallMaskY(dir)] = val;
  }
  
  void setOpacity(int dir, byte val) {
    scene.opacM[wallMaskX(dir)][wallMaskY(dir)] = val;
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
    for (int dir : T_ON_CENTRE) {
      if (dir != CENTRE && dir % 2 == 1) continue;
      Tile near = scene.tileAt(x + T_X[dir], y + T_Y[dir]);
      
      if (near != null) for (Element e : near.inside()) if (e.isProp()) {
        final Prop prop = (Prop) e;
        final Tile o = prop.origin();
        final int
          blocks  = prop.blockageAtOffset(dir, near.x - o.x, near.y - o.y),
          opacity = prop.blockSight() ? 1 : 0
        ;
        setWallMask(dir, (byte) Nums.max(blocks , wallMaskVal(dir)));
        setOpacity (dir, (byte) Nums.max(opacity, opacityVal (dir)));
        
        if (dir == CENTRE && blocks == Kind.BLOCK_FULL) blocked = true;
        if (dir == CENTRE && opacity > 0              ) opaque  = true;
      }
    }
  }
  
  
  public Tile[] tilesAdjacent(Tile temp[]) {
    for (int n : T_ADJACENT) {
      if (wallMaskVal(n) == Kind.BLOCK_FULL) continue;
      temp[n] = scene.tileAt(x + T_X[n], y + T_Y[n]);
    }
    for (int n : T_DIAGONAL) {
      Tile a = temp[(n + 1) % 8], b = temp[(n + 7) % 8];
      if (a == null || b == null) temp[n] = null;
      else temp[n] = scene.tileAt(x + T_X[n], y + T_Y[n]);
    }
    return temp;
  }
  
  
  boolean blocksSight(Vec2D origin, Vec2D line) {
    //
    //  TODO:  You have to check for neg-inf values below!
    float eastY = solveY(origin, line, x), westY = solveY(origin, line, x + 1);
    float nortX = solveX(origin, line, y), soutX = solveX(origin, line, y + 1);
    if (opacityVal(N) > 0 && nortX >= x && nortX <= x + 1) return true;
    if (opacityVal(S) > 0 && soutX >= x && soutX <= x + 1) return true;
    if (opacityVal(W) > 0 && westY >= y && westY >= y + 1) return true;
    if (opacityVal(E) > 0 && eastY >= y && eastY >= y + 1) return true;
    return false;
  }
  
  
  private float solveX(Vec2D o, Vec2D l, float atY) {
    if (o.y < atY && l.y <= 0) return Float.NEGATIVE_INFINITY;
    atY -= o.y;
    atY /= l.y;
    return o.x + (l.x * atY);
  }
  
  
  private float solveY(Vec2D o, Vec2D l, float atX) {
    if (o.x < atX && l.x <= 0) return Float.NEGATIVE_INFINITY;
    atX -= o.x;
    atX /= l.x;
    return o.y + (l.y * atX);
  }
  
  
  public boolean blocked() {
    return blocked;
  }
  
  
  public boolean opaque() {
    return opaque;
  }
  
  
  
  /**  Modifying state-
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
}



