

package proto.content.rooms;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;
import proto.content.items.Gadgets;



public class Workshop extends Room {
  
  
  final static Equipped SHOP_ITEMS[] = {
    Gadgets.BATARANGS   ,
    Gadgets.CABLE_GUN  ,
    Gadgets.BODY_ARMOUR,
    Gadgets.KEVLAR_VEST,
  };
  
  final List <Task> tasks = new List();
  
  
  public Workshop(Base base, int slotIndex) {
    super(base, BLUEPRINT, slotIndex);
    
    for (Equipped item : SHOP_ITEMS) {
      final Crafting crafting = new Crafting(item, this);
      tasks.add(crafting);
    }
  }
  
  
  public Workshop(Session s) throws Exception {
    super(s);
    s.loadObjects(tasks);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjects(tasks);
  }
  
  
  public Task[] possibleTasks() {
    return tasks.toArray(Task.class);
  }
  
  
  
  /**  Type definition for ease of enumeration and reference-
    */
  final public static Blueprint BLUEPRINT = new Blueprint(
    "Workshop", "blueprint_workshop",
    "Allows agents to equip, repair or manufacture gadgets and armour.",
    "media assets/main UI/room_workshop.png"
  ) {
    protected Room createRoom(Base base, int slotID) {
      return new Workshop(base, slotID);
    }
  };
}








