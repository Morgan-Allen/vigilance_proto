

package proto.content.rooms;
import proto.common.*;
import proto.content.items.Gadgets;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.List;



public class Laboratory extends Room {
  
  
  
  final static Equipped LAB_ITEMS[] = {
    Gadgets.MED_KIT ,
    Gadgets.TEAR_GAS
  };
  
  final List <Task> tasks = new List();
  
  
  
  public Laboratory(Base base, int slotIndex) {
    super(base, BLUEPRINT, slotIndex);
    
    for (Equipped item : LAB_ITEMS) {
      final Crafting crafting = new Crafting(item, this);
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
  final public static Blueprint BLUEPRINT = new Blueprint(
    "Laboratory", "blueprint_laboratory",
    "Allows agents to synthesise medicine, serums and chemicals.",
    "media assets/main UI/room_laboratory.png"
  ) {
    protected Room createRoom(Base base, int slotID) {
      return new Laboratory(base, slotID);
    }
  };
  
}








