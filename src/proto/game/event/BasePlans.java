

package proto.game.event;
import proto.game.world.*;
import proto.common.*;
import proto.util.*;



public class BasePlans {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  CrimeType crimeTypes[] = new CrimeType[0];
  Crime masterCrime;
  
  
  
  public BasePlans(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    crimeTypes  = (CrimeType[]) s.loadObjectArray(CrimeType.class);
    masterCrime = (Crime) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(crimeTypes);
    s.saveObject(masterCrime);
  }
  
  
  public void assignCrimeTypes(CrimeType... types) {
    this.crimeTypes = types;
  }
  
  
  public Crime generateNextCrime() {
    return null;
  }
  
  
  public void assignMasterCrime(Crime master) {
    this.masterCrime = master;
  }
  
  
  public void updatePlanning() {
    if (masterCrime != null) {
      //  TODO:  Add a delay here...
      
      if (! masterCrime.scheduled()) {
        base.world().events.scheduleEvent(masterCrime);
      }
    }
  }
  
}



