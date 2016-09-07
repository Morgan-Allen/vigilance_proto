

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Clue implements Session.Saveable {
  
  
  final static int
    IS_NEARBY    = 0,
    HAS_PROPERTY = 1,
    EXACT_MATCH  = 2;
  
  
  final String name;
  int type;
  
  Object leadsTo;
  private Object location;
  
  
  public Clue(String name) {
    this.name = name;
  }
  
  
  public Clue(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
    
    leadsTo  = s.loadObject();
    location = s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveString(name);
    
    s.saveObject(leadsTo );
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
