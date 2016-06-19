

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Scene implements Session.Saveable {
  
  
  final String name;
  
  final Region region;
  
  
  public Scene(String name, Region region) {
    this.name = name;
    this.region = region;
  }
  
  
  public Scene(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
    region = (Region) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(name);
    s.saveObject(region);
  }
  
  
  public String toString() {
    return name;
  }
}