

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
  
  
  public EquipmentView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    Person person = this.mainView.rosterView.selectedPerson();
    if (person == null) return false;
    
    renderCraftOptions(surface, g, person);
    return true;
  }
  
  
  void renderCraftOptions(Surface surface, Graphics2D g, Person person) {
    final Base base = mainView.world().playerBase();
    
    int down = 10, across = 10;

    TaskCraft current = null;
    for (Assignment a : person.assignments()) if (a instanceof TaskCraft) {
      current = (TaskCraft) a;
    }
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Crafting: "+(current == null ? "None" : current.made()), g,
      vx + across, vy + down, 320, 30
    );
    
    down += 30;
    TaskCraft hovered = current;
    
    for (TaskCraft option : base.stocks.craftingTasksFor(person)) {
      TaskView view = option.createView(mainView);
      view.showIcon = false;
      view.relBounds.set(vx + across, vy + down, 320, 20);
      view.renderNow(surface, g);
      down += view.relBounds.ydim() + 10;
      if (surface.wasHovered(option)) hovered = option;
    }
    
    if (hovered != null) {
      down += 10;
      
      int craftTime = hovered.craftingTime() / World.HOURS_PER_DAY;
      float progress = hovered.craftingProgress();
      String desc = hovered.made().defaultInfo();
      desc += "\n  "+hovered.testInfo();
      desc += "\n  Cost: "+hovered.made().buildCost;
      
      if (craftTime == -1) {
        desc += "\n  Insufficient skill to craft.";
      }
      else {
        desc += "\n  Crafting time: "+craftTime+" days";
        desc += " ("+(int) (progress * 100)+"% complete).";
      }
      
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(desc, g, vx + across, vy + down, 320, 200);
    }
  }
  
  
}












