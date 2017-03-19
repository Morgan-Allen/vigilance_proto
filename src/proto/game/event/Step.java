

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



//  TODO:  Have this extend Task?

public class Step {
  
  
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
  
  
  
  void saveStep(Session s) throws Exception {
    s.saveObjectArray(involved);
    s.saveInt (medium   );
    s.saveInt (timeTaken);
    s.saveInt (ID       );
    
    s.saveObject(meetsAt  );
    s.saveObject(infoGiven);
    
    s.saveInt (timeStart);
    s.saveBool(spooked  );
  }
  
  
  static Step loadStep(Session s) throws Exception {
    Step step = new Step();
    step.involved  = (Plot.Role[]) s.loadObjectArray(Plot.Role.class);
    step.medium    = s.loadInt();
    step.timeTaken = s.loadInt();
    step.ID        = s.loadInt();
    
    step.meetsAt   = (Plot.Role) s.loadObject();
    step.infoGiven = (Plot.Role) s.loadObject();
    
    step.timeStart = s.loadInt();
    step.spooked   = s.loadBool();
    return step;
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







