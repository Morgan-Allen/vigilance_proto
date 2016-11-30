

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public class TaskGuard extends Task {
  
  
  Event event;
  
  
  
  public TaskGuard(Base base, Event event) {
    super(base, Task.TIME_SHORT, new Object[0]);
    this.event = event;
  }
  
  
  public TaskGuard(Session s) throws Exception {
    super(s);
    event = (Event) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(event);
  }
  
  
  public Place targetLocation() {
    return event.place();
  }
  
  
  protected void onSuccess() {
    base.leads.closeLead(event);
    
    //  TODO:  This should only be triggered when the event itself is either
    //  going down or about to do so!
    
    Place place = event.place();
    SceneType sceneType = place.kind().sceneType();
    Scene mission = sceneType.generateScene(place, base.world(), 32);
    for (Person p : assigned()) mission.addToTeam(p);
    
    //  TODO:  Populate with suitable enemy forces based on the type of crime
    //  underway!
    
    final Batch <Person> enemies = new Batch();
    mission.assignMissionParameters(this, place, 0.5f, 100, enemies);
    mission.setupScene();
    base.world().enterScene(mission);
    
  }
  
  
  protected void onFailure() {
    base.leads.closeLead(event);
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String activeInfo() {
    return null;
  }
  
  
  public String helpInfo() {
    return null;
  }
  
  
  public Image icon() {
    return null;
  }
  
  
  public String choiceInfo() {
    return null;
  }
  
  
}




