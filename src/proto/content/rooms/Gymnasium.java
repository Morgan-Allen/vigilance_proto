

package proto.content.rooms;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.content.agents.Techniques;
import proto.content.places.*;
import static proto.game.person.PersonStats.*;



public class Gymnasium extends Place {
  
  
  final Task tasks[];
  
  
  public Gymnasium(Base base, int slotIndex) {
    super(BLUEPRINT, slotIndex, base.world());
    setOwner(base);
    //
    //  TODO:  You will need to refresh this list dynamically for each agent in
    //  your roster.
    final Ability techs[] = Techniques.PHYS_TECHNIQUES;
    tasks = new Task[techs.length];
    for (int i = 0; i < techs.length; i++) {
      tasks[i] = new TaskTrain(techs[i], PERSUADE, this, base);
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
    "Permits training in physical skills involving strength or reflex.",
    UrbanScenes.MANSION_SCENE
  ) {
    public Place createRoom(Base base, int slotID) {
      return new Gymnasium(base, slotID);
    }
  };
}








