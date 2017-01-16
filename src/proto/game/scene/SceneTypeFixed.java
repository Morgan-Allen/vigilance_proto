

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;


public class SceneTypeFixed extends SceneType {
  
  
  /**  ...and constructor definition for fixed scene layouts.
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
    for (Coord c : Visit.perimeter(0, 0, resolution, resolution)) {
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
    boolean blockT = at   == null ? true  : at.blocked();
    return blockG == blockT;
  }
  
  
  Kind propType(int gx, int gy) {
    try {
      byte index = fixedLayoutGrid[gy][gx];
      return index >= 0 ? fixedPropTypes[index] : null;
    }
    catch(ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  void applyToScene(Scene scene, int offX, int offY) {
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      Kind type = propType(c.x, c.y);
      if (type == null) continue;
      if (! scene.hasSpace(type, c.x + offX, c.y + offY)) continue;
      scene.addProp(type, c.x + offY, c.y + offY);
    }
  }
  
  
  
  /**  Actual scene-generation-
    */
  public Scene generateScene(World world, int size, boolean forTesting) {
    size = Nums.max(wide, high) + 2;
    final Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 1, 1);
    return scene;
  }
}





