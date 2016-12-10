

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public class LeadGuard extends Lead {
  
  
  Event event;
  
  
  public LeadGuard(Base base, Lead prior, Event event) {
    super(base, Task.TIME_SHORT, prior.subject, event, new Object[0]);
    this.event = event;
  }
  
  
  public LeadGuard(Session s) throws Exception {
    super(s);
    event = (Event) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(event);
  }
  
  
  public Place targetLocation() {
    return event.targetLocation();
  }
  
  
  protected void onSuccess() {
    base.leads.closeLead(this, true);
    
    //  TODO:  This should only be triggered when the event itself is either
    //  going down or about to do so!
    
    Place place = event.targetLocation();
    SceneType sceneType = place.kind().sceneType();
    Scene mission = sceneType.generateScene(place, 32);
    
    event.populateScene(mission);
    
    //
    //  Finally, introduce the agents themselves-
    int across = (32 - (assigned().size())) / 2;
    for (Person p : assigned()) {
      mission.addToTeam(p);
      mission.enterScene(p, across++, 0);
    }
    
    mission.assignMissionParameters(place, this, event);
    base.world().enterScene(mission);
  }
  
  
  protected void onFailure() {
    base.leads.closeLead(this, false);
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public Image icon() {
    return event.targetLocation().icon();
  }
  
  
  public String choiceInfo() {
    return "Guard "+event.targetLocation();
  }
  
  
  public String activeInfo() {
    return "Guarding "+event.targetLocation()+" during event: "+event;
  }
  
  
  public String helpInfo() {
    return "Guard "+event.targetLocation()+" to foil event: "+event;
  }
  
  
}




