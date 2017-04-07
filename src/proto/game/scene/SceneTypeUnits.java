

package proto.game.scene;
import proto.game.world.*;
import proto.util.*;
import static proto.game.scene.SceneGen.*;



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
  
  static class SceneGenUnits extends SceneGen {
    Room areaMarkup[][];
  }
  
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
    SceneTypeGrid type;
    int wallType = WALL_EXTERIOR;
    int exactX = -1, exactY = -1, exactDir = -1;
    int priority, percent, minCount, maxCount;
    
    public String toString() { return type.toString(); }
  }
  
  
  public static Unit unit(
    SceneTypeGrid type, int wallType,
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
    SceneTypeGrid type, int wallType,
    int priority, int percent, int minCount, int maxCount
  ) {
    return unit(
      type, wallType,
      -1, -1, N,
      priority, percent, minCount, maxCount
    );
  }
  
  
  public static Unit percentUnit(
    SceneTypeGrid type, int wallType, int percent
  ) {
    return unit(type, wallType, PRIORITY_MEDIUM, percent, -1, -1);
  }
  
  
  public static Unit numberUnit(
    SceneTypeGrid type, int wallType, int number
  ) {
    return unit(type, wallType, PRIORITY_HIGH, -1, number, number);
  }
  
  
  public static Unit numberOrPercentUnit(
    SceneTypeGrid type, int wallType, int percent, int number
  ) {
    return unit(type, wallType, PRIORITY_HIGH, percent, number, -1);
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(World world, int w, int h, boolean testing) {
    I.say("\n\nGENERATING GRID SCENE "+this);
    
    int gridW = w / resolution, gridH = h / resolution;
    int wideS = gridW * resolution, highS = gridH * resolution;
    
    Scene scene = new Scene(world, wideS + 4, highS + 4);
    scene.setupScene(testing);
    applyToScene(scene, 2, 2, N, wideS, highS, testing);
    return scene;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int w, int h, boolean testing
  ) {
    SceneGenUnits gen = new SceneGenUnits();
    gen.scene      = scene;
    gen.gridW      = w / resolution;
    gen.gridH      = h / resolution;
    gen.offX       = offX;
    gen.offY       = offY;
    gen.wide       = w;
    gen.high       = h;
    gen.areaMarkup = new Room[w][h];
    
    populateWithAreas(scene, gen);
    insertWallsAndDoors(scene, gen);
  }
  
  
  void populateWithAreas(Scene scene, SceneGenUnits g) {
    //
    //  First, we set up a tally of total placements for each unit-type:
    int counts[] = new int[units.length];
    class SpacePick { Unit unit; int tx; int ty; int facing; }
    //
    //  While there's space left, we iterate over all possible combinations of
    //  location, unit-type and facing, toss in a little random weighting, and
    //  see which looks most promising.  (Note that we skip over any unit types
    //  already past their placement quotas.)
    while (true) {
      Pick <SpacePick> pick = new Pick();
      
      for (Unit unit : units) {
        boolean exact = unit.exactX != -1 && unit.exactY != -1;
        SceneTypeGrid type = unit.type;
        int count = counts[unit.ID];
        
        if (! exact) {
          int percent = (count * 100) / (g.gridW * g.gridH);
          if (unit.maxCount > 0 && count   >= unit.maxCount) continue;
          if (unit.percent  > 0 && percent >= unit.percent ) continue;
        }
        
        for (Coord c : Visit.grid(0, 0, g.gridW, g.gridH, 1)) {
          int tX = g.offX + (c.x * resolution);
          int tY = g.offY + (c.y * resolution);
          if (g.areaMarkup[tX][tY] != null) continue;
          
          for (int face : T_ADJACENT) {
            float rating = unit.priority * 1f / PRIORITY_MEDIUM;
            if (exact) {
              if (c.x            != unit.exactX  ) continue;
              if (c.y            != unit.exactY  ) continue;
              if ((face + 0 % 8) != unit.exactDir) continue;
              rating += 100;
            }
            else if (type.checkBordering(scene, tX, tY, face, resolution)) {
              rating += Nums.max(0, unit.minCount - count);
              rating += Rand.num() / 2;
            }
            if (rating != -1) {
              SpacePick s = new SpacePick();
              s.tx     = tX  ;
              s.ty     = tY  ;
              s.unit   = unit;
              s.facing = face;
              pick.compare(s, rating);
            }
          }
        }
      }
      if (pick.empty()) break;
      //
      //  Having pick the most promising option, we apply the furnishings to
      //  the scene:
      SpacePick     s    = pick.result();
      SceneTypeGrid type = s.unit.type;
      Box2D bound = type.borderBounds(scene, s.tx, s.ty, s.facing, resolution);
      I.say("PICKED GRID UNIT "+type+" AT "+s.tx+"|"+s.ty+", FACE: "+s.facing);
      type.applyToScene(
        scene, s.tx, s.ty, s.facing, (int) bound.xdim(), (int) bound.ydim(),
        g.verbose
      );
      //
      //  And mark out a room within the grid with the appropriate attributes,
      //  before increment the type's placement counter and removing the
      //  location used:
      Room area = new Room();
      area.unit = s.unit;
      area.ID   = g.rooms.size();
      area.minX = s.tx;
      area.minY = s.ty;
      area.wide = (int) bound.xdim();
      area.high = (int) bound.ydim();
      g.rooms.add(area);
      
      for (Coord m : Visit.grid(bound)) try {
        g.areaMarkup[m.x - g.offX][m.y - g.offY] = area;
      }
      catch (Exception e) {}
      counts[s.unit.ID]++;
    }
  }
  
  
  void insertWallsAndDoors(Scene scene, SceneGenUnits g) {
    //
    //  We visit every point in the grid, then visit all adjacent points and
    //  keep a tally of nearby outdoor points and areas.  Points that border
    //  on an area (including outside) that demand a partition will have that
    //  point recorded as a wall.
    I.say("\nInserting walls and doors:");
    for (Coord p : Visit.grid(g.offX, g.offY, g.wide, g.high, 1)) {
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
      I.say("  "+DIR_NAMES[(wall.facing + 2) % 8]+" wall ");
      I.add("between "+wall.side1+" and "+wall.side2);
      I.say("    ");
      for (WallPiece p : wall.pieces) {
        I.add("["+p.x+", "+p.y+"] ");
        if (! Prop.hasSpace(scene, borders, p.x, p.y, p.facing)) continue;
        p.wall = scene.addProp(borders, p.x, p.y, p.facing);
      }
    }
    for (Wall wall : g.walls) {
      Batch <WallPiece> canDoor = new Batch();
      
      wallLoop: for (WallPiece p : wall.pieces) if (p.wall != null) {
        if (sceneBlocked(p.x, p.y, scene)) continue wallLoop;
        for (int dir : T_ADJACENT) {
          int nX = p.x + T_X[dir], nY = p.y + T_Y[dir];
          if (areaUnder(nX, nY, g) != areaUnder(p.x, p.y, g)) {
            if (sceneBlocked(nX, nY, scene)) continue wallLoop;
          }
        }
        canDoor.add(p);
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
  
  
  Room areaUnder(int x, int y, SceneGenUnits g) {
    try { return g.areaMarkup[x - g.offX][y - g.offY]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  boolean tryRecordingWall(
    Coord p, int facing, Room from, Room other, SceneGenUnits g
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
  
  
  Wall wallBetween(Room a, Room b, int facing, SceneGenUnits g) {
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
  
  
  boolean sceneBlocked(int x, int y, Scene s) {
    Tile at = s.tileAt(x, y);
    if (at == null) return true;
    return at.blockageVal(CENTRE) > 0 || at.opaque();
  }
  
}




