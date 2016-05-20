

package proto.game.scene;
import proto.common.*;



public class Clue implements Session.Saveable {
  
  
  final String name;
  
  
  public Clue(String name) {
    this.name = name;
  }
  
  
  public Clue(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(name);
  }
  
  
  public String toString() {
    return name;
  }
}
