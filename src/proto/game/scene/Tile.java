

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import static proto.util.TileConstants.*;



public class Tile implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public Scene scene;
  final public int x, y;
  
  private Prop prop;
  private Stack <Person> inside = new Stack();
  private int blockState = -1;
  
  Object flag;
  
  
  Tile(Scene s, int x, int y) {
    this.scene = s;
    this.x = x;
    this.y = y;
  }
  
  
  public Tile(Session s) throws Exception {
    s.cacheInstance(this);
    scene    = (Scene) s.loadObject();
    x        = s.loadInt();
    y        = s.loadInt();
    prop     = (Prop) s.loadObject();
    s.loadObjects(inside);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(scene);
    s.saveInt(x);
    s.saveInt(y);
    s.saveObject(prop);
    s.saveObjects(inside);
  }
  
  
  
  /**  Public query methods-
    */
  public Prop prop() {
    return prop;
  }
  
  
  public Series <Person> inside() {
    return inside;
  }
  
  
  public boolean blocked() {
    if (blockState == -1 || true) {
      blockState = 0;
      if (prop != null && prop.kind.blockPath()) blockState = 1;
      for (Person p : inside) if (p.health.conscious()) blockState = 1;
    }
    return blockState == 1;
  }
  
  
  public Tile[] tilesAdjacent() {
    final Tile t[] = new Tile[T_INDEX.length];
    for (int n : T_INDEX) {
      t[n] = scene.tileAt(x + T_X[n], y + T_Y[n]);
    }
    return t;
  }
  
  
  
  /**  Modifying state-
    */
  public void setInside(Person p, boolean is) {
    if (is) inside.include(p);
    else inside.remove(p);
    blockState = -1;
  }
  
  
  public void setProp(Prop prop) {
    this.prop = prop;
    blockState = -1;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Tile at "+x+"|"+y;
  }
}





