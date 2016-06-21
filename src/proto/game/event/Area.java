

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



//  TODO:  Consider having District extend this, and just refer to arbitrary
//         areas as parents.


public class Area implements Session.Saveable {
  
  
  final String name;
  
  final Region region;
  
  
  public Area(String name, Region region) {
    this.name = name;
    this.region = region;
  }
  
  
  public Area(Session s) throws Exception {
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