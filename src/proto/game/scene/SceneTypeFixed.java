

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneTypeFixed extends SceneType {
  
  
  /**  Data fields and construction-
    */
  final PropType fixedPropTypes[];
  final int wide, high;
  final byte fixedLayoutGrid[][];
  
  
  
  public SceneTypeFixed(
    String name, String ID,
    PropType floor, PropType propTypes[], int wide, int high, byte typeGrid[][]
  ) {
    super(
      name, ID,
      MIN_SIZE, Nums.max(wide, high),
      MAX_SIZE, Nums.max(wide, high)
    );
    fixedPropTypes  = propTypes;
    fixedLayoutGrid = typeGrid;
    this.floors     = floor;
    this.wide       = wide;
    this.high       = high;
    if (wide != typeGrid[0].length) I.complain("WRONG WIDTH" );
    if (high != typeGrid   .length) I.complain("WRONG HEIGHT");
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
    Kind type = propType(gx, gy);
    Tile at = scene.tileAt(tx, ty);
    boolean blockG = type == null ? false : type.blockLevel() > 0;
    boolean blockT = at   == null ? true  : at.blocked();
    
    //  TODO:  FIX THIS!
    return true;
    //return blockG == blockT;
  }
  
  
  PropType propType(int gx, int gy) {
    try {
      byte index = fixedLayoutGrid[gy][gx];
      return index > 0 ? fixedPropTypes[index - 1] : null;
    }
    catch(ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int resolution, boolean forTesting
  ) {
    offX -= (wide - resolution) / 2;
    offY -= (high - resolution) / 2;
    
    Mat2D rot = new Mat2D().setIdentity().rotateAndRound(facing * 45);
    if (facing == E) { offY += high - 1; }
    if (facing == W) { offX += wide - 1; }
    if (facing == S) { offX += wide - 1; offY += high - 1; }
    Vec2D temp = new Vec2D();
    
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      rot.transform(temp.set(c.x, c.y));
      int sx = (int) (temp.x + offX), sy = (int) (temp.y + offY);
      
      scene.addProp(floors, sx, sy, facing);
      PropType type = propType(c.x, c.y);
      if (type == null || ! scene.hasSpace(type, sx, sy, facing)) continue;
      scene.addProp(type, sx, sy, facing);
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


