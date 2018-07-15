

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.Scenery.*;
import proto.util.*;



public abstract class SceneType extends Index.Entry implements
  TileConstants, Session.Saveable
{
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <SceneType> INDEX = new Index <SceneType> ();
  
  final public static Object
    BORDERS    = "Borders"  ,
    FLOORS     = "Floors"   ,
    DOOR       = "Wall"     ,
    WINDOW     = "Window"   ,
    PROP       = "Prop"     ,
    CHILD      = "Child"    ,
    MIN_WIDE   = "Min. Wide",
    MAX_WIDE   = "Max. Wide",
    MIN_HIGH   = "Min. High",
    MAX_HIGH   = "Max. High",
    RESOLUTION = "Resolution"
  ;
  
  final String name;
  
  int minWide = -1, maxWide = -1, minHigh = -1, maxHigh = -1;
  int resolution = 8;
  
  PropType borders, door, window, floors;
  PropType props[];
  float propWeights[];
  SceneType kidTypes[] = new SceneType[0];
  
  Object cornering[] = SceneTypeUnits.INTERIOR;
  boolean exterior = false, entrance = false;
  
  
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    Batch <PropType > propB  = new Batch();
    Batch <SceneType> childB = new Batch();
    
    for (int i = 0; i < args.length; i += 2) try {
      final Object label = args[i], val = args[i + 1];
      if (label == BORDERS   ) borders    =  (PropType ) val;
      if (label == FLOORS    ) floors     =  (PropType ) val;
      if (label == DOOR      ) door       =  (PropType ) val;
      if (label == WINDOW    ) window     =  (PropType ) val;
      if (label == PROP      ) propB    .add((PropType ) val);
      if (label == CHILD     ) childB   .add((SceneType) val);
      if (label == MIN_WIDE  ) minWide    =  (Integer  ) val;
      if (label == MAX_WIDE  ) maxWide    =  (Integer  ) val;
      if (label == MIN_HIGH  ) minHigh    =  (Integer  ) val;
      if (label == MAX_HIGH  ) maxHigh    =  (Integer  ) val;
      if (label == RESOLUTION) resolution =  (Integer  ) val;
    } catch (Exception e) { I.report(e); }
    
    props    = propB .toArray(PropType.class);
    kidTypes = childB.toArray(SceneType.class);
  }
  
  
  public SceneType attachUnitParameters(
    Object[] cornering, boolean exterior, boolean entrance
  ) {
    this.cornering = cornering;
    this.exterior  = exterior ;
    this.entrance  = entrance ;
    return this;
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
  
  
  public int resolution() {
    return resolution;
  }
  
  
  public Scene generateScene(
    World world, int prefWide, int prefHigh, boolean testing
  ) {
    //  TODO:  Clamping apparently makes no effort at ensuring that resolution
    //  requirements are met.
    
    int     wide  = clampSize(prefWide, minWide, maxWide);
    int     high  = clampSize(prefHigh, minHigh, maxHigh);
    Scene   scene = new Scene(this, world, wide, high, testing);
    Scenery gen   = generateScenery(world, wide, high, testing);
    //
    //  Copy over all wings, rooms, and scenery:
    for (Scenery.Room room : gen.rooms) {
      Box2D bounds = borderBounds(scene, gen, 0, 0, N, room.wide);
      scene.recordRoom(room, bounds);
    }
    
    scene.setupWingsGrid(gen.wings());
    applyScenery(scene, gen, 0, 0, N, testing);
    return scene;
  }
  
  
  private int clampSize(int prefSize, int minSize, int maxSize) {
    int size = prefSize;
    size = (int) Nums.round(size, resolution, true);
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
  
  
  public Box2D borderBounds(
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
  

  public int borderingCheck(
    Scenery within, Scenery gen, int offX, int offY, int facing, int resolution
  ) {
    if (borderBounds(within, gen, offX, offY, facing, resolution) == null) {
      return -1;
    }
    
    int entranceCheck = entranceCheck(within, offX, offY, facing, false);
    if (entranceCheck == -1) return -1;
    
    Coord c = new Coord();
    int cornerID = 0;
    boolean allMatch = true;
    
    //I.say("\nChecking bordering...");
    
    for (int y = -1; y < 2; y++) for (int x = -1; x < 2; x++) {
      int needID = (Integer) cornering[cornerID++];
      if (needID == SceneTypeUnits.X) continue;
      
      c.x = (x * gen.wide) + (resolution / 2);
      c.y = (y * gen.high) + (resolution / 2);
      rotateCoord(gen, c, facing);
      c.x += offX;
      c.y += offY;
      
      Box2D wing = within.wingUnder(c.x, c.y);
      int wingID = wing == null ? 0 : 1;
      if (exterior) wingID = 1 - wingID;
      
      //I.say("  "+x+"|"+y+" Need/Got: "+needID+"/"+wingID);
      if (wingID != needID) allMatch = false; //I.add(" wrong!"); }
    }
    
    return allMatch ? entranceCheck : -1;
  }
  
  
  public void applyScenery(
    Scenery within, Scenery gen, int offX, int offY, int facing,
    boolean testing
  ) {
    //
    //  Set up parameters for the subunit, and copy over any interior props:
    gen.setUnitParameters(offX, offY, facing);
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
    //
    //  And finally, check for the provision of an entrance within the scene:
    entranceCheck(within, offX, offY, facing, true);
  }
  
  
  private int entranceCheck(
    Scenery g, int offX, int offY, int face, boolean createEntrance
  ) {
    if (exterior || ! entrance) return 0;
    
    int gx  = (offX + 1) / g.type.resolution;
    int gy  = (offY + 1) / g.type.resolution;
    int dir = (face + 6) % 8;  //  NOTE:  Another hack, blech.
    Island other = g.islandUnderGrid(gx + T_X[dir], gy + T_Y[dir]);
    Island under = g.islandUnderGrid(gx           , gy           );
    
    if (under == null || other == null || other == under) return -1;
    if (g.hasBridgeBetween(under, other)                ) return  0;
    
    if (createEntrance) {
      Room room = g.roomUnderGrid(gx, gy);
      g.setAsEntrance(room, under);
      g.setAsEntrance(room, other);
    }
    
    return 1;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}



