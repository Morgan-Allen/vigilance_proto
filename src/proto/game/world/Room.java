

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public abstract class Room implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Base base;
  final public int slotIndex;
  final public Blueprint blueprint;
  
  float buildProgress;
  
  
  
  protected Room(Base base, Blueprint print, int slotIndex) {
    this.base      = base;
    this.blueprint = print;
    this.slotIndex = slotIndex;
  }
  
  
  public Room(Session s) throws Exception {
    s.cacheInstance(this);
    base          = (Base) s.loadObject();
    slotIndex     = s.loadInt();
    blueprint     = (Blueprint) s.loadObject();
    buildProgress = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base);
    s.saveInt(slotIndex);
    s.saveObject(blueprint);
    s.saveFloat(buildProgress);
  }
  
  
  
  /**  Toggling visitors-
    */
  public Series <Person> visitors() {
    final Batch <Person> visitors = new Batch();
    for (Task t : possibleTasks()) for (Person p : t.assigned()) {
      visitors.include(p);
    }
    return visitors;
  }
  
  
  public void updateRoom() {
    for (Task t : possibleTasks()) {
      t.updateAssignment();
    }
  }
  
  
  public abstract Task[] possibleTasks();
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return blueprint.name;
  }
  
  
  public String name() {
    return blueprint.name;
  }
  
  
  public Image icon() {
    return blueprint.sprite;
  }
}











