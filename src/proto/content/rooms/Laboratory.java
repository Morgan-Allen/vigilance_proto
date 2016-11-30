

package proto.content.rooms;
import proto.common.*;
import proto.content.items.Gadgets;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.List;



public class Laboratory extends Place {
  
  
  
  final static Equipped LAB_ITEMS[] = {
    Gadgets.MED_KIT ,
    Gadgets.TEAR_GAS
  };
  
  final List <Task> tasks = new List();
  
  
  
  public Laboratory(Base base, int slotIndex) {
    super(BLUEPRINT, slotIndex, base.world());
    setOwner(base);
    
    for (Equipped item : LAB_ITEMS) {
      final TaskCraft crafting = new TaskCraft(item, this, base);
      tasks.add(crafting);
    }
  }
  
  
  public Laboratory(Session s) throws Exception {
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
    "Laboratory", "blueprint_laboratory",
    "media assets/main UI/room_laboratory.png",
    "Allows agents to synthesise medicine, serums and chemicals."
  ) {
    protected Place createRoom(Base base, int slotID) {
      return new Laboratory(base, slotID);
    }
  };
  
}








