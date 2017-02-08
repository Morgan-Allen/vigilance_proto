

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
  
  
  public PropType kind() {
    return (PropType) kind;
  }
  
  
  public Tile origin() {
    return origin;
  }
  
  
  public int facing() {
    return facing;
  }
  
  
  
  /**  Placement, removal and occupation methods-
    */
  private static Coord translate(Coord c, int facing, boolean invert) {
    int x = 0, y = 0, m = invert ? -1 : 1;
    switch (facing) {
      case(N): x =  c.x * m; y =  c.y * m; break;
      case(E): x =  c.y * m; y = -c.x * m; break;
      case(S): x = -c.x * m; y = -c.y * m; break;
      case(W): x = -c.y * m; y =  c.x * m; break;
    }
    c.x = x;
    c.y = y;
    return c;
  }
  
  
  private static Visit <Tile> tilesUnder(
    PropType type, final Scene scene,
    final int x, final int y, final int facing
  ) {
    final int w = Nums.max(1, type.wide()), h = Nums.max(1, type.high());
    final Visit <Coord> base = Visit.grid(0, 0, w, h, 1);
    return new Visit <Tile> () {
      
      public boolean hasNext() {
        return base.hasNext();
      }
      
      public Tile next() {
        Coord c = translate(base.next(), facing, false);
        return scene.tileAt(c.x + x, c.y + y);
      }
    };
  }
  
  
  public static boolean hasSpace(
    Scene scene, PropType type, int x, int y, int facing
  ) {
    for (Tile under : tilesUnder(type, scene, x, y, facing)) {
      if (under == null) return false;
      for (Element e : under.inside()) if (e.wouldBlock(type)) return false;
    }
    return true;
  }
  
  
  public boolean enterScene(Scene scene, int x, int y, int facing) {
    if (origin != null) I.complain("Already in scene!");
    final PropType kind = kind();
    
    for (Tile under : tilesUnder(kind, scene, x, y, facing)) {
      if (under == null) continue;
      for (Element e : under.inside()) if (e.wouldBlock(kind) && e.isProp()) {
        ((Prop) e).exitScene();
      }
      under.setInside(this, true);
      under.wipePathing();
    }
    
    this.origin = scene.tileAt(x, y);
    this.facing = facing;
    scene.props.add(this);
    
    for (Tile under : tilesUnder(kind, scene, x, y, facing)) {
      if (under != null) under.updatePathing();
    }
    return true;
  }
  
  
  public boolean exitScene() {
    if (origin == null) I.complain("Never entered scene!");
    final Scene scene = origin.scene;
    final Tile at = origin;
    
    for (Tile under : tilesUnder(kind(), scene, at.x, at.y, facing)) {
      if (under == null) continue;
      under.setInside(this, false);
      under.wipePathing();
    }
    
    this.origin = null;
    this.facing = -1;
    scene.props.remove(this);
    
    for (Tile under : tilesUnder(kind(), scene, at.x, at.y, facing)) {
      if (under != null) under.updatePathing();
    }
    return true;
  }
  
  
  public int blockageAtOffset(int relX, int relY, int facing) {
    PropType type = kind();
    Coord c = translate(new Coord(relX, relY), facing, true);
    relX = c.x;
    relY = c.y;
    facing = (facing + 8 - this.facing) % 8;
    int w = type.wide(), h = type.high(), block = type.blockLevel();
    
    if (h == 0) {
      if (relY == 0 && facing == N) return block;
      if (relY == 1 && facing == S) return block;
    }
    if (w == 0) {
      if (relX == 0 && facing == E) return block;
      if (relX == 1 && facing == W) return block;
    }
    if (relX >= 0 && relX < w && relY >= 0 && relY < h) {
      return block;
    }
    return 0;
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






