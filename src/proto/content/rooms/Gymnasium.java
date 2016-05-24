

package proto.content.rooms;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import static proto.game.person.PersonStats.*;



public class Gymnasium extends Room {
  
  final static Skill GYM_SKILLS[] = {
    MARKSMAN, GYMNASTICS, CLOSE_COMBAT, STAMINA
  };
  
  
  final Task tasks[];
  
  
  public Gymnasium(Base base, int slotIndex) {
    super(base, BLUEPRINT, slotIndex);
    
    tasks = new Task[GYM_SKILLS.length];
    for (int i = 0; i < GYM_SKILLS.length; i++) {
      tasks[i] = new Training(GYM_SKILLS[i], this);
    }
  }
  
  
  public Gymnasium(Session s) throws Exception {
    super(s);
    tasks = (Task[]) s.loadObjectArray(Task.class);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjectArray(tasks);
  }
  
  
  public Task[] possibleTasks() {
    return tasks;
  }
  
  
  
  /**  Type definition for ease of enumeration and reference-
    */
  final public static Blueprint BLUEPRINT = new Blueprint(
    "Gymnasium", "blueprint_gymnasium",
    "Permits training in physical skills involving strength or reflex.",
    "media assets/main UI/room_gymnasium.png"
  ) {
    protected Room createRoom(Base base, int slotID) {
      return new Gymnasium(base, slotID);
    }
  };
}








