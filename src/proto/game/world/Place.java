

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
  
  private Base owner;
  private Blueprint built;
  private float buildProgress;
  
  
  protected Place(World world, int slotID) {
    super(world);
    this.slotID = slotID;
  }
  
  
  protected Place(Base base, Blueprint print, int slotID) {
    super(base.world);
    this.slotID        = slotID;
    this.owner         = base;
    this.built         = print;
    this.buildProgress = 1.0f;
  }
  
  
  public Place(Session s) throws Exception {
    super(s);
    owner         = (Base) s.loadObject();
    slotID        = s.loadInt();
    built         = (Blueprint) s.loadObject();
    buildProgress = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(owner        );
    s.saveInt   (slotID       );
    s.saveObject(built        );
    s.saveFloat (buildProgress);
  }
  
  
  
  /**  Construction progress-
    */
  public Blueprint built() {
    return built;
  }
  
  
  public float buildProgress() {
    return buildProgress;
  }
  
  
  public Base owner() {
    return owner;
  }
  
  
  public void assignConstruction(Blueprint builds, Base owns, float progress) {
    built         = builds  ;
    owner         = owns    ;
    buildProgress = progress;
  }
  
  
  public void beginConstruction(Blueprint builds, Base owns) {
    assignConstruction(builds, owns, 0);
    owns.incFunding(0 - builds.buildCost);
  }
  
  
  public void setBuildProgress(float progress) {
    this.buildProgress = Nums.clamp(progress, 0, 1);
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
    if (built == null) return "Place";
    return built.name;
  }
  
  
  public Image icon() {
    if (built == null) return null;
    return built.icon;
  }
}



