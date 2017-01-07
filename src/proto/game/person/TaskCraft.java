

package proto.game.person;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Image;



//  TODO:  Show progress in the task based on skill level, and redefine
//  completion-criteria.


public class TaskCraft extends Task {
  
  
  final ItemType made;
  float progress = 0;
  
  
  public TaskCraft(ItemType made, Base base) {
    super(base, TIME_LONG, made.craftArgs);
    this.made = made;
  }
  
  
  public TaskCraft(Session s) throws Exception {
    super(s);
    made = (ItemType) s.loadObject();
    progress = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(made);
    s.saveFloat(progress);
  }
  
  
  
  /**  Task performance and completion-
    */
  protected void onFailure() {
    base.incFunding(0 - made.buildCost / 2);
  }
  
  
  protected void onSuccess() {
    base.incFunding(0 - made.buildCost);
    base.stocks.incStock(made, 1);
  }
  
  
  public ItemType made() {
    return made;
  }
  
  
  public Place targetLocation() {
    return base;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_TRAINING;
  }
  
  

  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return made.icon();
  }
  
  
  public String choiceInfo() {
    String info = "Craft "+made;
    int total = base.stocks.numStored(made);
    info += "  (In stock: "+total+")";
    return info;
  }
  
  
  public String activeInfo() {
    return "Crafting "+made;
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
  
  
  protected void presentMessage() {
    final World world = base.world();
    final Series <String> logs = world.events.extractLogInfo(this);
    StringBuffer s = new StringBuffer();

    final Series <Person> active = active();
    for (Person p : active) {
      s.append(p.name());
      if (p != active.last()) s.append(" and ");
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












