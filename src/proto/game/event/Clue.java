

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Clue {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Crime crime;
  Crime.Role role;
  Object match;
  
  Lead.Type leadType;
  Trait trait;
  float confidence;
  int timeFound;
  
  
  Clue() {
    return;
  }
  
  
  Clue(Crime crime, Crime.Role role) {
    this.crime = crime;
    this.role  = role ;
  }
  
  
  static Clue loadClue(Session s) throws Exception {
    Clue c = new Clue();
    c.crime      = (Crime     ) s.loadObject();
    c.role       = (Crime.Role) s.loadObject();
    c.match      = s.loadObject();
    c.leadType   = Lead.LEAD_TYPES[s.loadInt()];
    c.trait      = (Trait) s.loadObject();
    c.confidence = s.loadFloat();
    c.timeFound  = s.loadInt();
    return c;
  }
  
  
  void saveClue(Session s) throws Exception {
    s.saveObject(crime      );
    s.saveObject(role       );
    s.saveObject(match      );
    s.saveInt   (leadType.ID);
    s.saveObject(trait      );
    s.saveFloat (confidence );
    s.saveInt   (timeFound  );
  }
  
  
  void assignEvidence(
    Trait trait, Lead.Type leadType, float confidence, int timeFound
  ) {
    this.trait      = trait     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
    this.timeFound  = timeFound ;
  }
  
  
  void confirmMatch(Object match, Lead.Type leadType, int timeFound) {
    this.match      = match    ;
    this.leadType   = leadType ;
    this.confidence = 1.0f     ;
    this.timeFound  = timeFound;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    if (trait != null) return
      leadType.name+" indicates "+
      role.entryKey()+" has trait: "+trait
    ;
    return "";
  }
}





