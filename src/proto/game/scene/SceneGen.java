

package proto.game.scene;
import proto.util.*;



public class SceneGen {
  
  
  /**  Constants, data fields and construction methods-
    */
  final static byte
    MARK_NONE     = 0,
    MARK_CORRIDOR = 1,
    MARK_WALLS    = 2,
    MARK_DOORS    = 3,
    MARK_FLOOR    = 4
  ;
  
  final Scene scene;
  final byte markup[][];
  
  public int minRoomSize = 4;
  public float corWideFraction = 0.125f;
  public float maxSplitFrac = 0.5f;
  public boolean verbose = false;
  
  
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
  
  
  
  
  /**
    */
  public void attemptPopulation(SceneType parent, Box2D area) {
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
    //  a corrider along it's longer side (see below.)
    boolean longX = area.xdim() > area.ydim();
    int maxSide = (int) area.maxSide();
    if (maxSide <= minRoomSize) return;
    
    int corWide = Nums.max(1, (int) (maxSide * corWideFraction));
    float splitFrac = maxSide - ((minRoomSize * 2) + corWide + 2);
    splitFrac = Nums.clamp(splitFrac, 0, maxSide * maxSplitFrac);
    int split = (int) (((maxSide - splitFrac) / 2) + (Rand.num() * splitFrac));
    
    if (verbose) {
      I.say("Attempting to subdivide: "+area);
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
      else attemptPopulation(parent, a1);
      
      if (check2.room != null) performRoomFill(a2, check2.room);
      else attemptPopulation(parent, a2);
      
      return;
    }
    //
    //  If that's not possible, you'll need to insert a corridor between the
    //  two areas (to ensure ease of access) and try again.
    if (verbose) I.say("Initial split was not successful, adding corridor");
    Box2D ac = new Box2D().setTo(area).expandBy(1);
    int hCW = corWide / 2, p5, p6;
    p1 = 0;
    p2 = split - hCW;
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
    markArea(ac, MARK_CORRIDOR);
    attemptPopulation(parent, a1);
    attemptPopulation(parent, a2);
  }
  
  
  
  /**  
    */
  static class AreaCheck { SceneType room; boolean valid; }
  
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
    if (area.maxSide() > 8) return false;
    
    //  TODO:  Elaborate on this...
    check.room = parent;
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
  
  
  
  /**
    */
  void performRoomFill(Box2D area, SceneType room) {
    //  TODO:  Perform the task of actual population with walls, doors, floors,
    //  et cetera...
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    
    for (Coord c : Visit.grid(x, y, w, h, 1)) {
      markup[c.x][c.y] = MARK_FLOOR;
    }
    for (Coord c : Visit.perimeter(x, y, w, h)) {
      markup[c.x][c.y] = MARK_WALLS;
    }
    if (verbose) {
      I.say("Performed room fill!");
      printMarkup();
    }
  }
  
  
  void markArea(Box2D area, byte markVal) {
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    for (Coord c : Visit.grid(x, y, w, h, 1)) {
      markup[c.x][c.y] = markVal;
    }
    
    if (verbose) {
      I.say("Marked with value: "+markVal+", area: "+area);
      printMarkup();
    }
  }
  
}








