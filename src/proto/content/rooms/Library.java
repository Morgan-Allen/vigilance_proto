

package proto.content.rooms;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import static proto.game.person.PersonStats.*;
import proto.content.places.*;



public class Library extends Place {
  
  final static Skill LIB_SKILLS[] = {
    ENGINEERING,
    INFORMATICS,
    PHARMACY,
    ANATOMY,
    LAW_N_FINANCE,
    LANGUAGES
  };
  
  
  final Task tasks[];
  
  
  public Library(Base base, int slotIndex) {
    super(BLUEPRINT, slotIndex, base.world());
    setOwner(base);
    
    tasks = new Task[LIB_SKILLS.length];
    for (int i = 0; i < LIB_SKILLS.length; i++) {
      tasks[i] = new TaskTrain(LIB_SKILLS[i], QUESTION, this, base);
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
  final public static PlaceType BLUEPRINT = new PlaceType(
    "Library", "blueprint_library",
    "media assets/main UI/room_library.png",
    "Permits training in most intellectual skills.",
    UrbanScenes.MANSION_SCENE
  ) {
    protected Place createRoom(Base base, int slotID) {
      return new Library(base, slotID);
    }
  };
}








