

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public class Room implements Session.Saveable, Assignment {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Base base;
  final public int slotIndex;
  
  Blueprint blueprint;
  float buildProgress;
  List <Person> assigned = new List();
  
  
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
    s.loadObjects(assigned);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base);
    s.saveInt(slotIndex);
    s.saveObject(blueprint);
    s.saveFloat(buildProgress);
    s.saveObjects(assigned);
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
  public void setAssigned(Person p, boolean is) {
    assigned.toggleMember(p, is);
    p.setAssignment(is ? this : null);
  }
  
  
  public boolean allowsAssignment(Person p) {
    if (buildProgress < 1) return false;
    if (assigned.includes(p)) return true;
    return assigned.size() < blueprint.visitLimit;
  }
  
  
  public Series <Person> assigned() {
    return assigned;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return blueprint.name+" (Room "+slotIndex+")";
  }
  
  
  public String name() {
    return blueprint.name+" (Room "+slotIndex+")";
  }
  
  
  public Image icon() {
    return blueprint.sprite;
  }
}











