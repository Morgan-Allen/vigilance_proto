

package proto.content.rooms;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import static proto.game.person.PersonStats.*;



public class Gymnasium extends Place {
  
  final static Skill GYM_SKILLS[] = {
    MARKSMAN, GYMNASTICS, CLOSE_COMBAT, STAMINA
  };
  
  
  final Task tasks[];
  
  
  public Gymnasium(Base base, int slotIndex) {
    super(BLUEPRINT, slotIndex, base.world());
    setOwner(base);
    
    tasks = new Task[GYM_SKILLS.length];
    for (int i = 0; i < GYM_SKILLS.length; i++) {
      tasks[i] = new TaskTrain(GYM_SKILLS[i], INTIMIDATE, this, base);
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
  final public static PlaceType BLUEPRINT = new PlaceType(
    "Gymnasium", "blueprint_gymnasium",
    "media assets/main UI/room_gymnasium.png",
    "Permits training in physical skills involving strength or reflex."
  ) {
    protected Place createRoom(Base base, int slotID) {
      return new Gymnasium(base, slotID);
    }
  };
}








