

package proto.game.world;
import proto.common.Session;
import proto.common.Session.Saveable;
import proto.game.person.Person;
import proto.util.*;



public class Room implements Session.Saveable, Assignment {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Base base;
  final public int slotIndex;
  
  Blueprint blueprint;
  float buildProgress;
  List <Person> visitors = new List();
  
  
  Room(Base base, int slotIndex) {
    this.base = base;
    this.slotIndex = slotIndex;
  }
  
  
  public Room(Session s) throws Exception {
    s.cacheInstance(this);
    base          = (Base) s.loadObject();
    slotIndex     = s.loadInt();
    blueprint     = (Blueprint) s.loadObject();
    buildProgress = s.loadFloat();
    s.loadObjects(visitors);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base);
    s.saveInt(slotIndex);
    s.saveObject(blueprint);
    s.saveFloat(buildProgress);
    s.saveObjects(visitors);
  }
  
  
  
  /**  Blueprints and (physical) construction-
    */
  public Blueprint blueprint() {
    return blueprint;
  }
  
  
  public float buildProgress() {
    return buildProgress;
  }
  
  
  
  /**  Toggling visitors-
    */
  public void addVisitor(Person p) {
    visitors.include(p);
    p.setAssignment(this);
  }
  
  
  public void removeVisitor(Person p) {
    visitors.remove(p);
    p.setAssignment(null);
  }
  
  
  public boolean allowsAssignment(Person p) {
    if (buildProgress < 1) return false;
    if (visitors.includes(p)) return true;
    return visitors.size() < blueprint.visitLimit;
  }
  
  
  public Series <Person> visitors() {
    return visitors;
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











