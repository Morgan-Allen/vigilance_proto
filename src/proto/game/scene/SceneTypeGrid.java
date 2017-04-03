

package proto.game.scene;
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
    I.say("\n\nGENERATING GRID SCENE "+this);
    
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
    //
    //  First, we compile a list of all available spaces, and set up a tally
    //  of total placements for each unit-type:
    List <Coord> allSpaces = new List();
    for (Coord c : Visit.grid(0, 0, g.gridSize, g.gridSize, 1)) {
      int atX = g.offX + (c.x * resolution);
      int atY = g.offY + (c.y * resolution);
      allSpaces.add(new Coord(atX, atY));
    }
    int counts[] = new int[units.length];
    class SpacePick { GridUnit unit; Coord at; int facing; }
    //
    //  While there's space left, we iterate over all possible combinations of
    //  location, unit-type and facing, toss in a little random weighting, and
    //  see which looks most promising.  (Note that we skip over any unit types
    //  already past their placement quotas.)
    while (! allSpaces.empty()) {
      Pick <SpacePick> pick = new Pick();
      
      for (GridUnit unit : units) {
        int count = counts[unit.ID];
        int percent = (count * 100) / (g.gridSize * g.gridSize);
        if (unit.maxCount > 0 && count   >= unit.maxCount) continue;
        if (unit.percent  > 0 && percent >= unit.percent ) continue;
        
        for (Coord c : allSpaces) for (int face : T_ADJACENT) {
          if (unit.type.checkBordering(scene, c.x, c.y, face, resolution)) {
            float rating = unit.priority * 1f / PRIORITY_MEDIUM;
            rating += Nums.max(0, unit.minCount - count);
            rating += Rand.num() / 2;
            
            SpacePick s = new SpacePick();
            s.at     = c   ;
            s.unit   = unit;
            s.facing = face;
            pick.compare(s, rating);
          }
        }
      }
      if (pick.empty()) break;
      //
      //  Having pick the most promising option, we apply the furnishings to
      //  the scene:
      SpacePick s    = pick.result();
      Coord     at   = s.at;
      SceneType type = s.unit.type;
      I.say("PICKED GRID UNIT "+type+" AT "+at+", FACE: "+s.facing);
      type.applyToScene(scene, at.x, at.y, s.facing, resolution, g.verbose);
      //
      //  And mark out a room within the grid with the appropriate attributes,
      //  before increment the type's placement counter and removing the
      //  location used:
      Room area = new Room();
      area.unit = s.unit;
      area.ID   = g.rooms.size();
      area.minX = at.x;
      area.minY = at.y;
      area.wide = area.high = resolution;
      g.rooms.add(area);
      for (Coord m : Visit.grid(at.x, at.y, resolution, resolution, 1)) {
        g.areaMarkup[m.x - g.offX][m.y - g.offY] = area;
      }
      counts[s.unit.ID]++;
      allSpaces.remove(at);
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
        if (! Prop.hasSpace(scene, borders, p.x, p.y, p.facing)) continue;
        p.wall = scene.addProp(borders, p.x, p.y, p.facing);
      }
    }
    for (Wall wall : g.walls) {
      Batch <WallPiece> canDoor = new Batch();
      for (WallPiece p : wall.pieces) if (p.wall != null) {
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
    Object unitO = other == null ? null : other.unit;
    Object unitF = from  == null ? null : from .unit;
    int forO = other == null ? WALL_NONE : ((GridUnit) other.unit).wallType;
    int forF = from  == null ? WALL_NONE : ((GridUnit) from .unit).wallType;
    
    boolean shouldWall = forO != forF;
    if (forO == WALL_INTERIOR && unitO != unitF) shouldWall = true;
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



