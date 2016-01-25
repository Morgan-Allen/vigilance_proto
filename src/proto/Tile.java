

package proto;



public class Tile implements Session.Saveable {

  Scene scene;
  int x, y;
  Prop prop;
  Person standing;
  
  Object flag;
  
  
  Tile() {
    
  }
  
  
  public Tile(Session s) throws Exception {
    s.cacheInstance(this);
    scene = (Scene) s.loadObject();
    x = s.loadInt();
    y = s.loadInt();
    prop = (Prop) s.loadObject();
    standing = (Person) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(scene);
    s.saveInt(x);
    s.saveInt(y);
    s.saveObject(prop);
    s.saveObject(standing);
  }
  
  
  
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Tile at "+x+"|"+y;
  }
}
