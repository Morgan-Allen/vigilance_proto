

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Clue implements Session.Saveable {
  
  
  final String name;
  private Object location;
  
  
  public Clue(String name) {
    this.name = name;
  }
  
  
  public Clue(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
    location = s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.cacheInstance(this);
    s.saveObject(name);
    s.saveObject(location);
  }
  
  
  void attachToLocation(Object location) {
    this.location = location;
  }
  
  
  public Object location() {
    return location;
  }
  
  
  
  public String toString() {
    return name;
  }
}
