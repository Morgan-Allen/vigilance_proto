

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;
import java.awt.Image;



//  TODO:  You may not need this any more.  Review.

public class Packing extends Task {
  
  
  Base base;
  Equipped packed;
  
  
  public Packing(Base base, Equipped packed) {
    super(
      "Packing", "Packing",
      TIME_SHORT, base.world()
    );
    this.base = base;
    this.packed = packed;
  }
  
  
  public Packing(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  
  /**  Task performance and completion-
    */
  protected void onFailure() {
    onSuccess();
  }
  
  
  protected void onSuccess() {
    for (Person p : assigned) {
      p.equipItem(packed);
      base.removeFromStore(packed);
    }
  }
  
  
  public Object targetLocation() {
    return base;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return null;
    //return room.icon();
  }
  
  
  public String description() {
    return "";
  }
  
  
  public String longInfo() {
    String info = super.longInfo();
    //int total = room.base.numStored(made);
    //info += "  (In stock: "+total+")";
    return info;
  }
  
  
  public TaskView createView(WorldView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    return view;
  }
  

  protected void presentMessage(final World world) {
  }
}












