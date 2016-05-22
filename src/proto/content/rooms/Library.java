

package proto.content.rooms;
import proto.common.*;
import proto.game.person.*;
import proto.game.person.PersonStats.Stat;
import proto.game.scene.*;
import proto.game.world.*;
import static proto.game.person.PersonStats.*;



public class Library extends Room {
  
  final static Stat LIB_SKILLS[] = {
    ENGINEERING,
    INFORMATICS,
    PHARMACY,
    ANATOMY,
    LAW_N_FINANCE,
    LANGUAGES
  };
  
  
  final Task tasks[];
  
  
  public Library(Base base, int slotIndex) {
    super(base, BLUEPRINT, slotIndex);
    
    tasks = new Task[LIB_SKILLS.length];
    for (int i = 0; i < LIB_SKILLS.length; i++) {
      tasks[i] = new Training(LIB_SKILLS[i], this);
    }
  }
  
  
  public Library(Session s) throws Exception {
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
    "Library", "blueprint_library",
    "Permits training in most intellectual skills.",
    "media assets/main UI/room_library.png"
  ) {
    protected Room createRoom(Base base, int slotID) {
      return new Library(base, slotID);
    }
  };
}








