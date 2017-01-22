

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



//  TODO:  You'll also need to ensure contiguous pathing between all the
//  various sub-units (even if door-hacks are needed.)

public class SceneTypeGrid extends SceneType {
  
  
  /**  Data fields, construction and save/load methods-
    */
  public static enum WallType {
    NONE, EXTERIOR, INCLUDED
  };
  final public static int
    PRIORITY_HIGH   = 4,
    PRIORITY_MEDIUM = 2,
    PRIORITY_LOW    = 0
  ;
  
  public static class GridUnit {
    int ID;
    SceneTypeFixed type;
    WallType wallType = WallType.EXTERIOR;
    int priority, percent, minCount, maxCount;
  }
  
  final int resolution, wallPad;
  final GridUnit units[];
  
  
  public SceneTypeGrid(
    String name, String ID,
    int resolution, int wallPad,
    Kind wallType, Kind doorType, Kind windowType,
    GridUnit... units
  ) {
    super(name, ID);
    this.resolution = resolution;
    this.wallPad    = wallPad;
    this.units      = units;
    this.borders = wallType;
    this.door    = doorType;
    this.window  = windowType;
    int unitID = 0;
    for (GridUnit unit : units) unit.ID = unitID++;
  }
  
  
  
  /**  Specifying sub-units for placement within the grid-
    */
  public static GridUnit unit(
    SceneTypeFixed type,
    int priority, int percent, int minCount, int maxCount
  ) {
    GridUnit unit = new GridUnit();
    unit.type     = type;
    unit.priority = priority;
    unit.percent  = percent;
    unit.minCount = minCount;
    unit.maxCount = maxCount;
    return unit;
  }
  
  
  public static GridUnit percentUnit(SceneTypeFixed type, int percent) {
    return unit(type, PRIORITY_MEDIUM, percent, -1, -1);
  }
  
  
  public static GridUnit numberUnit(SceneTypeFixed type, int number) {
    return unit(type, PRIORITY_HIGH, -1, number, number);
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(World world, int size, boolean forTesting) {
    I.say("GENERATING GRID SCENE "+this);
    
    final int gridSize = size / resolution;
    size = (gridSize * resolution) + ((gridSize + 1) * wallPad) + 2;
    
    Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    
    SceneGen gen = new SceneGen();
    gen.scene = scene;
    gen.gridSize = gridSize;
    gen.areaMarkup = new GridArea[size][size];
    
    populateWithAreas(scene, gen);
    insertWallsAndDoors(scene, gen);
    
    return scene;
  }
  
  

  static class GridArea {
    GridUnit unit;
    int ID;
    int minX, minY;
    Batch <AreaWall> walls = new Batch();
    
    public String toString() { return ""+ID; }
  }
  
  
  static class AreaWall {
    boolean indoor;
    GridArea side1, side2;
    Batch <Coord> points = new Batch();
  }
  
  
  static class SceneGen {
    Scene scene;
    int gridSize;
    List <GridArea> areas = new List();
    List <AreaWall> walls = new List();
    GridArea areaMarkup[][];
  }
  
  
  void populateWithAreas(Scene scene, SceneGen g) {
    int off = wallPad + 1;
    int counts[] = new int[units.length];
    
    for (Coord c : Visit.grid(0, 0, g.gridSize, g.gridSize, 1)) {
      int atX = off + (c.x * (resolution + wallPad));
      int atY = off + (c.y * (resolution + wallPad));
      
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
      picked.type.applyToScene(scene, atX, atY, N, resolution);
      //
      //  
      GridArea area = new GridArea();
      area.unit = picked;
      area.ID = g.areas.size();
      area.minX = atX;
      area.minY = atY;
      g.areas.add(area);
      for (Coord m : Visit.grid(atX, atY, resolution, resolution, 1)) {
        g.areaMarkup[m.x][m.y] = area;
      }
      counts[picked.ID]++;
    }
  }
  
  
  void insertWallsAndDoors(Scene scene, SceneGen g) {
    for (Coord p : Visit.grid(0, 0, scene.size, scene.size, 1)) {
      checkForWall(p,  1,  0, g);
      checkForWall(p,  0,  1, g);
      checkForWall(p,  1,  1, g);
      checkForWall(p, -1,  1, g);
    }
    for (AreaWall wall : g.walls) {
      for (Coord c : wall.points) {
        scene.addProp(borders, c.x, c.y, N);
      }
    }
    for (AreaWall wall : g.walls) {
      Batch <Coord> canDoor = new Batch();
      for (Coord c : wall.points) {
        if (canInsertDoor(c.x, c.y, 1, 0, scene)) canDoor.add(c);
        if (canInsertDoor(c.x, c.y, 0, 1, scene)) canDoor.add(c);
      }
      //  TODO:  You also need to insert windows.
      if (! canDoor.empty()) {
        Coord d = (Coord) Rand.pickFrom(canDoor);
        scene.addProp(door, d.x, d.y, N);
      }
    }
  }
  
  
  void checkForWall(
    Coord p, int dx, int dy, SceneGen g
  ) {
    if (areaUnder(p.x, p.y, g) != null) return;
    GridArea from  = areaUnder(p.x - dx, p.y - dy, g);
    GridArea other = areaUnder(p.x + dx, p.y + dy, g);
    if (! shouldWallBetween(from, other)) return;
    AreaWall wall = wallBetween(from, other, g);
    for (Coord c : wall.points) if (c.matches(p)) return;
    wall.points.add(new Coord(p));
  }
  
  
  GridArea areaUnder(int x, int y, SceneGen g) {
    try { return g.areaMarkup[x][y]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  boolean shouldWallBetween(GridArea from, GridArea other) {
    WallType forO = other == null ? WallType.NONE : other.unit.wallType;
    WallType forF = from  == null ? WallType.NONE : from .unit.wallType;
    if (forO != forF) return true;
    if (forO == WallType.INCLUDED) return from.unit != other.unit;
    return false;
  }
  
  
  AreaWall wallBetween(GridArea a, GridArea b, SceneGen g) {
    GridArea source = a == null ? b : a;
    if (source == null) I.complain("No wall source!");
    for (AreaWall w : source.walls) {
      if (w.side1 == a && w.side2 == b) return w;
      if (w.side1 == b && w.side2 == a) return w;
    }
    AreaWall w = new AreaWall();
    w.indoor = a == null || b == null;
    w.side1 = a;
    w.side2 = b;
    if (a != null) a.walls.add(w);
    if (b != null) b.walls.add(w);
    g.walls.add(w);
    return w;
  }
  
  
  boolean checkOutside(int x, int y, Batch <GridArea> toWall) {
    for (GridArea a : toWall) {
      if ((x < a.minX - 1) || (x > a.minX + resolution)) continue;
      if ((y < a.minY - 1) || (y > a.minY + resolution)) continue;
      return false;
    }
    return true;
  }
  
  
  boolean canInsertDoor(int x, int y, int dx, int dy, Scene s) {
    if (sceneBlocked(x + dx, y + dy, s)) return false;
    if (sceneBlocked(x - dx, y - dy, s)) return false;
    return true;
  }
  
  
  boolean sceneBlocked(int x, int y, Scene s) {
    Tile at = s.tileAt(x, y);
    if (at == null) return true;
    return at.blocked();
  }
  
}




