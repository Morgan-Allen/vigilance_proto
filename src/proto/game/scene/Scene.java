

package proto.game.scene;
import proto.common.*;



public class Scene implements Session.Saveable {
  
  
  final String name;
  
  
  Scene(String name) {
    this.name = name;
  }
  
  
  public Scene(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(name);
  }
  
}