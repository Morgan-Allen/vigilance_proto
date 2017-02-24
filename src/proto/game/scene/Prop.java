

package proto.game.scene;
import proto.common.*;
import proto.game.person.Person;
import proto.game.world.*;
import proto.view.common.*;
import proto.view.scene.*;
import proto.util.*;

import java.awt.Color;
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
    if (! back) facing = (8 - facing) % 8;
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
    final int x, final int y, final int facing, final int margin
  ) {
    final int w = Nums.max(1, type.wide()), h = Nums.max(1, type.high());
    final Visit <Coord> base = Visit.grid(
      0 - margin, 0 - margin, w + (margin * 2), h + (margin * 2), 1
    );
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
    Scene scene, PropType kind, int x, int y, int facing
  ) {
    for (Tile under : tilesUnder(kind, scene, x, y, facing, 0)) {
      if (under == null) return false;
      for (Element e : under.inside()) if (e.isProp()) {
        final Prop p = (Prop) e;
        if (p.wouldBlock(kind, x, y, facing)) return false;
      }
    }
    return true;
  }
  
  
  public boolean wouldBlock(PropType type, int x, int y, int facing) {
    if (kind().thin  () || type.thin  ()) return false;
    if (kind().effect() || type.effect()) return false;
    return kind().blockLevel() > 0 == type.blockLevel() > 0;
  }
  
  
  public boolean enterScene(Scene scene, int x, int y, int facing) {
    if (origin != null) I.complain("Already in scene!");
    this.origin = scene.tileAt(x, y);
    this.facing = facing;
    if (origin == null) I.complain("Origin outside bounds!");
    
    final PropType kind = kind();
    for (Tile under : tilesUnder(kind, scene, x, y, facing, 0)) {
      if (under == null) continue;
      for (Element e : under.inside()) if (e.isProp()) {
        final Prop p = (Prop) e;
        if (p.wouldBlock(kind, x, y, facing)) p.exitScene();
      }
      
      under.setInside(this, true);
      under.wipePathing();
    }
    
    for (Tile under : tilesUnder(kind, scene, x, y, facing, 1)) {
      if (under != null) under.updatePathing();
    }
    
    scene.props.add(this);
    return true;
  }
  
  
  public boolean exitScene() {
    if (origin == null) I.complain("Never entered scene!");
    final Scene scene = origin.scene;
    final Tile at = origin;
    
    //  TODO:  Check this to see what happens immediately after removal, under
    //  a specific tile.

    final PropType kind = kind();
    for (Tile under : tilesUnder(kind, scene, at.x, at.y, facing, 0)) {
      if (under == null) continue;
      under.setInside(this, false);
      under.wipePathing();
    }
    
    for (Tile under : tilesUnder(kind, scene, at.x, at.y, facing, 1)) {
      if (under != null) under.updatePathing();
    }
    
    this.origin = null;
    this.facing = -1;
    scene.props.remove(this);
    return true;
  }
  
  
  public boolean occupies(int relX, int relY, int facing) {
    PropType type = kind();
    Coord c = rotate(new Coord(relX, relY), this.facing, true);
    relX = c.x;
    relY = c.y;
    if (facing != CENTRE) facing = (facing + 8 - this.facing) % 8;
    int w = type.wide(), h = type.high();
    
    if (h == 0 && relY == 0 && facing == W) return true;
    if (w == 0 && relX == 0 && facing == N) return true;
    if (relX >= 0 && relX < w && relY >= 0 && relY < h) return true;
    return false;
  }
  
  
  
  /**  Special manipulations-
    */
  public Action manipulationFor(Person person, Scene scene) {
    return kind().manipulationFor(person, scene, this);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void renderTo(Scene scene, SceneView view, Surface s, Graphics2D g) {
    int truW = kind().wide(), truH = kind().high();
    float midX = origin.x, midY = origin.y, scale = kind().spriteScale();
    float w = Nums.max(1, truW), h = Nums.max(1, truH);
    
    //  TODO:  Have a centre() method which returns this coordinate?
    //  NOTE:  The zero-dimensions offsets are intended for walls and doors (a
    //  slight hack.)
    float hw = (w - 1) / 2f, hh = (h - 1) / 2f;
    if (truW == 0) hw -= 0.05f;
    if (truH == 0) hh -= 0.05f;
    
    if (facing == N) { midX += hw; midY += hh; }
    if (facing == E) { midX -= hh; midY += hw; }
    if (facing == S) { midX -= hw; midY -= hh; }
    if (facing == W) { midX += hh; midY -= hw; }
    
    w *= scale;
    h *= scale;
    view.renderSprite(midX, midY, w, h, facing * 45, kind().sprite(), g);
    
    if (blockLevel() > 0 && GameSettings.viewSceneBlocks) {
      for (Tile t : tilesUnder(kind(), scene, origin.x, origin.y, facing, 0)) {
        if (t == null) continue;
        float radius = Nums.min(Nums.max(w, h), 3) / 4f;
        Color c = t == origin ? Color.RED : Color.YELLOW;
        view.renderColor(t.x, t.y, radius, radius, true, c, g);
      }
    }
  }
  
  
  public int renderPriority() {
    return blockLevel() + (kind().thin() || kind().effect() ? 5 : 0);
  }
}







