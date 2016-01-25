

package proto;



public class Action implements Session.Saveable {
  
  Person acting;
  Tile path[];
  Ability used;
  Object target;
  int timeStart;
  float progress;
  
  
  Action() {
    
  }
  
  
  public Action(Session s) throws Exception {
    s.cacheInstance(this);
    acting    = (Person) s.loadObject();
    path      = (Tile[]) s.loadObjectArray(Tile.class);
    used      = (Ability) s.loadObject();
    target    = s.loadObject();
    timeStart = s.loadInt();
    progress  = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(acting);
    s.saveObjectArray(path);
    s.saveObject(used);
    s.saveObject((Session.Saveable) target);
    s.saveInt(timeStart);
    s.saveFloat(progress);
  }
  
  
}








