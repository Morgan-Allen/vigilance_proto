

package proto.game.scene;
import proto.util.*;



public class SceneGen {
  
  final static byte
    MARK_NONE     = 0,
    MARK_CORRIDOR = 1,
    MARK_WALLS    = 2,
    MARK_DOORS    = 3,
    MARK_FLOOR    = 4
  ;
  
  Scene scene;
  byte markup[][];
  
  int minRoomSize = 4;
  float corWideFraction = 0.125f, maxSplitFrac = 0.5f;
  
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
  public void subdivideAreas(SceneType parent, Box2D area) {
    //
    //  TODO:  First, check to see if an existing room will fit inside here?

    boolean longX = area.xdim() > area.ydim();
    float maxSide = area.maxSide();
    if (maxSide <= minRoomSize) return;
    
    if (verbose) I.say("Attempting to subdivide: "+area);
    Box2D a1 = new Box2D().setTo(area), a2 = new Box2D().setTo(area);
    
    float splitFrac = maxSide - (minRoomSize * 2);
    splitFrac = Nums.min(splitFrac, maxSide * maxSplitFrac);
    int split = (int) (minRoomSize + (Rand.num() * splitFrac));
    
    if (longX) {
      a1.xdim(split);
      a2.incWide(0 - split);
      a2.incX(split);
    }
    else {
      a1.ydim(split);
      a2.incHigh(0 - split);
      a2.incY(split);
    }

    SplitCheck check1 = attemptSplitCheck(parent, a1);
    SplitCheck check2 = attemptSplitCheck(parent, a2);
    
    if (check1.valid && check2.valid) {
      
      if (check1.room != null) performRoomFill(a1, check1.room);
      else subdivideAreas(parent, a1);
      
      if (check2.room != null) performRoomFill(a2, check2.room);
      else subdivideAreas(parent, a2);
      
      return;
    }
    //
    //  If that fails, you'll need to insert a corridor between the two areas
    //  and go again.
    if (verbose) I.say("Initial split was not successful.  Adding corridor.");
    
    Box2D corridor = new Box2D().setTo(area);
    int corWide = Nums.max(1, (int) (maxSide * corWideFraction));
    int hCW = corWide / 2;
    if (longX) {
      a1.incWide(-hCW);
      a2.incX(corWide - hCW);
      a2.incWide(hCW - corWide);
      corridor.incX(a1.xdim());
      corridor.xdim(corWide);
    }
    else {
      a1.incHigh(-hCW);
      a2.incY(corWide - hCW);
      a2.incHigh(hCW - corWide);
      corridor.incY(a1.ydim());
      corridor.ydim(corWide);
    }
    markArea(corridor, MARK_CORRIDOR);
    //
    //  Then, check once more to see if it's possible to fill the sub-areas.
    //  And if that's still impossible, subdivide.
    SplitCheck check3 = attemptSplitCheck(parent, a1);
    SplitCheck check4 = attemptSplitCheck(parent, a2);
    
    if (check3.valid && check3.room != null) performRoomFill(a1, check3.room);
    else subdivideAreas(parent, a1);
    
    if (check4.valid && check4.room != null) performRoomFill(a2, check4.room);
    else subdivideAreas(parent, a2);
  }
  
  
  
  /**  
    */
  static class SplitCheck { SceneType room; boolean valid; }
  
  SplitCheck attemptSplitCheck(SceneType parent, Box2D area) {
    SplitCheck check = new SplitCheck();
    
    if (canFillRoom(parent, area, check) && hasCorridorOnSide(area, false)) {
      check.valid = true;
    }
    else if (hasCorridorOnSide(area, true)) {
      check.valid = true;
    }
    else check.valid = false;
    
    if (verbose) {
      Object room = check.room;
      if (! check.valid)     I.say("  Cannot split:     "+area);
      else if (room == null) I.say("  Should subdivide: "+area);
      else                   I.say("  Will place room:  "+room+" in "+area);
    }
    return check;
  }
  
  
  boolean canFillRoom(SceneType parent, Box2D area, SplitCheck check) {
    if (area.maxSide() > 8) return false;
    
    //  TODO:  Elaborate on this...
    check.room = parent;
    return true;
  }
  
  
  boolean hasCorridorOnSide(Box2D area, boolean longSideOnly) {
    int x = (int) area.xpos(), y = (int) area.ypos();
    int w = (int) area.xdim(), h = (int) area.ydim();
    boolean longX = area.xdim() > area.ydim();
    boolean screenX = longSideOnly &&   longX;
    boolean screenY = longSideOnly && ! longX;
    
    for (Coord c : Visit.perimeter(x, y, w, h)) try {
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
    for (Coord c : Visit.perimeter(x + 1, y + 1, w - 2, h - 2)) {
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








