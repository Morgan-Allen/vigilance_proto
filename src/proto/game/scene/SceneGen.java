

package proto.game.scene;
import proto.common.*;
import proto.util.*;




public class SceneGen implements TileConstants {
  
  
  /**  Constants, data fields and construction methods-
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
  
  final Scene scene;
  final byte markup[][];
  Tally <SceneType> frequencies = new Tally();
  
  public int minRoomSize = 4;
  public float corWideFraction = 0.125f;
  public float maxSplitFrac = 0.5f;
  public boolean verbose = false;
  
  static class AreaCheck {
    SceneType room;
    boolean valid;
  }
  
  static class Room {
    SceneType type;
    Box2D area;
    Coord walls[][], floor[], ceiling[];
  }

  static abstract class Tiling {
    abstract void tile(Coord wall[], Coord at, int dir);
  }
  
  List <Room> rooms = new List(), corridors = new List();
  
  
  public SceneGen(Scene scene) {
    this.scene = scene;
    this.markup = new byte[scene.size][scene.size];
  }
  
  
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
  
  
  
  /**  
    */
  public void populateAsRoot(SceneType root, Box2D area) {
    attemptRoomDivision(root, area);
    placeCurtainWall(root, area);
    for (Room room : rooms) {
      insertPropsForRoom(room);
    }
    for (Room room : corridors) {
      insertPropsForCorridor(room);
    }
  }
  
  
  void attemptRoomDivision(SceneType parent, Box2D area) {
    //
    //  Firstly, check if the area itself is fit to populate with one of the
    //  scene's room-types:
    AreaCheck check0 = attemptAreaCheck(parent, area);
    if (check0.valid && check0.room != null) {
      performRoomFill(area, check0.room);
      return;
    }
    //
    //  Failing that, try subdividing the area into two different areas along
    //  the longer axis, and try to ensure that each can be filled with a room
    //  that has access to a corridor- or failing that, that each has access to
    //  a corridor along it's longer side (see below.)
    boolean longX = area.xdim() > area.ydim();
    int maxSide = (int) area.maxSide();
    if (maxSide <= minRoomSize) return;
    
    int corWide = Nums.max(1, (int) (maxSide * corWideFraction));
    float splitFrac = maxSide - ((minRoomSize * 2) + corWide + 2);
    splitFrac = Nums.clamp(splitFrac, 0, maxSide * maxSplitFrac);
    int split = (int) (((maxSide - splitFrac) / 2) + (Rand.num() * splitFrac));
    
    if (verbose) {
      I.say("Attempting to subdivide: "+area);
      I.say("  Max. Side: "+maxSide);
      I.say("  Split fraction: "+splitFrac+", split at: "+split);
    }

    Box2D a1 = new Box2D().setTo(area), a2 = new Box2D().setTo(area);
    int p1, p2, p3, p4;
    p1 = 0;
    p2 = split;
    p3 = p2 + 1;
    p4 = maxSide;
    
    if (longX) {
      int x = (int) area.xpos();
      a1.setX(x + p1, p2 - p1);
      a2.setX(x + p3, p4 - p3);
    }
    else {
      int y = (int) area.ypos();
      a1.setY(y + p1, p2 - p1);
      a2.setY(y + p3, p4 - p3);
    }
    
    AreaCheck check1 = attemptAreaCheck(parent, a1);
    AreaCheck check2 = attemptAreaCheck(parent, a2);
    
    if (check1.valid && check2.valid) {
      
      if (check1.room != null) performRoomFill(a1, check1.room);
      else attemptRoomDivision(parent, a1);
      
      if (check2.room != null) performRoomFill(a2, check2.room);
      else attemptRoomDivision(parent, a2);
      
      return;
    }
    //
    //  If that's not possible, you'll need to insert a corridor between the
    //  two areas (to ensure ease of access) and try again.
    if (verbose) I.say("Initial split was not successful, adding corridor");
    Box2D ac = new Box2D().setTo(area).expandBy(1);
    int hCW = corWide / 2, p5, p6;
    p1 = 0;
    p2 = split - (hCW + 1);
    p3 = p2 + 1;
    p4 = p3 + corWide;
    p5 = p4 + 1;
    p6 = maxSide;
    
    if (longX) {
      int x = (int) area.xpos();
      a1.setX(x + p1, p2 - p1);
      ac.setX(x + p3, p4 - p3);
      a2.setX(x + p5, p6 - p5);
    }
    else {
      int y = (int) area.ypos();
      a1.setY(y + p1, p2 - p1);
      ac.setY(y + p3, p4 - p3);
      a2.setY(y + p5, p6 - p5);
    }
    
    performCorridorFill(ac, parent);
    attemptRoomDivision(parent, a1);
    attemptRoomDivision(parent, a2);
  }
  
  
  
  /**  Utility methods for checking the validity of room placement-
    */
  AreaCheck attemptAreaCheck(SceneType parent, Box2D area) {
    AreaCheck check = new AreaCheck();
    
    if (canFillRoom(parent, area, check) && hasCorridorAccess(area, false)) {
      check.valid = true;
    }
    else if (hasCorridorAccess(area, true)) {
      check.valid = true;
    }
    else check.valid = false;
    
    if (verbose) {
      Object room = check.room;
      if      (! check.valid) I.say("  Cannot split:     "+area);
      else if (room == null ) I.say("  Should subdivide: "+area);
      else                    I.say("  Will place room:  "+room+" in "+area);
    }
    return check;
  }
  
  
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
  
  
  boolean hasCorridorAccess(Box2D area, boolean longSideOnly) {
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    boolean longX = area.xdim() > area.ydim();
    boolean screenX = longSideOnly &&   longX;
    boolean screenY = longSideOnly && ! longX;
    
    for (Coord c : Visit.perimeter(x - 1, y - 1, w + 2, h + 2)) try {
      if (screenY && (c.y < y || c.y >= (y + h))) continue;
      if (screenX && (c.x < x || c.x >= (x + w))) continue;
      
      byte markVal = markup[c.x][c.y];
      if (markVal == MARK_CORRIDOR) return true;
    } catch (ArrayIndexOutOfBoundsException e) { continue; }
    
    return false;
  }
  
  
  
  /**  Utility methods for doing actual markup of the floorplan-
    */
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
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    final Room room = new Room();
    room.type = type;
    room.area = area;
    room.walls = new Coord[4][];
    room.walls[0] = new Coord[w + 2];
    room.walls[1] = new Coord[h + 2];
    room.walls[2] = new Coord[w + 2];
    room.walls[3] = new Coord[h + 2];
    room.floor    = new Coord[w * h];
    room.ceiling  = new Coord[w * h];
    collection.add(room);
    
    int floorIndex = 0, wallIndex = 0, tileIndex = 0;
    for (Coord c : Visit.grid(x, y, w, h, 1)) {
      if (floorVal != MARK_NONE) markup[c.x][c.y] = floorVal;
      room.floor  [floorIndex] = new Coord(c);
      room.ceiling[floorIndex] = room.floor[floorIndex++];
    }
    //
    //  Then we iterate over the perimeter, mark as required, and store an
    //  array of points visited as walls.  (Note some post-processing to ensure
    //  corners appear in both adjoining walls.)
    for (Coord c : Visit.perimeter(x, y, w, h)) {
      if (wallVal != MARK_NONE) markup[c.x][c.y] = wallVal;
      room.walls[wallIndex][tileIndex] = new Coord(c);
      
      if (++tileIndex > room.walls[wallIndex].length - 2) {
        tileIndex = 0;
        wallIndex++;
      }
    }
    for (int i = 4; i-- > 0;) {
      Coord wall[] = room.walls[i], nextWall[] = room.walls[(i + 1) % 4];
      wall[wall.length - 1] = nextWall[0];
    }
    //
    //  Finally, we update the frequencies for this room-type:
    float freqInc = room.area.area() / (scene.size * scene.size);
    frequencies.add(freqInc, room.type);
    //
    //  And report and return-
    if (verbose) {
      I.say("Performed room fill!");
      printMarkup();
    }
  }
  
  
  
  /**  Utility methods for furniture placement-
    */
  //  TODO:  In future, you might want to do a proper perimeter trace...
  void placeCurtainWall(SceneType buildingType, Box2D area) {
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    
    for (Coord c : Visit.perimeter(x, y, w, h)) {
      insertProp(c.x, c.y, buildingType.borders, MARK_OUT_WALL, true);
    }
    for (Coord c : Visit.perimeter(x - 1, y - 1, w + 2, h + 2)) {
      markup(c.x, c.y, MARK_OUTSIDE);
    }
  }
  
  
  void insertPropsForRoom(final Room room) {
    visitWalls(room, VISIT_FLOORS, new Tiling() {
      void tile(Coord[] wall, Coord at, int dir) {
        insertProp(at.x, at.y, room.type.floors, MARK_FLOOR, false);
      }
    });
    visitWalls(room, VISIT_ALL, new Tiling() {
      void tile(Coord[] wall, Coord at, int dir) {
        insertProp(at.x, at.y, room.type.borders, MARK_WALLS, false);
      }
    });
    visitWalls(room, VISIT_RANDOM, new Tiling() {
      void tile(Coord[] wall, Coord at, int dir) {
        if (sampleFacing(at.x, at.y, dir) == MARK_CORRIDOR) {
          insertProp(at.x, at.y, room.type.door, MARK_DOORS, true);
        }
      }
    });
    visitWalls(room, VISIT_MIDDLE, new Tiling() {
      void tile(Coord[] wall, Coord at, int dir) {
        if (sampleFacing(at.x, at.y, dir) == MARK_OUTSIDE) {
          insertProp(at.x, at.y, room.type.window, MARK_WINDOW, true);
        }
      }
    });
    //
    //  TODO:  You'll want to use some more sophisticated options for placing
    //  props- e.g, at regular spacing, with varying frequency, next to the
    //  walls, surrounding other props, et cetera.
    final Box2D a = room.area;
    float roomArea = a.area();
    
    for (Kind propType : room.type.props) {
      float areaUsed = 0, maxUsed = (roomArea * 0.5f) / room.type.props.length;
      float propArea = propType.wide() * propType.high();
      int numTries = 0, maxTries = (int) (roomArea * 4f / propArea);
      
      while (areaUsed < maxUsed && numTries++ < maxTries) {
        int x = (int) Rand.range(a.xpos(), a.xmax());
        int y = (int) Rand.range(a.ypos(), a.ymax());
        
        if (couldBlockPathing(propType, x, y)) continue;
        if (! insertProp(x, y, propType, MARK_PROP, false)) {
          continue;
        }
        areaUsed += propArea;
      }
    }
    
  }
  
  
  void insertPropsForCorridor(final Room room) {
    visitWalls(room, VISIT_FLOORS, new Tiling() {
      void tile(Coord[] wall, Coord at, int dir) {
        insertProp(at.x, at.y, room.type.floors, MARK_CORRIDOR, false);
      }
    });
    visitWalls(room, VISIT_NO_CORNERS, new Tiling() {
      void tile(Coord[] wall, Coord at, int dir) {
        int atX = at.x - T_X[dir], atY = at.y - T_Y[dir];
        if (sampleFacing(atX, atY, dir) == MARK_OUTSIDE) {
          insertProp(atX, atY, room.type.door, MARK_DOORS, true);
        }
      }
    });
  }
  
  
  boolean insertProp(
    int atX, int atY, Kind kind, byte markVal, boolean replace
  ) {
    if (kind == null) return false;
    if ((! replace) && (! scene.hasSpace(kind, atX, atY, N))) return false;
    
    scene.addProp(kind, atX, atY, N);
    
    final int w = kind.wide(), h = kind.high();
    for (Coord c : Visit.grid(atX, atY, w, h, 1)) markup(c.x, c.y, markVal);
    return true;
  }
  
  
  
  /**  Other utility methods for iteration over structural features:
    */
  void visitWalls(Room room, int visitMode, Tiling visit) {
    
    if (visitMode == VISIT_FLOORS || visitMode == VISIT_CEILING) {
      final Box2D a = room.area;
      int x = (int) a.xpos(), y = (int) a.ypos();
      int w = (int) a.xdim(), h = (int) a.ydim();
      
      for (Coord c : Visit.grid(x, y, w, h, 1)) {
        visit.tile(room.floor, c, CENTRE);
      }
    }
    else for (int i = 4; i-- > 0;) {
      final Coord wall[] = room.walls[i];
      final int dir = T_ADJACENT[(i + 3) % 4];
      
      if (visitMode == VISIT_ALL) {
        for (Coord at : wall) visit.tile(wall, at, dir);
      }
      if (visitMode == VISIT_NO_CORNERS) {
        for (Coord at : wall) {
          if (at == wall[0] || at == Visit.last(wall)) continue;
          visit.tile(wall, at, dir);
        }
      }
      if (visitMode == VISIT_MIDDLE) {
        Coord at = wall[wall.length / 2];
        visit.tile(wall, at, dir);
      }
      if (visitMode == VISIT_RANDOM) {
        Coord at = wall[1 + Rand.index(wall.length - 2)];
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
    
    ///StringBuffer b = new StringBuffer();
    ///Coord initC = null;
    
    for (Coord c : Visit.perimeter(atX, atY, w, h)) {
      final byte mark = sampleFacing(c.x, c.y, CENTRE);
      final boolean blocked = mark != MARK_FLOOR && mark != MARK_CORRIDOR;
      
      ///if (initC == null) initC = new Coord(c);
      ///b.append(mark+" ");
      
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
      ///I.say("Perimeter okay for "+propType+" at "+atX+"/"+atY);
      ///I.say("  Blockages: "+numBlockages+", init coord: "+initC);
      ///I.say("  "+b);
      return false;
    }
  }
  
}

