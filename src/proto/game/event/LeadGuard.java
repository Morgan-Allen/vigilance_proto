

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
    Place place = event.targetLocation();
    SceneType sceneType = place.kind().sceneType();
    Scene mission = sceneType.generateScene(place.world());
    event.populateScene(mission);
    mission.entry.provideBorderEntry(active());
    mission.assignMissionParameters(place, this, event);
    base.world().enterScene(mission);
  }
  
  
  public void onSceneExit(Scene scene, EventReport report) {
    CaseFile file = base.leads.caseFor(guarded);
    file.refreshInvestigationOptions();
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




