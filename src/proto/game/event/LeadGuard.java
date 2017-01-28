

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public class LeadGuard extends Lead {
  
  
  Place guarded;
  Event event;
  
  
  public LeadGuard(Base base, Place guarded, Event event) {
    super(base, Task.TIME_LONG, guarded, new Object[0]);
    this.guarded = guarded;
    this.event   = event  ;
  }
  
  
  public LeadGuard(Session s) throws Exception {
    super(s);
    guarded = (Place) s.loadObject();
    event   = (Event) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(guarded);
    s.saveObject(event  );
  }
  
  
  public Place targetLocation() {
    return guarded;
  }

  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    
    if (event.complete()) {
      setCompleted(false);
    }
    else if (event.hasBegun()) {
      setCompleted(true);
    }
    else resetTask();
    
    return true;
  }
  
  
  protected void onSuccess() {
    //
    //  Firstly, generate the scene and populate with minions-
    Place place = event.targetLocation();
    SceneType sceneType = place.kind().sceneType();
    Scene mission = sceneType.generateScene(place.world());
    event.populateScene(mission);
    //
    //  Then, introduce the agents themselves-
    final Series <Person> active = active();
    int across = (mission.size() - (active.size())) / 2;
    for (Person p : active) {
      p.addAssignment(mission);
      mission.enterScene(p, across++, 0);
    }
    //
    //  Then, set the scene as active within the world-
    mission.assignMissionParameters(place, this, event);
    base.world().enterScene(mission);
  }


  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public Image icon() {
    return guarded.icon();
  }
  
  
  public String choiceInfo(Person p) {
    return "Guard "+guarded;
  }
  
  
  public String activeInfo() {
    return "Guarding "+guarded;//+" during event: "+event;
  }
  
  
  public String helpInfo() {
    return "Guard "+guarded+" to foil event: "+event;
  }
  
  
}




