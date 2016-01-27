

package proto.game.scene;
import proto.common.Session;



public class Action implements Session.Saveable {
  
  
  final public Ability used;
  
  Person acting;
  Object target;
  Tile path[];
  Volley volley;
  
  int timeStart;
  float progress;
  
  
  Action(Ability used) {
    this.used = used;
  }
  
  
  public Action(Session s) throws Exception {
    s.cacheInstance(this);
    used      = (Ability) s.loadObject();
    acting    = (Person ) s.loadObject();
    path      = (Tile[] ) s.loadObjectArray(Tile.class);
    volley    = (Volley ) s.loadObject();
    target    = s.loadObject();
    timeStart = s.loadInt   ();
    progress  = s.loadFloat ();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(used);
    s.saveObject(acting);
    s.saveObjectArray(path);
    s.saveObject(volley);
    s.saveObject((Session.Saveable) target);
    s.saveInt(timeStart);
    s.saveFloat(progress);
  }
  
  
  
  /**  Life-cycle and update methods-
    */
  public float progress() {
    return progress;
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  
}








