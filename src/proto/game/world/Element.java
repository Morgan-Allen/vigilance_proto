

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.util.*;



public class Element implements Session.Saveable {
  
  
  World world;
  Place location;
  List <Clue> attached = new List();
  
  
  
  protected Element(World world) {
    this.world = world;
  }
  
  
  public Element(Session s) throws Exception {
    s.cacheInstance(this);
    world    = (World) s.loadObject();
    location = (Place) s.loadObject();
    s.loadObjects(attached);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(world);
    s.saveObject(location);
    s.saveObjects(attached);
  }
  
  
  public World world() {
    return world;
  }
  
  
  
  
  
}






