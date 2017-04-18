

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
    super(base, TIME_INDEF);
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
    if (progress == 0) attempt = configAttempt(active());
    int craftTime = craftingTime(attempt);
    if (craftTime == -1) return;
    
    if (progress == 0) base.finance.incSecretFunds(0 - made.buildCost);
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
  
  
  public float craftingProgress() {
    return progress;
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
  
  
  
  /**  General task-method overrides-
    */
  protected Attempt configAttempt(Series <Person> attempting) {
    Attempt attempt = new Attempt(this);
    attempt.setupFromArgsList(10, made.craftArgs);
    attempt.grantFacilityModifiers(base);
    attempt.setAssigned(attempting);
    return attempt;
  }
  
  
  public int craftingTime(Person... extra) {
    final Batch <Person> all = new Batch();
    Visit.appendTo(all, active());
    for (Person p : extra) all.include(p);
    Attempt sample = configAttempt(all);
    return craftingTime(sample);
  }
  
  
  int craftingTime(Attempt attempt) {
    if (attempt == null) return -1;
    if (active().empty()) return made.craftTime;
    float craftChance = attempt.testChance();
    if (craftChance <= 0) return -1;
    int baseTime = made.craftTime;
    baseTime *= 4 - (craftChance * 3);
    baseTime /= 1 + attempt.speedBonus;
    return baseTime;
  }
  
  
  public Element targetElement(Person p) {
    return base;
  }


  public int assignmentPriority() {
    return PRIORITY_TRAINING;
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
  
  
  protected void presentMessage() {
    final World world = base.world();
    final Task craftTask = this;
    StringBuffer s = new StringBuffer();
    
    final Series <Person> active = active();
    for (Person p : active) {
      s.append(p.name());
      if (p != active.last()) s.append(" and ");
    }
    s.append(" crafted "+made+".");

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


