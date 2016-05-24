

package proto.content.rooms;
import proto.common.*;
import proto.game.person.*;
import proto.game.person.PersonStats.Stat;
import proto.game.scene.*;
import proto.game.world.*;
import static proto.game.person.PersonStats.*;



public class Laboratory extends Room {
  
  
  
  
  public Laboratory(Base base, int slotIndex) {
    super(base, BLUEPRINT, slotIndex);
    
  }
  
  
  public Laboratory(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public Task[] possibleTasks() {
    return new Task[0];
  }
  
  
  
  /**  Type definition for ease of enumeration and reference-
    */
  final public static Blueprint BLUEPRINT = new Blueprint(
    "Laboratory", "blueprint_laboratory",
    "Allows agents to equip and synthesise medicine, serums and chemicals.",
    "media assets/main UI/room_laboratory.png"
  ) {
    protected Room createRoom(Base base, int slotID) {
      return new Laboratory(base, slotID);
    }
  };
  
}








