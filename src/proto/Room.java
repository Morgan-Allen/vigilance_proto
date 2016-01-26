

package proto;
import util.*;



public class Room implements Session.Saveable, Assignment {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Blueprint blueprint;
  Base base;
  
  int slotIndex;
  float buildProgress;
  List <Person> visitors = new List();
  
  
  Room(Base base, Blueprint blueprint) {
    this.blueprint = blueprint;
    this.base = base;
  }
  
  
  public Room(Session s) throws Exception {
    s.cacheInstance(this);
    blueprint     = (Blueprint) s.loadObject();
    base          = (Base) s.loadObject();
    slotIndex     = s.loadInt();
    buildProgress = s.loadFloat();
    s.loadObjects(visitors);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(blueprint);
    s.saveObject(base);
    s.saveInt(slotIndex);
    s.saveFloat(buildProgress);
    s.saveObjects(visitors);
  }
  
  
  
  /**  Toggling visitors-
    */
  void addVisitor(Person p) {
    visitors.include(p);
    p.setAssignment(this);
  }
  
  
  void removeVisitor(Person p) {
    visitors.remove(p);
    p.setAssignment(null);
  }
  
  
  public boolean allowsAssignment(Person p) {
    if (buildProgress < 1) return false;
    if (visitors.includes(p)) return true;
    return visitors.size() < blueprint.visitLimit;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return blueprint.name+" (Room "+slotIndex+")";
  }
  
  
  public String name() {
    return blueprint.name+" (Room "+slotIndex+")";
  }
}











