

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class SceneType extends Index.Entry implements
  TileConstants, Session.Saveable
{
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <SceneType> INDEX = new Index <SceneType> ();
  
  final public static Object
    BORDERS  = "Borders"  ,
    FLOORS   = "Floors"   ,
    DOOR     = "Wall"     ,
    WINDOW   = "Window"   ,
    PROP     = "Prop"     ,
    CHILD    = "Child"    ,
    MIN_WIDE = "Min. Wide",
    MAX_WIDE = "Max. Wide",
    MIN_HIGH = "Min. High",
    MAX_HIGH = "Max. High"
  ;
  
  final String name;
  
  int minWide = -1, maxWide = -1, minHigh = -1, maxHigh = -1;
  PropType borders, door, window, floors;
  PropType props[];
  float propWeights[];
  SceneType kidTypes[] = new SceneType[0];
  
  
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    Batch <PropType > propB  = new Batch();
    Batch <SceneType> childB = new Batch();
    
    for (int i = 0; i < args.length; i += 2) try {
      final Object label = args[i], val = args[i + 1];
      if (label == BORDERS ) borders =  (PropType ) val;
      if (label == FLOORS  ) floors  =  (PropType ) val;
      if (label == DOOR    ) door    =  (PropType ) val;
      if (label == WINDOW  ) window  =  (PropType ) val;
      if (label == PROP    ) propB .add((PropType ) val);
      if (label == CHILD   ) childB.add((SceneType) val);
      if (label == MIN_WIDE) minWide =  (Integer  ) val;
      if (label == MAX_WIDE) maxWide =  (Integer  ) val;
      if (label == MIN_HIGH) minHigh =  (Integer  ) val;
      if (label == MAX_HIGH) maxHigh =  (Integer  ) val;
    } catch (Exception e) { I.report(e); }
    
    props    = propB .toArray(PropType.class);
    kidTypes = childB.toArray(SceneType.class);
  }
  
  
  public static SceneType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(World world) {
    int wide = (int) Rand.range(minWide, maxWide);
    int high = (int) Rand.range(minHigh, maxHigh);
    return generateScene(world, wide, high, false);
  }
  
  
  public Scene generateScene(
    World world, int prefWide, int prefHigh, boolean testing
  ) {
    int     wide  = clampSize(prefWide, minWide, maxWide);
    int     high  = clampSize(prefHigh, minHigh, maxHigh);
    Scene   scene = new Scene(world, wide, high);
    Scenery gen   = generateScenery(world, wide, high, testing);
    scene.setupScene(testing);
    applyScenery(scene, gen, 0, 0, N, testing);
    return scene;
  }
  
  
  private int clampSize(int prefSize, int minSize, int maxSize) {
    int size = prefSize;
    if (maxSize > 0 && maxSize < size) size = maxSize;
    if (minSize > 0 && minSize > size) size = minSize;
    return size;
  }
  
  
  public abstract Scenery generateScenery(
    World world, boolean testing
  );
  
  
  public abstract Scenery generateScenery(
    World world, int prefWide, int prefHigh, boolean testing
  );
  
  
  
  /**  Helper methods for scene-composition:
    */
  protected void rotateCoord(Scenery gen, Coord c, int facing) {
    int offH = gen.wide - 1, offV = gen.high - 1, x, y;
    if (facing == N) {
      x = c.x;
      y = c.y;
    }
    else if (facing == E) {
      x = offV - c.y;
      y = c.x;
    }
    else if (facing == W) {
      x = c.y;
      y = offH - c.x;
    }
    else {
      x = offH - c.x;
      y = offV - c.y;
    }
    c.x = x;
    c.y = y;
  }
  
  
  Box2D borderBounds(
    Scenery within, Scenery gen, int offX, int offY, int facing, int resolution
  ) {
    Box2D bound = null;
    int wideB = Nums.round(gen.wide, resolution, true) - 1;
    int highB = Nums.round(gen.high, resolution, true) - 1;
    
    for (Coord c : Visit.grid(0, 0, 2, 2, 1)) {
      c.x *= wideB;
      c.y *= highB;
      rotateCoord(gen, c, facing);
      c.x += offX;
      c.y += offY;
      
      if (within.tileAt(c.x, c.y) == null) return null;
      
      if (bound == null) bound = new Box2D(c.x, c.y, 0, 0);
      else bound.include(c.x, c.y, 0);
    }
    
    bound.incHigh(1);
    bound.incWide(1);
    return bound;
  }
  
  
  boolean checkBordering(
    Scenery within, Scenery gen, int offX, int offY, int facing, int resolution
  ) {
    Coord temp = new Coord();
    if (borderBounds(within, gen, offX, offY, facing, resolution) == null) {
      return false;
    }
    
    for (Coord c : Visit.perimeter(0, 0, gen.wide, gen.high)) {
      temp.setTo(c);
      rotateCoord(gen, temp, facing);
      int tx = temp.x + offX, ty = temp.y + offY, gx = c.x, gy = c.y, dir = 0;
      if (c.x < 0          ) { gx++; dir = W; }
      if (c.y < 0          ) { gy++; dir = S; }
      if (c.x >= resolution) { gx--; dir = E; }
      if (c.y >= resolution) { gy--; dir = N; }
      if (c.x != gx && c.y != gy) {
        continue;
      }
      //
      //  Check to ensure that no doors or windows are blocked.
      //
      //  TODO:  In future, you may want to implement a more jigsaw-esque
      //  approach, depending on the wall-types specs for a grid-unit.
      boolean isDoor = gen.tileAt(gx, gy).hasWall(dir);
      Tile    at     = within.tileAt(tx, ty);
      boolean blockT = at == null ? true  : (at.blocked() || at.opaque());
      if (isDoor && blockT) return false;
    }
    
    return true;
  }
  
  
  public void applyScenery(
    Scenery within, Scenery gen, int offX, int offY, int facing,
    boolean testing
  ) {
    Coord temp = new Coord();
    for (Prop prop : gen.props()) {
      Tile at = prop.origin();
      if (at == null) continue;
      
      temp.x = at.x;
      temp.y = at.y;
      rotateCoord(gen, temp, facing);
      int newFace = (prop.facing + facing) % 8;
      prop.exitScene();
      prop.enterScene(within, temp.x + offX, temp.y + offY, newFace);
    }
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}










