

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
  List <Crime> currentCrimes = new List();
  
  
  
  public BasePlans(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    crimeTypes  = (CrimeType[]) s.loadObjectArray(CrimeType.class);
    masterCrime = (Crime) s.loadObject();
    s.loadObjects(currentCrimes);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(crimeTypes);
    s.saveObject(masterCrime);
    s.saveObjects(currentCrimes);
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
    
  }
  
}









