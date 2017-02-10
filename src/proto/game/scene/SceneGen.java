

package proto.game.scene;
import proto.common.*;
import proto.util.*;



public class SceneGen implements TileConstants {
  
  
  /**  Data fields and structure definitions-
    */
  final static byte
    MARK_NONE     = -1,
    MARK_INIT     =  0,
    MARK_OUTSIDE  =  1,
    MARK_OUT_WALL =  2,
    MARK_WALLS    =  3,
    MARK_WINDOW   =  4,
    MARK_DOORS    =  5,
    MARK_FLOOR    =  6,
    MARK_PROP     =  7,
    MARK_CORRIDOR =  8,
    MARKUP_TYPES  =  9
  ;
  final static int
    VISIT_ALL        = 0,
    VISIT_NO_CORNERS = 1,
    VISIT_MIDDLE     = 2,
    VISIT_RANDOM     = 3,
    VISIT_FLOORS     = 4,
    VISIT_CEILING    = 5
  ;
  
  Scene scene;
  int gridSize, offX, offY, limit;
  byte markup[][];
  Tally <SceneType> frequencies = new Tally();
  
  public boolean verbose = false;
  List <Wall> walls = new List();
  List <Room> rooms = new List(), corridors = new List();
  
  
  static class AreaCheck {
    SceneType room;
    boolean valid;
  }
  
  static class WallPiece extends Coord {
    int facing;
    Prop wall;
    WallPiece(int xp, int yp, int f) { super(xp, yp); facing = f; }
  }
  
  static class Wall {
    boolean indoor;
    Room side1, side2;
    int wallTypeDiff;
    
    Batch <WallPiece> pieces = new Batch();
  }
  
  static class Room {
    SceneType type;
    Object unit;
    int ID;
    int minX, minY, wide, high;
    
    Batch <Wall> walls = new Batch();
    Wall floor = new Wall(), ceiling = new Wall();
  }
  
  static abstract class Tiling {
    abstract void tile(Wall wall, Coord at, int dir);
  }
  
  
  
  /**  Markup methods-
    */
  boolean canFillRoom(SceneType parent, Box2D area, AreaCheck check) {
    final int size = (int) area.maxSide();
    
    Pick <SceneType> pick = new Pick <SceneType> () {
      public void compare(SceneType k, float rating) {
        if (k.maxSize > 0 && size > k.maxSize) return;
        if (k.minSize > 0 && size < k.minSize) return;
        
        float freq = frequencies.valueFor(k);
        rating /= 1 + freq;
        super.compare(k, rating);
      }
    };
    
    pick.compare(parent, 1);
    for (SceneType kidType : parent.kidTypes) pick.compare(kidType, 1);
    
    if (pick.result() == null) return false;
    check.room = pick.result();
    return true;
  }
  
  
  void performRoomFill(Box2D area, SceneType type) {
    performFill(area, type, MARK_FLOOR, MARK_WALLS, rooms);
  }
  
  
  void performCorridorFill(Box2D area, SceneType type) {
    performFill(area, type, MARK_CORRIDOR, MARK_NONE, corridors);
  }
  
  
  void performFill(
    Box2D area, SceneType type, byte floorVal, byte wallVal,
    Series <Room> collection
  ) {
    //
    //  First we generate the room object, store it, and mark the entire floor.
    final Room room = new Room();
    int x = room.minX = (int) area.xpos();
    int y = room.minY = (int) area.ypos();
    int w = room.wide = (int) area.xdim();
    int h = room.high = (int) area.ydim();
    room.type = type;
    collection.add(room);
    
    for (Coord c : Visit.grid(x, y, w, h, 1)) {
      if (floorVal != MARK_NONE) markup[c.x][c.y] = floorVal;
      room.floor  .pieces.add(new WallPiece(c.x, c.y, CENTRE));
      room.ceiling.pieces.add(new WallPiece(c.x, c.y, CENTRE));
    }
    //
    //  Then we iterate over the perimeter, mark as required, and store an
    //  array of points visited as walls.
    int wallIndex = 0, tileIndex = 0;
    for (Coord c : Visit.perimeter(x, y, w, h)) {
      if (wallVal != MARK_NONE) markup[c.x][c.y] = wallVal;
      Wall wall = room.walls.atIndex(wallIndex);
      if (wall == null) room.walls.add(wall = new Wall());
      wall.pieces.add(new WallPiece(c.x, c.y, wallIndex * 2));
      
      int maxLength = (wallIndex % 2 == 0) ? (w + 1) : (h + 1);
      if (++tileIndex >= maxLength) {
        tileIndex = 0;
        wallIndex++;
      }
    }
    //
    //  Finally, we update the frequencies for this room-type:
    float freqInc = (room.wide * room.high) * 1f / (scene.size * scene.size);
    frequencies.add(freqInc, room.type);
    //
    //  And report and return-
    if (verbose) {
      I.say("Performed room fill!");
      printMarkup();
    }
  }
  

  void visitWalls(Room room, int visitMode, Tiling visit) {
    if (visitMode == VISIT_FLOORS || visitMode == VISIT_CEILING) {
      for (Coord c : room.floor.pieces) {
        visit.tile(room.floor, c, CENTRE);
      }
    }
    else if (visitMode == VISIT_CEILING) {
      for (Coord c : room.ceiling.pieces) {
        visit.tile(room.ceiling, c, CENTRE);
      }
    }
    else for (Wall wall : room.walls) {
      Series <WallPiece> points = wall.pieces;
      final int dir = T_ADJACENT[(room.walls.indexOf(wall) + 3) % 4];
      
      if (visitMode == VISIT_ALL) {
        for (Coord at : points) visit.tile(wall, at, dir);
      }
      if (visitMode == VISIT_NO_CORNERS) {
        for (Coord at : points) {
          if (at == points.first() || at == points.last()) continue;
          visit.tile(wall, at, dir);
        }
      }
      if (visitMode == VISIT_MIDDLE) {
        Coord at = points.atIndex(points.size() / 2);
        visit.tile(wall, at, dir);
      }
      if (visitMode == VISIT_RANDOM) {
        Coord at = points.atIndex(1 + Rand.index(points.size() - 2));
        visit.tile(wall, at, dir);
      }
    }
  }
  
  
  boolean markup(int x, int y, byte markVal) {
    try { markup[x][y] = markVal; return true; }
    catch (ArrayIndexOutOfBoundsException e) { return false; }
  }
  
  
  byte sampleFacing(int atX, int atY, int dir) {
    try { return markup[atX + T_X[dir]][atY + T_Y[dir]]; }
    catch (ArrayIndexOutOfBoundsException e) { return MARK_NONE; }
  }
  
  
  boolean couldBlockPathing(Kind propType, int atX, int atY) {
    int w = propType.wide(), h = propType.high();
    int isBlocked = -1, firstBlocked = -1, numBlockages = 0;
    boolean blocksPath = false;
    
    for (Coord c : Visit.grid(atX, atY, w, h, 1)) {
      final byte mark = sampleFacing(c.x, c.y, CENTRE);
      if (mark != MARK_FLOOR && mark != MARK_CORRIDOR) return true;
    }
    
    for (Coord c : Visit.perimeter(atX, atY, w, h)) {
      final byte mark = sampleFacing(c.x, c.y, CENTRE);
      final boolean blocked = mark != MARK_FLOOR && mark != MARK_CORRIDOR;
      
      if (mark == MARK_DOORS) blocksPath = true;
      
      if (firstBlocked == -1) {
        firstBlocked = blocked ? 1 : 0;
      }
      if (blocked) {
        if (isBlocked != 1) {
          isBlocked = 1;
          numBlockages++;
        }
      }
      else {
        if (isBlocked != 0) {
          isBlocked = 0;
        }
      }
    }
    if (isBlocked != firstBlocked) numBlockages++;
    
    if (numBlockages >= 2 || blocksPath) {
      return true;
    }
    else {
      return false;
    }
  }
  
  
  boolean insertProp(
    int atX, int atY, PropType kind, byte markVal, boolean replace
  ) {
    if (kind == null) return false;
    if ((! replace) && (! Prop.hasSpace(scene, kind, atX, atY, N))) {
      return false;
    }
    scene.addProp(kind, atX, atY, N);
    
    final int w = kind.wide(), h = kind.high();
    for (Coord c : Visit.grid(atX, atY, w, h, 1)) markup(c.x, c.y, markVal);
    return true;
  }
  
  
  
  /**  Debugging and printout methods-
    */
  public void printMarkup() {
    I.say("\nPrinting markup for scene: "+scene);
    
    for (int y = scene.size; y-- > 0;) {
      I.say("  ");
      for (int x = 0; x < scene.size; x++) {
        byte b = markup[x][y];
        I.add(b+" ");
      }
    }
    I.say("\n");
  }
  
  
  public void printMarkupVisually() {
    
    int colorVals[][] = new int[scene.size][scene.size];
    final int colorKeys[] = new int[MARKUP_TYPES];
    
    for (int i = MARKUP_TYPES; i-- > 0;) {
      float hue = i * 1f / MARKUP_TYPES;
      colorKeys[i] = java.awt.Color.HSBtoRGB(hue, 1, 0.5f);
    }
    
    for (int y = scene.size; y-- > 0;) {
      for (int x = 0; x < scene.size; x++) {
        byte b = markup[x][y];
        colorVals[x][y] = colorKeys[Nums.clamp(b, MARKUP_TYPES)];
      }
    }
    
    int winSize = scene.size * 10;
    I.present(colorVals, "Generated Scene", winSize, winSize);
  }
}











