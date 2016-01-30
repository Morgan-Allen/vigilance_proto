

package proto.game.scene;
import static proto.util.TileConstants.*;

import proto.common.Session;
import proto.common.Session.Saveable;
import proto.game.person.Person;



public class Tile implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public Scene scene;
  final public int x, y;
  
  Prop prop;
  Person standing;
  
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
    standing = (Person) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(scene);
    s.saveInt(x);
    s.saveInt(y);
    s.saveObject(prop);
    s.saveObject(standing);
  }
  
  
  
  /**  Public query methods-
    */
  public Prop prop() {
    return prop;
  }
  
  
  public Person standing() {
    return standing;
  }
  
  
  public boolean blocked() {
    if (prop != null && prop.kind.blockPath()) return true;
    if (standing != null) return true;
    
    //  TODO:  ALLOW UNCONSCIOUS PERSONS TO NOT BLOCK PATHING- BUT FOR THAT TO
    //  WORK YOU'LL NEED TO ALLOW MULTIPLE OBJECTS IN THE SAME TILE.?
    
    return false;
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
    if (is) {
      if (standing == null) standing = p;
    }
    else {
      if (p == standing) standing = null;
    }
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Tile at "+x+"|"+y;
  }
}





