

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
  final boolean thickWalls;
  final GridUnit units[];
  
  
  public SceneTypeGrid(
    String name, String ID,
    int resolution, int maxUnitSize, boolean thickWalls,
    Kind wallType, Kind doorType, Kind windowType, Kind floorType,
    GridUnit... units
  ) {
    super(
      name, ID,
      MIN_SIZE, resolution * 2,
      MAX_SIZE, (maxUnitSize > 1 ? (resolution * maxUnitSize) : -1)
    );
    this.resolution = resolution;
    this.thickWalls = thickWalls;
    this.units      = units;
    this.borders = wallType;
    this.door    = doorType;
    this.window  = windowType;
    this.floors  = floorType;
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
    
    final int gridSize = size / resolution, wallPad = thickWalls ? 1 : 0;
    size = (gridSize * resolution) + ((gridSize + 1) * wallPad) + 2;
    
    Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 1, 1, N, size, forTesting);
    
    return scene;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int size, boolean forTesting
  ) {
    int wallPad = thickWalls ? 1 : 0;
    int gridSize = (size - wallPad) / (resolution + wallPad);
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
    int wallPad = thickWalls ? 1 : 0;
    int counts[] = new int[units.length];
    
    //  TODO:  You need to select optimal facings as well- and iterate over
    //  types before you iterate over locations.
    
    for (Coord c : Visit.grid(0, 0, g.gridSize, g.gridSize, 1)) {
      int atX = g.offX + wallPad + (c.x * (resolution + wallPad));
      int atY = g.offY + wallPad + (c.y * (resolution + wallPad));
      
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
      Room under = areaUnder(p.x, p.y, g);
      if (under != null) continue;
      Batch <Room> nearB = new Batch(8);
      int numOutside = 0;
      boolean walled = false;
      
      for (int dir : T_INDEX) {
        Room next = areaUnder(p.x + T_X[dir], p.y + T_Y[dir], g);
        if (next != null) nearB.include(next);
        else numOutside++;
      }
      for (Room a : nearB) {
        for (Room b : nearB) if (a != b) {
          walled |= tryRecordingWall(p, a, b, g);
        }
        if (numOutside > 4) {
          walled |= tryRecordingWall(p, a, null, g);
        }
      }
      if ((! walled) && (! nearB.empty())) {
        scene.addProp(floors, p.x, p.y, N);
      }
    }
    //
    //  Once all wall-points have been recorded, we populate the area with
    //  actual wall-objects accordingly, and punctuate with doors and windows.
    for (Wall wall : g.walls) {
      for (Coord c : wall.points) {
        scene.addProp(borders, c.x, c.y, N);
      }
    }
    for (Wall wall : g.walls) {
      Batch <Coord> canDoor = new Batch();
      for (Coord c : wall.points) {
        if (canInsertDoor(c.x, c.y, 1, 0, scene)) canDoor.add(c);
        if (canInsertDoor(c.x, c.y, 0, 1, scene)) canDoor.add(c);
      }
      if (! canDoor.empty()) {
        Coord d = (Coord) Rand.pickFrom(canDoor);
        Coord w = (Coord) Rand.pickFrom(canDoor);
        if (wall.wallTypeDiff < 2) {
          scene.addProp(door, d.x, d.y, N);
        }
        if (w != d && ! wall.indoor) {
          scene.addProp(window, w.x, w.y, N);
        }
      }
    }
  }
  
  
  Room areaUnder(int x, int y, SceneGenGrid g) {
    try { return g.areaMarkup[x - g.offX][y - g.offY]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  boolean tryRecordingWall(Coord p, Room from, Room other, SceneGenGrid g) {
    int forO = other == null ? WALL_NONE : ((GridUnit) other.unit).wallType;
    int forF = from  == null ? WALL_NONE : ((GridUnit) from .unit).wallType;
    
    boolean shouldWall = false;
    if (forO != forF) shouldWall = true;
    else if (forO == WALL_INTERIOR) shouldWall = true;
    if (! shouldWall) return false;
    
    Wall wall = wallBetween(from, other, g);
    for (Coord c : wall.points) if (c.matches(p)) return false;
    wall.points.add(new Coord(p));
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



