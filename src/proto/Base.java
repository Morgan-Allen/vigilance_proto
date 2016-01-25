

package proto;
import util.*;



public class Base implements Session.Saveable {
  
  final static int
    MAX_FACILITIES = 8;
  
  
  List <Person> roster = new List();
  Facility facilities[] = new Facility[MAX_FACILITIES];
  float facilityProgress[] = new float[MAX_FACILITIES];
  
  int currentFunds = 0, income = 0;
  
  
  Base() {
    
  }
  
  
  public Base(Session s) throws Exception {
    s.cacheInstance(this);
    s.loadObjects(roster);
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      facilities[n] = (Facility) s.loadObject();
      facilityProgress[n] = s.loadFloat();
    }
    
    currentFunds = s.loadInt();
    income = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjects(roster);

    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      s.saveObject(facilities[n]);
      s.saveFloat(facilityProgress[n]);
    }
    
    s.saveInt(currentFunds);
    s.saveInt(income);
  }
  
}














