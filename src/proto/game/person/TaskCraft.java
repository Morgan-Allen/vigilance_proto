

package proto.game.person;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Image;



public class TaskCraft extends Task {
  
  
  final ItemType made;
  int numOrders = 1;
  float progress = 0;
  
  
  public TaskCraft(ItemType made, Base base) {
    super(base, TIME_INDEF, made.craftArgs);
    this.made = made;
  }
  
  
  public TaskCraft(Session s) throws Exception {
    super(s);
    made      = (ItemType) s.loadObject();
    numOrders = s.loadInt  ();
    progress  = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(made);
    s.saveInt  (numOrders);
    s.saveFloat(progress );
  }
  
  
  
  /**  Task performance and completion-
    */
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    advanceCrafting();
    return true;
  }
  
  
  protected void advanceCrafting() {
    int craftTime = craftingTime();
    if (craftTime == -1) return;
    
    if (progress == 0) {
      base.finance.incSecretFunds(0 - made.buildCost);
    }
    
    //  TODO:  Grant experience in the relevant skills!
    progress += base.world().timing.hoursInTick() / craftTime;
    
    if (progress >= 1) {
      progress = 0;
      numOrders--;
      base.stocks.incStock(made, 1);
      presentMessage();
    }
    if (numOrders <= 0) {
      setCompleted(true);
    }
  }
  
  
  public int craftingTime() {
    if (active().empty()) return made.craftTime;
    float craftChance = testChance();
    if (craftChance <= 0) return -1;
    int baseTime = made.craftTime;
    baseTime *= 4 - (craftChance * 3);
    return baseTime;
  }
  
  
  public float craftingProgress() {
    return progress;
  }
  
  
  public Place targetLocation(Person p) {
    return base;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_TRAINING;
  }
  
  
  public ItemType made() {
    return made;
  }
  
  
  public void incOrders(int inc) {
    numOrders += inc;
    if (numOrders < 0) numOrders = 0;
  }
  
  
  public int numOrders() {
    return numOrders;
  }
  
  
  public void resetTask() {
    super.resetTask();
    numOrders = 1;
    progress  = 0;
  }
  
  

  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return made.icon();
  }
  
  
  public String choiceInfo(Person p) {
    String info = ""+made;
    int total = base.stocks.numStored(made);
    info += "  ("+total+")";
    return info;
  }
  
  
  public String activeInfo() {
    return "Crafting "+made;
  }
  
  
  public String helpInfo() {
    return made.defaultInfo()+"\n\n"+made.describeStats(null);
  }
  
  
  public TaskView createView(MainView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    return view;
  }
  
  
  protected void presentMessage() {
    final World world = base.world();
    final Task craftTask = this;
    final Series <String> logs = world.events.extractLogInfo(this);
    StringBuffer s = new StringBuffer();

    final Series <Person> active = active();
    for (Person p : active) {
      s.append(p.name());
      if (p != active.last()) s.append(" and ");
    }
    s.append(" crafted "+made+".");
    
    for (String info : logs) {
      s.append("\n");
      s.append(info);
    }

    final MainView view = world.view();
    view.queueMessage(new MessageView(
      view,
      icon(), "Task complete: "+activeInfo(),
      s.toString(),
      "Continue Crafting",
      "New Assignment"
    ) {
      protected void whenClicked(String option, int optionID) {
        if (optionID == 0) {
          view.dismissMessage(this);
        }
        if (optionID == 1) {
          for (Person p : active) p.removeAssignment(craftTask);
          view.switchToTab(view.equipView);
          view.dismissMessage(this);
        }
      }
    });
  }
  
}


