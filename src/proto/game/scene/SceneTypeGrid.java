

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneTypeGrid extends SceneType {
  
  
  /**  Data fields and construction-
    */
  final int wide, high;
  
  byte fillMask[][];
  static class Placing {
    PropType type;
    int x, y, facing;
  }
  List <Placing> placings = new List();
  
  
  public SceneTypeGrid(
    String name, String ID,
    int wide, int high
  ) {
    super(
      name, ID,
      MIN_WIDE, wide, MAX_WIDE, wide,
      MIN_HIGH, high, MAX_HIGH, high
    );
    this.wide = wide;
    this.high = high;
    this.fillMask = new byte[wide][high];
  }
  
  
  public void attachPlacing(PropType type, int x, int y, int facing) {
    if (type == null) return;
    Placing p = new Placing();
    p.type = type;
    p.x = x;
    p.y = y;
    p.facing = facing;
    placings.add(p);
    //
    //  We use the fill-mask to check for border-compatibility later on (see
    //  below.)
    int mask = 1;
    if (type.thin() && type.blockLevel() == Kind.BLOCK_NONE) mask = -1;
    if (type.thin() && type.blockSight() == false          ) mask = -1;
    for (Coord c : Prop.coordsUnder(type, x, y, facing)) try {
      fillMask[c.x][c.y] = (byte) mask;
    }
    catch (ArrayIndexOutOfBoundsException e) {}
  }
  
  
  public SceneTypeGrid(
    String name, String ID,
    PropType floor, PropType propTypes[],
    int wide, int high, byte typeGrid[][]
  ) {
    this(name, ID, wide, high);
    if (wide != typeGrid[0].length) I.complain("WRONG WIDTH" );
    if (high != typeGrid   .length) I.complain("WRONG HEIGHT");
    
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      int index = typeGrid[c.x][c.y];
      PropType type = index == 0 ? floor : propTypes[index - 1];
      if (type != floor && floor != null) attachPlacing(floor, c.y, c.x, N);
      if (type != null                  ) attachPlacing(type , c.y, c.x, N);
    }
  }
  
  
  
  /**  Utility methods for either scene-generation or scene-insertion.
    */
  private void rotateCoord(Coord c, int facing) {
    int offH = wide - 1, offV = high - 1, x, y;
    if (facing == N) {
      x = c.x;
      y = c.y;
    }
    else if (facing == E) {
      x = offV - c.y;
      y = c.x;
    }
    else if (facing == W) {
      x = c.y;
      y = offH - c.x;
    }
    else {
      x = offH - c.x;
      y = offV - c.y;
    }
    c.x = x;
    c.y = y;
  }
  
  
  Box2D borderBounds(
    Scene scene, int offX, int offY, int facing, int resolution
  ) {
    Box2D bound = null;
    int wideB = Nums.round(wide, resolution, true) - 1;
    int highB = Nums.round(high, resolution, true) - 1;
    
    ///I.say("\nGetting bounds...");
    for (Coord c : Visit.grid(0, 0, 2, 2, 1)) {
      c.x *= wideB;
      c.y *= highB;
      ///I.say("  "+c);
      rotateCoord(c, facing);
      ///I.add(" ->"+c);
      c.x += offX;
      c.y += offY;
      ///I.add(" ->"+c);
      
      if (scene.tileAt(c.x, c.y) == null) return null;
      
      if (bound == null) bound = new Box2D(c.x, c.y, 0, 0);
      else bound.include(c.x, c.y, 0);
    }
    
    bound.incHigh(1);
    bound.incWide(1);
    ///I.say("\nFinal bound: "+bound);
    return bound;
  }
  
  
  boolean checkBordering(
    Scene scene, int offX, int offY, int facing,
    int resolution
  ) {
    Coord temp = new Coord();
    if (borderBounds(scene, offX, offY, facing, resolution) == null) {
      return false;
    }
    
    for (Coord c : Visit.perimeter(0, 0, wide, high)) {
      temp.setTo(c);
      rotateCoord(temp, facing);
      int tx = temp.x + offX, ty = temp.y + offY, gx = c.x, gy = c.y, dir = 0;
      if (c.x < 0          ) { gx++; dir = W; }
      if (c.y < 0          ) { gy++; dir = S; }
      if (c.x >= resolution) { gx--; dir = E; }
      if (c.y >= resolution) { gy--; dir = N; }
      if (c.x != gx && c.y != gy) {
        continue;
      }
      //
      //  Check to ensure that no doors or windows are blocked.
      //boolean blockG = fillMask[gx][gy] >   0;
      //
      //  TODO:  In future, you may want to implement a more jigsaw-esque
      //  approach, depending on the wall-types specs for a grid-unit.
      boolean isDoor = fillMask[gx][gy] == -1;
      Tile    at     = scene.tileAt(tx, ty);
      boolean blockT = at == null ? true  : (at.blocked() || at.opaque());
      if (isDoor && blockT) return false;
    }
    
    return true;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int w, int h, boolean testing
  ) {
    Coord temp = new Coord();
    for (Placing p : placings) {
      temp.x = p.x;
      temp.y = p.y;
      rotateCoord(temp, facing);
      temp.x += offX;
      temp.y += offY;
      
      int propDir = (p.facing + facing) % 8;
      if (p.type == floors) propDir = N;
      
      if (Prop.hasSpace(scene, p.type, temp.x, temp.y, propDir)) {
        scene.addProp(p.type, temp.x, temp.y, propDir);
      }
    }
  }
  
  
  
  /**  Actual scene-generation-
    */
  public Scene generateScene(
    World world, int wide, int high, boolean testing
  ) {
    final Scene scene = new Scene(world, wide, high);
    scene.setupScene(testing);
    applyToScene(scene, 0, 0, N, wide, high, testing);
    return scene;
  }
}






