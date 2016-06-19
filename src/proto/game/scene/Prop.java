

package proto.game.scene;
import proto.common.Kind;
import proto.common.Session;
import proto.common.Session.Saveable;
import proto.util.*;



public class Prop implements Session.Saveable {

  Kind kind;
  Tile origin;
  
  
  Prop(Kind kind) {
    this.kind = kind;
  }
  
  
  public Prop(Session s) throws Exception {
    s.cacheInstance(this);
    kind   = (Kind) s.loadObject();
    origin = (Tile) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind);
    s.saveObject(origin);
  }
  
  
  public Kind kind() {
    return kind;
  }
  
  
  public Tile origin() {
    return origin;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return kind.name();
  }
}

