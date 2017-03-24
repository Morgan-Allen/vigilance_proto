

package proto.view.base;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class EquipmentView extends UINode {
  
  
  TaskCraft selectedItem;
  
  public EquipmentView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    Person person = this.mainView.rosterView.selectedPerson();
    if (person == null) return false;
    
    renderCraftOptions(surface, g, person);
    renderCraftingQueue(surface, g);
    return true;
  }
  
  
  void renderCraftOptions(Surface surface, Graphics2D g, Person person) {
    final Base base = mainView.player();
    int down = 10, across = 10;
    
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Current Inventory",
      g, vx + across, vy + down, 320, 30
    );
    
    
    down += 30;
    for (final TaskCraft option : base.stocks.craftingTasks()) {
      String label = option.made().name();
      label += " (x"+base.stocks.numStored(option.made())+")";
      
      StringButton pickOption = new StringButton(
        label, new Box2D(across, down, 300, 20), this
      ) {
        protected void whenClicked() {
          selectedItem = option;
        }
      };
      pickOption.baseTone = Color.LIGHT_GRAY;
      pickOption.toggled = option == selectedItem;
      pickOption.refers = option;
      pickOption.renderNow(surface, g);
      
      ViewUtils.renderAssigned(
        option.assigned(), vx + across + 300, vy + down + 20, this, surface, g
      );
      down += 25;
    }
    
    if (selectedItem == null) {
      String desc =
        "Select an item type to view further information and manufacturing "+
        "options.";
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(desc, g, vx + across, vy + down, 320, 200);
    }
    else {
      down += 10;
      
      float craftTime = selectedItem.craftingTime(person);
      craftTime *= 1f / World.HOURS_PER_DAY;
      String desc = selectedItem.made().defaultInfo();
      desc += "\n  "+selectedItem.testInfo(person);
      desc += "\n  Cost: "+selectedItem.made().buildCost;
      
      if (craftTime == -1) {
        desc += "  Insufficient skill.";
      }
      else {
        desc += "  Time: "+I.shorten(craftTime, 1)+" days";
      }
      
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(desc, g, vx + across, vy + down, 320, 120);
      
      down += 120;
      final Person crafter = person;
      
      boolean isCrafted = ! selectedItem.assigned().empty();
      boolean isHelping = crafter.assignments().includes(selectedItem);
      String increaseDesc = "Begin Manufacture";
      if (isHelping) increaseDesc = "Increase Order";
      else if (isCrafted) increaseDesc = "Assist Manufacture";
      
      StringButton increaseButton = new StringButton(
        increaseDesc, new Box2D(across, down, 200, 20), this
      ) {
        protected void whenClicked() {
          if (crafter.assignments().includes(selectedItem)) {
            selectedItem.incOrders(1);
          }
          else crafter.addAssignment(selectedItem);
        }
      };
      StringButton cancelButton = new StringButton(
        "Cancel", new Box2D(across + 200, down, 100, 20), this
      ) {
        protected void whenClicked() {
          selectedItem.incOrders(-1);
          if (selectedItem.numOrders() == 0) {
            selectedItem.setCompleted(false);
            selectedItem.resetTask();
          }
        }
      };
      increaseButton.refers = "craft_+";
      cancelButton  .refers = "craft_-";
      increaseButton.renderNow(surface, g);
      cancelButton  .renderNow(surface, g);
      increaseButton.valid = cancelButton.valid = craftTime != -1;
    }
  }
  
  
  void renderCraftingQueue(Surface surface, Graphics2D g) {
    final Base base = mainView.player();
    
    //  TODO:  I'm not positive the time estimates here are accurate, or if it
    //  wouldn't be possible to simplify the system.  (e.g, by having a single
    //  'assign to workshop' task for the agents, and just letting them work
    //  through the queue of orders.)
    
    class arrival {
      TaskCraft task;
      ItemType made;
      float time, prog;
    }
    
    List <arrival> arrivals = new List <arrival> () {
      protected float queuePriority(arrival r) {
        return r.time;
      }
    };
    
    for (TaskCraft task : base.stocks.craftingTasks()) {
      if (task.assigned().empty()) continue;
      ItemType made = task.made();
      float craftTime = task.craftingTime(), prog = task.craftingProgress();
      
      for (int order = 1; order <= task.numOrders(); order++) {
        arrival a = new arrival();
        a.task = task;
        a.made = made;
        a.time = craftTime * (order - prog);
        a.prog = order == 1 ? prog : -1;
        arrivals.add(a);
      }
    }
    arrivals.queueSort();
    
    g.setColor(Color.LIGHT_GRAY);
    int down = 10, across = 330;
    
    ViewUtils.drawWrappedString(
      "Current Orders:", g,
      vx + across, vy + down, 300, 30
    );
    down += 35;
    
    if (arrivals.empty()) {
      
    }
    else for (arrival a : arrivals) {
      float time = a.time / World.HOURS_PER_DAY;
      String desc = "  "+a.made+" ("+I.shorten(time, 1)+" days)";
      
      if (a.prog >= 0) {
        int prog = (int) (100 * a.prog);
        desc += " ("+prog+"% complete)";
      }
      
      ViewUtils.drawWrappedString(
        desc, g,
        vx + across, vy + down, 300, 20
      );
      down += 20;
    }
  }
  
}






