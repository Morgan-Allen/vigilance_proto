

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.view.scene.*;
import proto.util.*;

import java.awt.Graphics2D;



public class Prop extends Element implements TileConstants {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Tile origin;
  int facing = TileConstants.N;
  
  
  Prop(PropType kind, World world) {
    super(kind, world);
  }
  
  
  public Prop(Session s) throws Exception {
    super(s);
    origin = (Tile) s.loadObject();
    facing = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(origin);
    s.saveInt(facing);
  }
  
  
  public Tile origin() {
    return origin;
  }
  
  
  public int facing() {
    return facing;
  }
  
  
  
  /**  Placement, removal and occupation methods-
    */
  public static Visit <Coord> coordsUnder(Kind type, int x, int y, int facing) {
    int w = type.wide(), h = type.high();
    if (facing == W || facing == E) {
      w = type.high();
      h = type.wide();
    }
    if (facing == W) x -= w - 1;
    if (facing == E) y -= h - 1;
    if (facing == S) { y -= h - 1; x -= w - 1; }
    return Visit.grid(x, y, w, h, 1);
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void renderTo(Scene scene, SceneView view, Surface s, Graphics2D g) {
    float midX = origin.x, midY = origin.y;
    float w = kind().wide(), h = kind().high();
    
    if (facing == E) { midY += w; }
    if (facing == W) { midX += h; }
    if (facing == S) { midX += w; midY += h; }
    
    view.renderAt(midX, midY, w, h, kind().sprite(), facing * -45, null, g);
  }
  
  
  public int renderPriority() {
    return blockLevel();
  }
}






