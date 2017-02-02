

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
    
    renderEquipment(surface, g, person);
    renderCraftOptions(surface, g, person);
    
    return true;
  }
  
  
  void renderEquipment(Surface surface, Graphics2D g, Person person) {
    int down = 10, across = vw - 320;
    
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Equipment", g, vx + across, vy + down, 320, 30
    );
    down += 30;
    
    int maxSlots = person.gear.maxSlots();
    for (int slotID : PersonGear.SLOT_IDS) {
      if (slotID >= maxSlots) break;
      
      Item inSlot = person.gear.itemInSlot(slotID);
      int slotType = PersonGear.SLOT_TYPES[slotID];
      Image  icon = inSlot == null ? null   : inSlot.icon();
      String desc = inSlot == null ? "None" : inSlot.name();
      String slotName = PersonGear.SLOT_TYPE_NAMES[slotType];
      
      final boolean hovered = surface.tryHover(
        vx + 5 + across, vy + down, vw - 10, 40, "Slot_"+slotID
      );
      if (hovered) g.setColor(Color.YELLOW);
      else         g.setColor(Color.WHITE );
      
      g.drawImage(icon, vx + across + 5, vy + down, 40, 40, null);
      g.drawString(slotName+": "+desc, vx + across + 50, vy + down + 15);
      
      if (hovered && surface.mouseClicked()) {
        createItemMenu(person, slotID, vx + across + 5, vy + down + 20);
      }
      
      down += 40 + 10;
    }
  }
  
  
  void createItemMenu(final Person person, final int slotID, int x, int y) {
    final Base base = mainView.world().playerBase();
    final Batch <ItemType> types = new Batch();
    final BaseStocks stocks = mainView.world().playerBase().stocks;
    final int slotType = PersonGear.SLOT_TYPES[slotID];
    
    for (ItemType type : stocks.availableItemTypes(person, slotType)) {
      types.add(type);
    }
    if (types.empty()) return;
    
    mainView.showClickMenu(new ClickMenu <ItemType> (
      types, x, y, mainView
    ) {
      protected Image imageFor(ItemType option) {
        return option.icon();
      }
      
      protected String labelFor(ItemType option) {
        return option.name()+" ("+option.describeStats(person)+")";
      }
      
      protected void whenPicked(ItemType option, int optionID) {
        Item item = base.stocks.nextOfType(option);
        person.gear.equipItem(item, slotID, base);
      }
    });
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












