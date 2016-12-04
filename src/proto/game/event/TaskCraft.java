

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;

import proto.view.common.*;
import proto.view.world.*;
import proto.util.*;

import java.awt.Image;



//  TODO:  Show progress in the task based on skill level, and redefine
//  completion-criteria.


public class TaskCraft extends Task {
  
  
  final Place room;
  final ItemType made;
  float progress = 0;
  
  
  public TaskCraft(ItemType made, Place room, Base base) {
    super(base, TIME_LONG, made.craftArgs);
    this.room = room;
    this.made = made;
  }
  
  
  public TaskCraft(Session s) throws Exception {
    super(s);
    room = (Place   ) s.loadObject();
    made = (ItemType) s.loadObject();
    progress = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(room);
    s.saveObject(made);
    s.saveFloat(progress);
  }
  
  
  
  /**  Task performance and completion-
    */
  protected void onFailure() {
    room.owner().incFunding(0 - made.buildCost / 2);
  }
  
  
  protected void onSuccess() {
    room.owner().incFunding(0 - made.buildCost);
    room.owner().stocks.incStock(made, 1);
  }
  
  
  public Place room() {
    return room;
  }
  
  
  public ItemType made() {
    return made;
  }
  
  
  public Place targetLocation() {
    return room;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return made.icon();
  }
  
  
  public String choiceInfo() {
    String info = "Crafting "+made;
    int total = room.owner().stocks.numStored(made);
    info += "  (In stock: "+total+")";
    return info;
  }
  
  
  public String activeInfo() {
    return "Crafting "+made+" in "+room;
  }
  
  
  public String helpInfo() {
    return made.defaultInfo()+"\n\n"+made.describeStats(null);
  }
  
  
  public String testInfo() {
    String info = super.testInfo();
    info += " Cost: "+made.buildCost;
    return info;
  }
  
  
  public TaskView createView(MainView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    return view;
  }
  

  protected void presentMessage(final World world) {
    final Series <String> logs = world.events.extractLogInfo(this);
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
      s.append(" They encountered difficulties.");
    }
    
    for (String info : logs) {
      s.append("\n");
      s.append(info);
    }
    
    world.view().queueMessage(new MessageView(
      world.view(),
      icon(), "Task complete: "+activeInfo(),
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
  
}












