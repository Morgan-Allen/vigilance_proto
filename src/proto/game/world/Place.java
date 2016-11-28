

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;
import java.awt.Image;



public class Place extends Element {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public int slotID;
  final public District parent;
  
  private Base owner;
  private float buildProgress;
  private List <Trait> properties = new List();
  
  
  public Place(Blueprint print, int slotID, World world) {
    super(print, Element.TYPE_PLACE, world);
    this.slotID        = slotID;
    this.parent        = null;
    this.buildProgress = 1.0f;
  }
  
  
  public Place(Session s) throws Exception {
    super(s);
    slotID        = s.loadInt();
    owner         = (Base     ) s.loadObject();
    parent        = (District ) s.loadObject();
    buildProgress = s.loadFloat();
    s.loadObjects(properties);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveInt   (slotID       );
    s.saveObject(owner        );
    s.saveObject(parent       );
    s.saveFloat (buildProgress);
    s.saveObjects(properties);
  }
  
  
  
  /**  Construction progress-
    */
  public Blueprint blueprint() {
    return (Blueprint) kind;
  }
  
  
  public float buildProgress() {
    return buildProgress;
  }
  
  
  public Base owner() {
    return owner;
  }
  
  
  public void beginConstruction(Base owns, float initProgress) {
    setOwner(owns);
    owns.incFunding(0 - blueprint().buildCost);
    setBuildProgress(initProgress);
  }
  
  
  public void setOwner(Base owns) {
    this.owner = owns;
  }
  
  
  public void setBuildProgress(float progress) {
    this.buildProgress = Nums.clamp(progress, 0, 1);
  }
  
  
  public boolean hasProperty(Trait t) {
    return properties.includes(t);
  }
  
  public void setProperty(Trait t, boolean is) {
    properties.toggleMember(t, is);
  }
  
  
  
  /**  Updates and other life-cycle methods:
    */
  public void updatePlace() {
    for (Task t : possibleTasks()) {
      t.updateAssignment();
    }
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
  
  
  public Task[] possibleTasks() {
    return new Task[0];
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name();
  }
  
  
  public String name() {
    return blueprint().name();
  }
  
  
  public Image icon() {
    return blueprint().icon();
  }
}



