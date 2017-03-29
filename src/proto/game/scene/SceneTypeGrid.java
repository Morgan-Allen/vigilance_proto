

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.scene.SceneGen.*;



public class SceneTypeGrid extends SceneType {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    PRIORITY_HIGH   = 4,
    PRIORITY_MEDIUM = 2,
    PRIORITY_LOW    = 0,
    WALL_NONE     = 0,
    WALL_EXTERIOR = 1,
    WALL_INTERIOR = 2
  ;
  
  static class SceneGenGrid extends SceneGen {
    Room areaMarkup[][];
  }
  
  public static class GridUnit {
    int ID;
    SceneTypeFixed type;
    int wallType = WALL_EXTERIOR;
    int priority, percent, minCount, maxCount;
  }
  
  final int resolution;
  final GridUnit units[];
  
  
  public SceneTypeGrid(
    String name, String ID,
    int resolution, int maxUnitSize,
    PropType wallType, PropType doorType, PropType windowType,
    PropType floorType, GridUnit... units
  ) {
    super(
      name, ID,
      MIN_SIZE, resolution * 2,
      MAX_SIZE, (maxUnitSize > 1 ? (resolution * maxUnitSize) : -1)
    );
    this.resolution = resolution;
    this.units      = units;
    this.borders    = wallType;
    this.door       = doorType;
    this.window     = windowType;
    this.floors     = floorType;
    int unitID = 0;
    for (GridUnit unit : units) unit.ID = unitID++;
  }
  
  
  
  /**  Specifying sub-units for placement within the grid-
    */
  public static GridUnit unit(
    SceneTypeFixed type, int wallType,
    int priority, int percent, int minCount, int maxCount
  ) {
    GridUnit unit = new GridUnit();
    unit.type     = type    ;
    unit.wallType = wallType;
    unit.priority = priority;
    unit.percent  = percent ;
    unit.minCount = minCount;
    unit.maxCount = maxCount;
    return unit;
  }
  
  
  public static GridUnit percentUnit(
    SceneTypeFixed type, int wallType, int percent
  ) {
    return unit(type, wallType, PRIORITY_MEDIUM, percent, -1, -1);
  }
  
  
  public static GridUnit numberUnit(
    SceneTypeFixed type, int wallType, int number
  ) {
    return unit(type, wallType, PRIORITY_HIGH, -1, number, number);
  }
  
  
  public static GridUnit numberOrPercentUnit(
    SceneTypeFixed type, int wallType, int percent, int number
  ) {
    return unit(type, wallType, PRIORITY_HIGH, percent, number, -1);
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(World world, int size, boolean forTesting) {
    I.say("GENERATING GRID SCENE "+this);
    
    final int gridSize = size / resolution;
    size = (gridSize * resolution) + 4;
    
    Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 2, 2, N, gridSize * resolution, forTesting);
    return scene;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int size, boolean forTesting
  ) {
    int gridSize = size / resolution;
    SceneGenGrid gen = new SceneGenGrid();
    gen.scene      = scene;
    gen.gridSize   = gridSize;
    gen.offX       = offX;
    gen.offY       = offY;
    gen.limit      = size;
    gen.areaMarkup = new Room[size][size];
    
    populateWithAreas(scene, gen);
    insertWallsAndDoors(scene, gen);
  }
  
  
  void populateWithAreas(Scene scene, SceneGenGrid g) {
    int counts[] = new int[units.length];
    
    //  TODO:  You need to select optimal facings as well- and iterate over
    //  types before you iterate over locations.
    
    for (Coord c : Visit.grid(0, 0, g.gridSize, g.gridSize, 1)) {
      int atX = g.offX + (c.x * resolution);
      int atY = g.offY + (c.y * resolution);
      
      Pick <GridUnit> pick = new Pick();
      for (GridUnit unit : units) {
        int count = counts[unit.ID];
        int percent = (count * 100) / (g.gridSize * g.gridSize);
        
        if (unit.maxCount > 0 && count   >= unit.maxCount) continue;
        if (unit.percent  > 0 && percent >= unit.percent ) continue;
        if (! unit.type.checkBordering(scene, atX, atY, resolution)) continue;
        
        float rating = unit.priority * 1f / PRIORITY_MEDIUM;
        rating += Nums.max(0, unit.minCount - count);
        rating += Rand.num() / 2;
        pick.compare(unit, rating);
      }
      GridUnit picked = pick.result();
      
      if (picked == null) continue;
      I.say("PICKED GRID UNIT "+picked.type+" AT "+atX+" "+atY);
      int pickFace = T_ADJACENT[Rand.index(4)];
      picked.type.applyToScene(scene, atX, atY, pickFace, resolution, g.verbose);
      //
      //  
      Room area = new Room();
      area.unit = picked;
      area.ID   = g.rooms.size();
      area.minX = atX;
      area.minY = atY;
      area.wide = area.high = resolution;
      g.rooms.add(area);
      for (Coord m : Visit.grid(atX, atY, resolution, resolution, 1)) {
        g.areaMarkup[m.x - g.offX][m.y - g.offY] = area;
      }
      counts[picked.ID]++;
    }
  }
  
  
  void insertWallsAndDoors(Scene scene, SceneGenGrid g) {
    //
    //  We visit every point in the grid, then visit all adjacent points and
    //  keep a tally of nearby outdoor points and areas.  Points that border
    //  on an area (including outside) that demand a partition will have that
    //  point recorded as a wall.
    for (Coord p : Visit.grid(g.offX, g.offY, g.limit, g.limit, 1)) {
      Room atP = areaUnder(p.x, p.y, g);
      for (int dir : T_ADJACENT) {
        Room atN = areaUnder(p.x + T_X[dir], p.y + T_Y[dir], g);
        if (atP != atN) {
          //  TODO:  NOTE- this is a hack and should be fixed by patching up
          //  the TileConstants class...
          int fudgedDir = (dir + 2) % 8;
          tryRecordingWall(p, fudgedDir, atP, atN, g);
        }
      }
    }
    //
    //  Once all wall-points have been recorded, we populate the area with
    //  actual wall-objects accordingly, and punctuate with doors and windows.
    for (Wall wall : g.walls) {
      for (WallPiece p : wall.pieces) {
        p.wall = scene.addProp(borders, p.x, p.y, p.facing);
      }
    }
    for (Wall wall : g.walls) {
      Batch <WallPiece> canDoor = new Batch();
      for (WallPiece p : wall.pieces) {
        if (canInsertDoor(p.x, p.y, 1, 0, scene)) canDoor.add(p);
        if (canInsertDoor(p.x, p.y, 0, 1, scene)) canDoor.add(p);
      }
      if (! canDoor.empty()) {
        WallPiece d = (WallPiece) Rand.pickFrom(canDoor);
        WallPiece w = (WallPiece) Rand.pickFrom(canDoor);
        if (wall.wallTypeDiff < 2) {
          if (d.wall != null) d.wall.exitScene();
          d.wall = scene.addProp(door, d.x, d.y, d.facing);
        }
        if (w != d && this.window != null && ! wall.indoor) {
          if (w.wall != null) w.wall.exitScene();
          w.wall = scene.addProp(window, w.x, w.y, w.facing);
        }
      }
    }
  }
  
  
  Room areaUnder(int x, int y, SceneGenGrid g) {
    try { return g.areaMarkup[x - g.offX][y - g.offY]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  boolean tryRecordingWall(
    Coord p, int facing, Room from, Room other, SceneGenGrid g
  ) {
    if (this.borders == null) return false;
    int forO = other == null ? WALL_NONE : ((GridUnit) other.unit).wallType;
    int forF = from  == null ? WALL_NONE : ((GridUnit) from .unit).wallType;
    
    boolean shouldWall = false;
    if (forO != forF) shouldWall = true;
    else if (forO == WALL_INTERIOR) shouldWall = true;
    if (! shouldWall) return false;
    
    Wall wall = wallBetween(from, other, g);
    if (wall.indoor && (facing == E || facing == N)) return false;
    
    wall.pieces.add(new WallPiece(p.x, p.y, facing));
    wall.wallTypeDiff = Nums.abs(forO - forF);
    return true;
  }
  
  
  Wall wallBetween(Room a, Room b, SceneGenGrid g) {
    Room source = a == null ? b : a;
    if (source == null) I.complain("No wall source!");
    for (Wall w : source.walls) {
      if (w.side1 == a && w.side2 == b) return w;
      if (w.side1 == b && w.side2 == a) return w;
    }
    Wall w = new Wall();
    w.indoor = a != null && b != null;
    w.side1 = a;
    w.side2 = b;
    if (a != null) a.walls.add(w);
    if (b != null) b.walls.add(w);
    g.walls.add(w);
    return w;
  }
  
  
  boolean canInsertDoor(int x, int y, int dx, int dy, Scene s) {
    if (this.door == null) return false;
    if (sceneBlocked(x + dx, y + dy, s)) return false;
    if (sceneBlocked(x - dx, y - dy, s)) return false;
    return true;
  }
  
  
  boolean sceneBlocked(int x, int y, Scene s) {
    Tile at = s.tileAt(x, y);
    if (at == null) return true;
    return at.blocked() || at.opaque();
  }
  
}



