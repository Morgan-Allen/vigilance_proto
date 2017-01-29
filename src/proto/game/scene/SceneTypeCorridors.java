

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import static proto.game.scene.SceneGen.*;
import proto.util.*;



public class SceneTypeCorridors extends SceneType {
  
  
  /**  Public constructor definition and generation methods-
    */
  public SceneTypeCorridors(
    String name, String ID, Object... args
  ) {
    super(name, ID, args);
  }
  

  public Scene generateScene(World world, int size, boolean forTesting) {
    final Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 2, 2, N, size - 4, forTesting);
    return scene;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int limit, boolean forTesting
  ) {
    final SceneGenCorridors g = new SceneGenCorridors();
    final Box2D area = new Box2D(offX, offY, limit, limit);
    g.scene = scene;
    g.verbose = forTesting;
    attemptRoomDivision(g, this, area);
    
    placeCurtainWall(g, this, area);
    for (Room room : g.rooms) {
      insertPropsForRoom(g, room);
    }
    for (Room room : g.corridors) {
      insertPropsForCorridor(g, room);
    }
  }
  
  
  
  /**  Internal utility methods and data structures-
    */
  static class SceneGenCorridors extends SceneGen {
    int minRoomSize = 4;
    float corWideFraction = 0.125f;
    float maxSplitFrac = 0.5f;
  }
  
  
  void attemptRoomDivision(SceneGenCorridors g, SceneType parent, Box2D area) {
    //
    //  Firstly, check if the area itself is fit to populate with one of the
    //  scene's room-types:
    AreaCheck check0 = attemptAreaCheck(g, parent, area);
    if (check0.valid && check0.room != null) {
      g.performRoomFill(area, check0.room);
      return;
    }
    //
    //  Failing that, try subdividing the area into two different areas along
    //  the longer axis, and try to ensure that each can be filled with a room
    //  that has access to a corridor- or failing that, that each has access to
    //  a corridor along it's longer side (see below.)
    boolean longX = area.xdim() > area.ydim();
    int maxSide = (int) area.maxSide();
    if (maxSide <= g.minRoomSize) return;
    
    int corWide = Nums.max(1, (int) (maxSide * g.corWideFraction));
    float splitFrac = maxSide - ((g.minRoomSize * 2) + corWide + 2);
    splitFrac = Nums.clamp(splitFrac, 0, maxSide * g.maxSplitFrac);
    int split = (int) (((maxSide - splitFrac) / 2) + (Rand.num() * splitFrac));
    
    if (g.verbose) {
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
    
    AreaCheck check1 = attemptAreaCheck(g, parent, a1);
    AreaCheck check2 = attemptAreaCheck(g, parent, a2);
    
    if (check1.valid && check2.valid) {
      
      if (check1.room != null) g.performRoomFill(a1, check1.room);
      else attemptRoomDivision(g, parent, a1);
      
      if (check2.room != null) g.performRoomFill(a2, check2.room);
      else attemptRoomDivision(g, parent, a2);
      
      return;
    }
    //
    //  If that's not possible, you'll need to insert a corridor between the
    //  two areas (to ensure ease of access) and try again.
    if (g.verbose) I.say("Initial split was not successful, adding corridor");
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
    
    g.performCorridorFill(ac, parent);
    attemptRoomDivision(g, parent, a1);
    attemptRoomDivision(g, parent, a2);
  }
  
  
  
  /**  Utility methods for checking the validity of room placement-
    */
  AreaCheck attemptAreaCheck(
    SceneGenCorridors g, SceneType parent, Box2D area
  ) {
    AreaCheck check = new AreaCheck();
    if (
      g.canFillRoom(parent, area, check) &&
      hasCorridorAccess(g, area, false)
    ) {
      check.valid = true;
    }
    else if (hasCorridorAccess(g, area, true)) {
      check.valid = true;
    }
    else {
      check.valid = false;
    }
    
    if (g.verbose) {
      Object room = check.room;
      if      (! check.valid) I.say("  Cannot split:     "+area);
      else if (room == null ) I.say("  Should subdivide: "+area);
      else                    I.say("  Will place room:  "+room+" in "+area);
    }
    return check;
  }
  
  
  boolean hasCorridorAccess(
    SceneGenCorridors g, Box2D area, boolean longSideOnly
  ) {
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    boolean longX = area.xdim() > area.ydim();
    boolean screenX = longSideOnly &&   longX;
    boolean screenY = longSideOnly && ! longX;
    
    for (Coord c : Visit.perimeter(x - 1, y - 1, w + 2, h + 2)) try {
      if (screenY && (c.y < y || c.y >= (y + h))) continue;
      if (screenX && (c.x < x || c.x >= (x + w))) continue;
      
      byte markVal = g.markup[c.x][c.y];
      if (markVal == MARK_CORRIDOR) return true;
    } catch (ArrayIndexOutOfBoundsException e) { continue; }
    
    return false;
  }
  
  
  
  /**  Utility methods for furniture placement-
    */
  //  TODO:  In future, you might want to do a proper perimeter trace...
  void placeCurtainWall(
    SceneGenCorridors g, SceneType buildingType, Box2D area
  ) {
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    
    for (Coord c : Visit.perimeter(x, y, w, h)) {
      g.insertProp(c.x, c.y, buildingType.borders, MARK_OUT_WALL, true);
    }
    for (Coord c : Visit.perimeter(x - 1, y - 1, w + 2, h + 2)) {
      g.markup(c.x, c.y, MARK_OUTSIDE);
    }
  }
  
  
  void insertPropsForRoom(final SceneGenCorridors g, final Room room) {
    g.visitWalls(room, VISIT_FLOORS, new Tiling() {
      void tile(Wall wall, Coord at, int dir) {
        g.insertProp(at.x, at.y, room.type.floors, MARK_FLOOR, false);
      }
    });
    g.visitWalls(room, VISIT_ALL, new Tiling() {
      void tile(Wall  wall, Coord at, int dir) {
        g.insertProp(at.x, at.y, room.type.borders, MARK_WALLS, false);
      }
    });
    g.visitWalls(room, VISIT_RANDOM, new Tiling() {
      void tile(Wall wall, Coord at, int dir) {
        if (g.sampleFacing(at.x, at.y, dir) == MARK_CORRIDOR) {
          g.insertProp(at.x, at.y, room.type.door, MARK_DOORS, true);
        }
      }
    });
    g.visitWalls(room, VISIT_MIDDLE, new Tiling() {
      void tile(Wall wall, Coord at, int dir) {
        if (g.sampleFacing(at.x, at.y, dir) == MARK_OUTSIDE) {
          g.insertProp(at.x, at.y, room.type.window, MARK_WINDOW, true);
        }
      }
    });
    //
    //  TODO:  You'll want to use some more sophisticated options for placing
    //  props- e.g, at regular spacing, with varying frequency, next to the
    //  walls, surrounding other props, et cetera.
    //final Box2D a = room.area;
    float roomArea = room.wide * room.high;
    
    for (Kind propType : room.type.props) {
      float areaUsed = 0, maxUsed = (roomArea * 0.5f) / room.type.props.length;
      float propArea = propType.wide() * propType.high();
      int numTries = 0, maxTries = (int) (roomArea * 4f / propArea);
      
      while (areaUsed < maxUsed && numTries++ < maxTries) {
        int x = room.minX + Rand.index(room.wide);
        int y = room.minY + Rand.index(room.high);
        
        if (g.couldBlockPathing(propType, x, y)) continue;
        if (! g.insertProp(x, y, propType, MARK_PROP, false)) {
          continue;
        }
        areaUsed += propArea;
      }
    }
    
  }
  
  
  void insertPropsForCorridor(final SceneGenCorridors g, final Room room) {
    g.visitWalls(room, VISIT_FLOORS, new Tiling() {
      void tile(Wall wall, Coord at, int dir) {
        g.insertProp(at.x, at.y, room.type.floors, MARK_CORRIDOR, false);
      }
    });
    g.visitWalls(room, VISIT_NO_CORNERS, new Tiling() {
      void tile(Wall wall, Coord at, int dir) {
        int atX = at.x - T_X[dir], atY = at.y - T_Y[dir];
        if (g.sampleFacing(atX, atY, dir) == MARK_OUTSIDE) {
          g.insertProp(atX, atY, room.type.door, MARK_DOORS, true);
        }
      }
    });
  }
}











