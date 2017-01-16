

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class Prop extends Element {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Tile origin;
  int facing;
  
  
  Prop(Kind kind, World world) {
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
}



