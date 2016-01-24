

package proto;
import util.*;



public class Base {
  
  final static int
    MAX_FACILITIES = 8;
  
  
  List <Person> roster = new List();
  Facility facilities[] = new Facility[MAX_FACILITIES];
  float facilityProgress[] = new float[MAX_FACILITIES];
  
  int currentFunds = 0, income = 0;
  
  //  TODO:  Introduce facility types!
  //  Infirmary.      (Allows 2 league members to recover injury faster.)
  //  Training room.  (Allows 2 league members to gain XP outside missions.)
  //  Rec hall.       (Allows 2 league members to recover stress faster.)
  //  Sensor array.   (Increases chance of detecting crises & ground intel.)
  //  Generator.      (Increases no. of facilities that can be installed.)
  //  Laboratory.     (Increases chance to analyse clues gathered?)
  
}


class Facility {
  
  int powerCost;
  
  
  
  
}












