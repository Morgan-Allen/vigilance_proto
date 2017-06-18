

package proto.game.scene;
import proto.game.world.*;
import proto.util.*;
import static proto.game.scene.Scenery.*;



public class SceneTypeUnits extends SceneType {
  
  
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
  
  final int resolution;
  final Unit units[];
  
  
  public SceneTypeUnits(
    String name, String ID,
    int resolution,
    int minUnitsW, int maxUnitsW, int minUnitsH, int maxUnitsH,
    PropType wallType, PropType doorType, PropType windowType,
    PropType floorType, Unit... units
  ) {
    super(
      name, ID,
      MIN_WIDE, minUnitsW * resolution,
      MAX_WIDE, maxUnitsW * resolution,
      MIN_HIGH, minUnitsH * resolution,
      MAX_HIGH, maxUnitsH * resolution
    );
    this.resolution = resolution;
    this.units      = units     ;
    this.borders    = wallType  ;
    this.door       = doorType  ;
    this.window     = windowType;
    this.floors     = floorType ;
    int unitID = 0;
    for (Unit unit : units) unit.ID = unitID++;
  }
  
  
  
  /**  Specifying sub-units for placement within the grid-
    */
  public static class Unit {
    int ID;
    SceneType type;
    
    int wallType = WALL_EXTERIOR;
    int exactX = -1, exactY = -1, exactDir = -1;
    int priority, percent, minCount, maxCount;
    
    public String toString() { return type.toString(); }
  }
  
  
  public static Unit unit(
    SceneType type, int wallType,
    int exactX, int exactY, int exactDir,
    int priority, int percent, int minCount, int maxCount
  ) {
    Unit unit = new Unit();
    unit.type     = type    ;
    unit.wallType = wallType;
    
    unit.exactX   = exactX  ;
    unit.exactY   = exactY  ;
    unit.exactDir = exactDir;
    
    unit.priority = priority;
    unit.percent  = percent ;
    unit.minCount = minCount;
    unit.maxCount = maxCount;
    
    return unit;
  }
  
  
  public static Unit unit(
    SceneType type, int wallType,
    int priority, int percent, int minCount, int maxCount
  ) {
    return unit(
      type, wallType,
      -1, -1, N,
      priority, percent, minCount, maxCount
    );
  }
  
  
  public static Unit percentUnit(
    SceneType type, int wallType, int percent
  ) {
    return unit(type, wallType, PRIORITY_MEDIUM, percent, -1, -1);
  }
  
  
  public static Unit numberUnit(
    SceneType type, int wallType, int number
  ) {
    return unit(type, wallType, PRIORITY_HIGH, -1, number, number);
  }
  
  
  public static Unit numberOrPercentUnit(
    SceneType type, int wallType, int percent, int number
  ) {
    return unit(type, wallType, PRIORITY_HIGH, percent, number, -1);
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scenery generateScenery(
    World world, boolean testing
  ) {
    int wide = (int) Rand.range(minWide, maxWide);
    int high = (int) Rand.range(minHigh, maxHigh);
    return generateScenery(world, wide, high, testing);
  }
  
  
  public Scenery generateScenery(
    World world, int wide, int high, boolean testing
  ) {
    Scenery gen = new Scenery(wide, high, testing);
    gen.gridW = wide / resolution;
    gen.gridH = high / resolution;
    populateWithAreas  (world, gen, testing);
    //insertWallsAndDoors(world, gen, testing);
    return gen;
  }
  
  
  void populateWithAreas(World world, Scenery g, boolean testing) {
    I.say("\nPopulating areas for "+this);
    //
    //  We apply all units with exact positions in a separate pass first:
    for (Unit unit : units) {
      boolean exact = unit.exactX != -1 && unit.exactY != -1;
      if (! exact) continue;

      int tX = g.offX + (unit.exactX * resolution);
      int tY = g.offY + (unit.exactY * resolution);
      
      Scenery scenery = unit.type.generateScenery(world, testing);
      unit.type.applyScenery(g, scenery, tX, tY, unit.exactDir, testing);
      
      if (g.areaUnder(tX, tY) != null) continue;
      insertRoom(unit, g, scenery, tX, tY, unit.exactDir, testing);
    }
    //
    //  Then we set up a tally of total placements for other unit-types:
    int     counts  [] = new int    [units.length];
    Scenery typeGens[] = new Scenery[units.length];
    class SpacePick {
      Unit unit;
      int tx, ty, facing;
    }
    //
    //  While there's space left, we iterate over all possible combinations of
    //  location, unit-type and facing, toss in a little random weighting, and
    //  see which looks most promising.  (Note that we skip over any unit types
    //  already past their placement quotas.)
    while (true) {
      Pick <SpacePick> pick     = new Pick();
      List <SpacePick> possible = new List();
      
      for (Unit unit : units) {
        boolean exact = unit.exactX != -1 && unit.exactY != -1;
        if (exact) continue;
        
        SceneType type    = unit.type;
        int       count   = counts  [unit.ID];
        Scenery   typeGen = typeGens[unit.ID];
        
        if (typeGen == null) {
          typeGen = type.generateScenery(world, testing);
          typeGens[unit.ID] = typeGen;
        }
        
        int percent = (count * 100) / (g.gridW * g.gridH);
        if (unit.maxCount > 0 && count   >= unit.maxCount) continue;
        if (unit.percent  > 0 && percent >= unit.percent ) continue;
        
        for (Coord c : Visit.grid(0, 0, g.gridW, g.gridH, 1)) {
          int tX = g.offX + (c.x * resolution);
          int tY = g.offY + (c.y * resolution);
          if (g.areaUnder(tX, tY) != null) continue;
          
          for (int face : T_ADJACENT) {
            if (! type.checkBordering(g, typeGen, tX, tY, face, resolution)) {
              continue;
            }
            
            float rating = unit.priority * 1f / PRIORITY_MEDIUM;
            rating += Nums.max(0, unit.minCount - count);
            rating += Rand.num() / 2;
            
            SpacePick s = new SpacePick();
            s.tx     = tX  ;
            s.ty     = tY  ;
            s.unit   = unit;
            s.facing = face;
            pick.compare(s, rating);
            possible.add(s);
          }
        }
      }
      if (pick.empty()) break;
      
      /*
      I.say("\nPossible placements were: ");
      for (SpacePick s : possible) {
        I.say("  "+s.unit+" at "+s.tx+"|"+s.ty+", face: "+s.facing);
      }
      I.say("");
      //*/
      //
      //  Having pick the most promising option, we apply the furnishings to
      //  the scene:
      SpacePick s       = pick.result();
      Scenery   typeGen = typeGens[s.unit.ID];
      insertRoom(s.unit, g, typeGen, s.tx, s.ty, s.facing, testing);
      counts  [s.unit.ID]++;
      typeGens[s.unit.ID] = null;
    }
  }
  
  
  void insertRoom(
    Unit unit, Scenery g, Scenery typeGen,
    int tX, int tY, int facing, boolean testing
  ) {
    //
    //  
    Box2D bound = unit.type.borderBounds(
      g, typeGen, tX, tY, facing, resolution
    );
    unit.type.applyScenery(g, typeGen, tX, tY, facing, testing);
    //
    //  And mark out a room within the grid with the appropriate attributes,
    //  before incrementing the type's placement counter and clearing the
    //  scenery-
    Room area = new Room();
    area.unit = unit;
    area.ID   = g.rooms.size();
    area.minX = tX;
    area.minY = tY;
    area.wide = (int) bound.xdim();
    area.high = (int) bound.ydim();
    g.recordRoom(area, bound);
  }
  
  
  
  
  final static char
    UNIT_CORNER = 'C',
    UNIT_WALL   = 'W',
    UNIT_BEND   = 'B',
    UNIT_INNER  = 'I';
  
  final static Object RAW_WINGS_MAP[] = {
      0, 0,
      0, 1, UNIT_CORNER, N,
      
      0, 0,
      1, 0, UNIT_CORNER, E,
      
      1, 0,
      0, 0, UNIT_CORNER, S,
      
      0, 1,
      0, 0, UNIT_CORNER, W,
      
      0, 0,
      1, 1, UNIT_WALL, N,
      
      1, 0,
      1, 0, UNIT_WALL, E,
      
      1, 1,
      0, 0, UNIT_WALL, S,
      
      0, 1,
      0, 1, UNIT_WALL, W,
      
      0, 1,
      1, 1, UNIT_BEND, N,
      
      1, 0,
      1, 1, UNIT_BEND, E,
      
      1, 1,
      1, 0, UNIT_BEND, S,
      
      1, 1,
      0, 1, UNIT_BEND, W,
      
      1, 1,
      1, 1, UNIT_INNER, N
  };
  final static Object[][] WINGS_MAP = Visit.splitByDivision(
    RAW_WINGS_MAP, RAW_WINGS_MAP.length / 6
  );
  
  
  public static class Wing {
    public int dir;
    public char unitType;
    public int x, y;
  }
  
  
  public Series <Box2D> generateWingBounds(
    Scenery scene, int resolution,
    float coverFraction, int maxUnitsWide
  ) {
    int r = resolution, gridW = scene.wide / r, gridH = scene.high / r;
    float areaNeeded = scene.wide * scene.high * coverFraction;
    Box2D sceneBound = new Box2D(0, 0, scene.wide, scene.high);
    
    class WingWall extends Vec2D {
      int dir, len;
    }
    class WingArea extends Box2D {
      void addWalls(Series <WingWall> walls, int dir) {
        WingWall w;
        for (int d : T_ADJACENT) if (d != dir) {
          w = new WingWall();
          w.dir = d;
          w.len = (d == N || d == S) ? (int) xdim() : (int) ydim();
          if (d == N) w.set(xpos(), ymax());
          if (d == E) w.set(xmax(), ymax());
          if (d == S) w.set(xmax(), ypos());
          if (d == W) w.set(xpos(), ypos());
          walls.add(w);
        }
      }
      void extend(int dir, int amount) {
        if (dir == N)   incHigh(amount);
        if (dir == E)   incWide(amount);
        if (dir == S) { incHigh(amount); incY(-amount); }
        if (dir == W) { incWide(amount); incX(-amount); }
      }
    }
    
    Series <WingArea> areas = new Batch();
    List <WingWall> walls = new List();
    float totalArea = 0;
    
    int w = (int) (gridW * (1 + Rand.num()) / 2);
    int h = (int) (gridH * (1 + Rand.num()) / 2);
    int x = Rand.index(gridW + 1 - w);
    int y = Rand.index(gridH + 1 - h);
    
    WingArea init = new WingArea();
    init.set(x * r, y * r, w * r, h * r);
    init.expandBy(-2);
    init.addWalls(walls, -1);
    areas.add(init);
    totalArea += init.area();
    
    while (totalArea < areaNeeded && walls.size() > 0) {
      //
      //  Pick one of the existing areas, pick a side, and extend it.
      WingWall parent = (WingWall) Rand.pickFrom(walls);
      walls.remove(parent);
      if (parent.len <= resolution) continue;
      
      WingArea area = new WingArea();
      area.set(parent.x, parent.y, 0, 0);
      int d = parent.dir, l = parent.len;
      int extend = ((d == N || d == S) ? scene.high : scene.wide) / 2;
      area.extend((d + 2) % 8, l);
      
      while (extend-- > 0) {
        area.extend(d, resolution);
        boolean overlaps = false;
        for (WingArea other : areas) {
          if (other.overlaps(area)) {
            overlaps = true;
            break;
          }
          if (! area.containedBy(sceneBound)) {
            overlaps = true;
            break;
          }
        }
        if (overlaps) {
          area.extend(d, -resolution);
          break;
        }
      }
      totalArea += area.area();
      //
      //  If there's room for expansion here, add this wing to the list and
      //  increment total space consumed-
      if (area.area() > 0) {
        areas.add(area);
        area.addWalls(walls, d);
      }
    }
    
    return (Series) areas;
  }
  
  
  public Series <Wing> generateWings(
    Scenery area, int resolution, Series <Box2D> bounds
  ) {
    int gridW = area.wide / resolution, gridH = area.high / resolution;
    int samples[] = new int[4];
    
    Batch <Wing> wings = new Batch();
    for (Coord c : Visit.grid(0, 0, gridW, gridH, 1)) {
      
      int i = 0;
      for (Coord t : Visit.grid(0, 0, 2, 2, 1)) {
        int sx = (c.x + t.x) * resolution;
        int sy = (c.y + t.y) * resolution;
        boolean inBounds = false;
        for (Box2D b : bounds) if (b.contains(sx, sy)) inBounds = true;
        samples[i++] = inBounds ? 1 : 0;
      }
      
      for (Object[] map : WINGS_MAP) {
        boolean match = true;
        i = 4;
        while (i-- > 0) if (((Integer) map[i]) != samples[i]) match = false;
        
        if (match) {
          Wing wing = new Wing();
          wing.unitType = (Character) map[4];
          wing.dir      = (Integer  ) map[5];
          wing.x        = c.x;
          wing.y        = c.y;
          wings.add(wing);
        }
      }
    }
    
    return wings;
  }
  
  
  
  
  
  
  /*
  void insertWallsAndDoors(World world, Scenery g, boolean testing) {
    //
    //  We visit every point in the grid, then visit all adjacent points and
    //  keep a tally of nearby outdoor points and areas.  Points that border
    //  on an area (including outside) that demand a partition will have that
    //  point recorded as a wall.
    I.say("\nInserting walls and doors:");
    for (Coord p : Visit.grid(g.offX, g.offY, g.wide, g.high, 1)) {
      Room atP = g.areaUnder(p.x, p.y);
      for (int dir : T_ADJACENT) {
        Room atN = g.areaUnder(p.x + T_X[dir], p.y + T_Y[dir]);
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
      I.say("  "+DIR_NAMES[(wall.facing + 2) % 8]+" wall ");
      I.add("between "+wall.side1+" and "+wall.side2);
      I.say("    ");
      for (WallPiece p : wall.pieces) {
        I.add("["+p.x+", "+p.y+"] ");
        if (! Prop.hasSpace(g, borders, p.x, p.y, p.facing)) continue;
        p.wall = g.addProp(borders, p.x, p.y, p.facing, world);
      }
    }
    for (Wall wall : g.walls) {
      Batch <WallPiece> canDoor = new Batch();
      
      wallLoop: for (WallPiece p : wall.pieces) if (p.wall != null) {
        if (sceneBlocked(p.x, p.y, g)) continue wallLoop;
        for (int dir : T_ADJACENT) {
          int nX = p.x + T_X[dir], nY = p.y + T_Y[dir];
          if (g.areaUnder(nX, nY) != g.areaUnder(p.x, p.y)) {
            if (sceneBlocked(nX, nY, g)) continue wallLoop;
          }
        }
        canDoor.add(p);
      }
      
      if (! canDoor.empty()) {
        WallPiece d = (WallPiece) Rand.pickFrom(canDoor);
        WallPiece w = (WallPiece) Rand.pickFrom(canDoor);
        if (wall.wallTypeDiff < 2) {
          if (d.wall != null) d.wall.exitScene();
          d.wall = g.addProp(door, d.x, d.y, d.facing, world);
        }
        if (w != d && this.window != null && ! wall.indoor) {
          if (w.wall != null) w.wall.exitScene();
          w.wall = g.addProp(window, w.x, w.y, w.facing, world);
        }
      }
    }
  }
  
  
  boolean tryRecordingWall(
    Coord p, int facing, Room from, Room other, Scenery g
  ) {
    if (this.borders == null) return false;
    Object unitO = other == null ? null : other.unit;
    Object unitF = from  == null ? null : from .unit;
    int forO = other == null ? WALL_NONE : ((Unit) other.unit).wallType;
    int forF = from  == null ? WALL_NONE : ((Unit) from .unit).wallType;
    
    boolean shouldWall = forO != forF;
    if (forO == WALL_INTERIOR && unitO != unitF) shouldWall = true;
    if (! shouldWall) return false;
    
    Wall wall = wallBetween(from, other, facing, g);
    if (wall.indoor && (facing == E || facing == N)) return false;
    
    wall.pieces.add(new WallPiece(p.x, p.y, facing));
    wall.wallTypeDiff = Nums.abs(forO - forF);
    return true;
  }
  
  
  Wall wallBetween(Room a, Room b, int facing, Scenery g) {
    Room source = a == null ? b : a;
    if (source == null) I.complain("No wall source!");
    for (Wall w : source.walls) {
      if (w.facing != facing) continue;
      if (w.side1 == a && w.side2 == b) return w;
      if (w.side1 == b && w.side2 == a) return w;
    }
    Wall w = new Wall();
    w.indoor = a != null && b != null;
    w.facing = facing;
    w.side1  = a;
    w.side2  = b;
    if (a != null) a.walls.add(w);
    if (b != null) b.walls.add(w);
    g.walls.add(w);
    return w;
  }
  
  
  boolean sceneBlocked(int x, int y, Scenery s) {
    Tile at = s.tileAt(x, y);
    if (at == null) return true;
    return at.blockageVal(CENTRE) > 0 || at.opaque();
  }
  //*/
  
}




