

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.view.*;

import java.awt.Image;



public class Crafting extends Task {
  
  
  final Room room;
  final Equipped made;
  
  
  public Crafting(Equipped made, Room room) {
    super(
      "Crafting "+made.name,
      "Craft "+made.name,
      TIME_LONG, room.base.world(),
      made.craftArgs
    );
    this.room = room;
    this.made = made;
  }
  
  
  public Crafting(Session s) throws Exception {
    super(s);
    room = (Room    ) s.loadObject();
    made = (Equipped) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(room);
    s.saveObject(made);
  }
  
  
  
  /**  Task performance and completion-
    */
  protected void onFailure() {
    room.base.incFunding(0 - made.buildCost / 2);
  }
  
  
  protected void onSuccess() {
    room.base.incFunding(0 - made.buildCost);
    room.base.addEquipment(made);
  }
  
  
  public Room room() {
    return room;
  }
  
  
  public Equipped made() {
    return made;
  }
  
  
  public Object targetLocation() {
    return room;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return room.icon();
  }
  
  
  public String description() {
    return name()+" in "+room.name();
  }
  
  
  public String longInfo() {
    String info = super.longInfo();
    int total = room.base.numStored(made);
    info += "  (In stock: "+total+")";
    return info;
  }
  

  public TaskView createView(WorldView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    view.isCraft  = true ;
    return view;
  }
  

  protected void presentMessage(final World world) {
    StringBuffer s = new StringBuffer();
    
    for (Person p : assigned) {
      s.append(p.name());
      if (p != assigned.last()) s.append(" and ");
    }
    s.append(" attempted to make "+made+".");
    if (success()) {
      s.append(" They were successful.");
    }
    else {
      s.append(" The encountered difficulties.");
    }
    
    world.view().queueMessage(new MessageView(
      icon(), "Task complete: "+name,
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
}












