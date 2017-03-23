

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



//  TODO:  Have this extend Task?

public class Step implements Session.Saveable {
  
  
  /**  Data fields and save/load methods-
    */
  Plot.Role involved[];
  int medium;
  int timeTaken;
  int ID;
  
  Plot.Role meetsAt;
  Plot.Role infoGiven;
  
  int timeStart = -1;
  boolean spooked = false;
  
  
  Step() {
    return;
  }
  
  
  public Step(Session s) throws Exception {
    involved  = (Plot.Role[]) s.loadObjectArray(Plot.Role.class);
    medium    = s.loadInt();
    timeTaken = s.loadInt();
    ID        = s.loadInt();
    
    meetsAt   = (Plot.Role) s.loadObject();
    infoGiven = (Plot.Role) s.loadObject();
    
    timeStart = s.loadInt();
    spooked   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(involved);
    s.saveInt (medium   );
    s.saveInt (timeTaken);
    s.saveInt (ID       );
    
    s.saveObject(meetsAt  );
    s.saveObject(infoGiven);
    
    s.saveInt (timeStart);
    s.saveBool(spooked  );
  }
  
  
  
  /**  Assorted no-brainer access methods-
    */
  public boolean isHeist() {
    return medium == Lead.MEDIUM_HEIST;
  }
  
  
  public boolean isPhysical() {
    return Lead.isPhysical(medium);
  }
  
  
  public boolean isMeeting() {
    return Lead.isSocial(medium);
  }
  
  
  public boolean isWired() {
    return Lead.isWired(medium);
  }
  
  
  
  /**  Additional config and support methods-
    */
  public void setMeetsAt(Plot.Role meetsAt) {
    this.meetsAt = meetsAt;
  }
  
  
  public void setInfoGiven(Plot.Role infoGiven) {
    this.infoGiven = infoGiven;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Step involving "+I.list(involved);
  }
}







