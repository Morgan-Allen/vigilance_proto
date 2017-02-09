

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
  private static Coord rotate(Coord c, int facing, boolean back) {
    if (back) facing = (8 - facing) % 8;
    int x = 0, y = 0;
    switch (facing) {
      case(CENTRE):
      case(N): x =  c.x; y =  c.y; break;
      case(E): x =  c.y; y = -c.x; break;
      case(S): x = -c.x; y = -c.y; break;
      case(W): x = -c.y; y =  c.x; break;
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
        Coord c = rotate(base.next(), facing, false);
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
    this.origin = scene.tileAt(x, y);
    this.facing = facing;
    if (origin == null) I.complain("Origin outside bounds!");
    
    final PropType kind = kind();
    boolean verbose = false;
    
    if (kind.wide() > 1 || kind.high() > 1) {
      I.say("Adding prop: "+kind);
      verbose = true;
    }
    
    for (Tile under : tilesUnder(kind, scene, x, y, facing)) {
      if (under == null) continue;
      for (Element e : under.inside()) if (e.wouldBlock(kind) && e.isProp()) {
        ((Prop) e).exitScene();
      }
      
      if (verbose) I.say("  entered at "+under);
      under.setInside(this, true);
      under.wipePathing();
    }
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
  
  
  public boolean occupies(int relX, int relY, int facing) {
    PropType type = kind();
    Coord c = rotate(new Coord(relX, relY), this.facing, true);
    relX = c.x;
    relY = c.y;
    facing = (facing + 8 - this.facing) % 8;
    int w = type.wide(), h = type.high();
    
    //  TODO:  These will have to be swapped (N and S are actually along the x-
    //  axis!)
    if (h == 0) {
      if (relY == 0 && facing == N) return true;
      if (relY == 1 && facing == S) return true;
    }
    if (w == 0) {
      if (relX == 0 && facing == E) return true;
      if (relX == 1 && facing == W) return true;
    }
    if (relX >= 0 && relX < w && relY >= 0 && relY < h) {
      return true;
    }
    return false;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void renderTo(Scene scene, SceneView view, Surface s, Graphics2D g) {
    float midX = origin.x, midY = origin.y;
    float w = kind().wide(), h = kind().high();
    
    if (facing == E) { midY += 1; }
    if (facing == W) { midX += 1; }
    if (facing == S) { midX += 1; midY += 1; }
    
    view.renderAt(midX, midY, w, h, kind().sprite(), facing * -45, null, g);
  }
  
  
  public int renderPriority() {
    return blockLevel();
  }
}









