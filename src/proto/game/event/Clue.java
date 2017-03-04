

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Clue {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Object subject;
  Crime crime;
  int roleID;
  
  int leadType;
  Object trait;
  float confidence;
  
  
  Clue() {
    return;
  }
  
  
  Clue(Object subject, Crime crime, int roleID) {
    this.subject = subject;
    this.crime   = crime  ;
    this.roleID  = roleID ;
  }
  
  
  static Clue loadClue(Session s) throws Exception {
    Clue c = new Clue();
    c.subject    = s.loadObject();
    c.crime      = (Crime) s.loadObject();
    c.roleID     = s.loadInt();
    c.leadType   = s.loadInt();
    c.trait      = s.loadObject();
    c.confidence = s.loadFloat();
    return c;
  }
  
  
  void saveClue(Session s) throws Exception {
    s.saveObject(subject   );
    s.saveObject(crime     );
    s.saveInt   (roleID    );
    s.saveInt   (leadType  );
    s.saveObject(trait     );
    s.saveFloat (confidence);
  }
  
  
  void assignEvidence(Object trait, int leadType, float confidence) {
    this.trait      = trait     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
  }
}








