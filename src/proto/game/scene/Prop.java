

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class Prop extends Element {
  
  
  Tile origin;
  
  
  Prop(Kind kind, World world) {
    super(kind, world);
  }
  
  
  public Prop(Session s) throws Exception {
    super(s);
    origin = (Tile) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(origin);
  }
  
  
  public Tile origin() {
    return origin;
  }
}

