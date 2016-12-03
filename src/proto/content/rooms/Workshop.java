

package proto.content.rooms;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.content.items.Gadgets;
import proto.content.places.*;



public class Workshop extends Place {
  
  
  final static Equipped SHOP_ITEMS[] = {
    Gadgets.BATARANGS   ,
    Gadgets.CABLE_GUN  ,
    Gadgets.BODY_ARMOUR,
    Gadgets.KEVLAR_VEST,
  };
  
  final List <Task> tasks = new List();
  
  
  public Workshop(Base base, int slotIndex) {
    super(BLUEPRINT, slotIndex, base.world());
    setOwner(base);
    
    for (Equipped item : SHOP_ITEMS) {
      final TaskCraft crafting = new TaskCraft(item, this, base);
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
  final public static PlaceType BLUEPRINT = new PlaceType(
    "Workshop", "blueprint_workshop",
    "media assets/main UI/room_workshop.png",
    "Allows agents to repair or manufacture gadgets and armour.",
    UrbanScenes.MANSION_SCENE
  ) {
    public Place createRoom(Base base, int slotID) {
      return new Workshop(base, slotID);
    }
  };
}








