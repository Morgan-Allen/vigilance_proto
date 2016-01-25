

package proto;
import util.*;


//  TODO:  Introduce facility types!
//  Infirmary.      (Allows 2 league members to recover injury faster.)
//  Training room.  (Allows 2 league members to gain XP outside missions.)
//  Rec hall.       (Allows 2 league members to recover stress faster.)
//  Sensor array.   (Increases chance of detecting crises & ground intel.)
//  Generator.      (Increases no. of facilities that can be installed.)

//  Cybernetic Lab
//  Biology Lab
//  Psych Lab
//  Library



public class Facility extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Facility> INDEX = new Index <Facility> ();
  
  
  int powerCost;
  String name;
  
  
  Facility(String name, String uniqueID) {
    super(INDEX, uniqueID);
    this.name = name;
  }
  
  
  public static Facility loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
}





