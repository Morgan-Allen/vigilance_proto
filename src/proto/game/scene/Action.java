

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public class Action implements Session.Saveable {
  
  
  final public Ability used;
  final public Person acting;
  final public Object target;
  
  Tile path[];
  Volley volley;
  int timeSpent;
  float progress;
  
  
  public Action(Ability used, Person acting, Object target) {
    this.used     = used  ;
    this.acting   = acting;
    this.target   = target;
    this.progress = -1    ;
  }
  
  
  public Action(Session s) throws Exception {
    s.cacheInstance(this);
    used      = (Ability) s.loadObject();
    acting    = (Person ) s.loadObject();
    path      = (Tile[] ) s.loadObjectArray(Tile.class);
    volley    = (Volley ) s.loadObject();
    target    = s.loadObject();
    timeSpent = s.loadInt   ();
    progress  = s.loadFloat ();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(used  );
    s.saveObject(acting);
    s.saveObjectArray(path);
    s.saveObject(volley   );
    s.saveObject(target   );
    s.saveInt   (timeSpent);
    s.saveFloat (progress );
  }
  
  
  
  /**  General query methods-
    */
  public void attachPath(Tile path[]) {
    this.path = path;
  }
  
  
  public void attachVolley(Volley volley) {
    this.volley = volley;
  }
  
  
  public void setProgress(float progress) {
    this.progress = progress;
  }
  
  
  public int timeSpent() {
    return timeSpent;
  }
  
  
  public float progress() {
    return progress;
  }
  
  
  public boolean started() {
    return progress >= 0;
  }
  
  
  public boolean complete() {
    return progress >= 1;
  }
  
  
  public Scene scene() {
    return acting.currentScene();
  }
  
  
  public Tile[] path() {
    return path;
  }
  
  
  public Volley volley() {
    return volley;
  }
  
  
  public float moveRate() {
    return 4;
  }
  
  
  public void incTimeSpent(int timeInc) {
    timeSpent += timeInc;
  }
  
  
  public float timeSteps() {
    return timeSpent * moveRate() / RunGame.FRAME_RATE;
  }
  
  
  public boolean inMotion() {
    if (Visit.empty(path)) return false;
    return timeSteps() <= path.length;
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return used.name+": "+acting+" -> "+target;
  }
}








