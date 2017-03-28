

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneTypeFixed extends SceneType {
  
  
  /**  Data fields and construction-
    */
  final int wide, high;
  
  static class Placing {
    PropType type;
    int x, y, facing;
  }
  List <Placing> placings = new List();
  
  
  public SceneTypeFixed(
    String name, String ID,
    int wide, int high
  ) {
    super(
      name, ID,
      MIN_SIZE, Nums.max(wide, high),
      MAX_SIZE, Nums.max(wide, high)
    );
    this.wide = wide;
    this.high = high;
  }
  
  
  public void attachPlacing(PropType type, int x, int y, int facing) {
    if (type == null) return;
    Placing p = new Placing();
    p.type = type;
    p.x = x;
    p.y = y;
    p.facing = facing;
    placings.add(p);
  }
  
  
  public SceneTypeFixed(
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
  boolean checkBordering(
    Scene scene, int offX, int offY, int resolution
  ) {
    int offGX = (wide - resolution) / 2;
    int offGY = (high - resolution) / 2;
    
    for (Coord c : Visit.perimeter(offGX, offGY, resolution, resolution)) {
      int tx = c.x + offX, ty = c.y + offY, gx = c.x, gy = c.y;
      if (c.x < 0) gx--; if (c.x >= resolution) gx++;
      if (c.y < 0) gy--; if (c.y >= resolution) gy++;
      if (c.x != gx) {
        if (! checkBorderAt(gx, c.y, scene, tx, ty)) return false;
      }
      if (c.y < gy) {
        if (! checkBorderAt(c.x, gy, scene, tx, ty)) return false;
      }
    }
    return true;
  }
  
  
  boolean checkBorderAt(int gx, int gy, Scene scene, int tx, int ty) {
    //Kind type = propType(gx, gy);
    //Tile at = scene.tileAt(tx, ty);
    //boolean blockG = type == null ? false : type.blockLevel() > 0;
    //boolean blockT = at   == null ? true  : at.blocked();
    
    //  TODO:  FIX THIS!
    return true;
    //return blockG == blockT;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int resolution,
    boolean forTesting
  ) {
    offX -= (wide - resolution) / 2;
    offY -= (high - resolution) / 2;
    Mat2D rot = new Mat2D().setIdentity().rotateAndRound(facing * -45);
    
    if (facing == E) { offX += high - 1; }
    if (facing == W) { offY += wide - 1; }
    if (facing == S) { offX += wide - 1; offY += high - 1; }
    Vec2D temp = new Vec2D();
    
    for (Placing p : placings) {
      rot.transform(temp.set(p.x, p.y));
      int sx = (int) (temp.x + offX), sy = (int) (temp.y + offY);
      int propDir = (p.facing + facing) % 8;
      if (Prop.hasSpace(scene, p.type, sx, sy, propDir)) {
        scene.addProp(p.type, sx, sy, propDir);
      }
    }
  }
  
  
  
  /**  Actual scene-generation-
    */
  public Scene generateScene(World world, int size, boolean forTesting) {
    size = Nums.max(wide, high) + 2;
    final Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 0, 0, N, size, forTesting);
    return scene;
  }
}


