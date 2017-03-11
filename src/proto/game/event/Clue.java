

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Clue {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Crime crime;
  Crime.Role role;

  Object match;
  boolean confirmed;
  Trait trait;
  
  Lead.Type leadType;
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
    c.confirmed  = s.loadBool();
    c.trait      = (Trait) s.loadObject();
    c.leadType   = Lead.LEAD_TYPES[s.loadInt()];
    c.confidence = s.loadFloat();
    c.timeFound  = s.loadInt();
    return c;
  }
  
  
  void saveClue(Session s) throws Exception {
    s.saveObject(crime      );
    s.saveObject(role       );
    s.saveObject(match      );
    s.saveBool  (confirmed  );
    s.saveObject(trait      );
    s.saveInt   (leadType.ID);
    s.saveFloat (confidence );
    s.saveInt   (timeFound  );
  }
  
  
  void assignEvidence(
    Object match, Trait trait,
    Lead.Type leadType, float confidence, int timeFound
  ) {
    this.match      = match     ;
    this.trait      = trait     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
    this.timeFound  = timeFound ;
  }
  
  
  void confirmMatch(
    Object match, Lead.Type leadType, int timeFound
  ) {
    this.match      = match    ;
    this.confirmed  = true     ;
    this.leadType   = leadType ;
    this.confidence = 1.0f     ;
    this.timeFound  = timeFound;
  }
  
  
  boolean matchesType(Clue other) {
    if (crime != other.crime) return false;
    if (role  != other.role ) return false;
    if (trait != other.trait) return false;
    if (match != other.match) return false;
    return true;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    if (match != null && confirmed) return
      leadType.name+" indicates "+
      role+" is: "+match
    ;
    if (trait != null) return
      leadType.name+" indicates "+
      role+" has trait: "+trait
    ;
    return "";
  }
}





