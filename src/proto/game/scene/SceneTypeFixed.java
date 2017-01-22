

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneTypeFixed extends SceneType {
  
  
  /**  Data fields and construction-
    */
  final Kind fixedPropTypes[];
  final int wide, high;
  final byte fixedLayoutGrid[][];
  
  
  
  public SceneTypeFixed(
    String name, String ID,
    Kind propTypes[], byte typeGrid[][]
  ) {
    super(name, ID);
    fixedPropTypes = propTypes;
    fixedLayoutGrid = typeGrid;
    wide = fixedLayoutGrid[0].length;
    high = fixedLayoutGrid.length;
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
    boolean blockG = type == null ? false : type.blockPath();
    boolean blockT = at   == null ? true  : at  .blocked  ();
    
    //  TODO:  FIX THIS!
    return true;
    //return blockG == blockT;
  }
  
  
  Kind propType(int gx, int gy) {
    try {
      byte index = fixedLayoutGrid[gy][gx];
      return index >= 0 ? fixedPropTypes[index] : null;
    }
    catch(ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  void applyToScene(
    Scene scene, int offX, int offY, int facing, int resolution
  ) {
    offX -= (wide - resolution) / 2;
    offY -= (high - resolution) / 2;
    
    Mat2D rot = new Mat2D().setIdentity().rotateAndRound(facing * 45);
    if (facing == E) { offY += high - 1; }
    if (facing == W) { offX += wide - 1; }
    if (facing == S) { offX += wide - 1; offY += high - 1; }
    Vec2D temp = new Vec2D();
    
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      Kind type = propType(c.x, c.y);
      if (type == null) continue;
      
      rot.transform(temp.set(c.x, c.y));
      int sx = (int) (temp.x + offX), sy = (int) (temp.y + offY);
      if (! scene.hasSpace(type, sx, sy, facing)) continue;
      scene.addProp(type, sx, sy, facing);
    }
  }
  
  
  
  /**  Actual scene-generation-
    */
  public Scene generateScene(World world, int size, boolean forTesting) {
    size = Nums.max(wide, high) + 2;
    final Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 0, 0, N, size);
    return scene;
  }
}


