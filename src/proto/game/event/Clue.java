

package proto.game.event;
import proto.common.*;
import proto.game.world.*;



public class Clue {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Crime crime;
  Crime.Role role;
  Object match;
  
  int leadType;
  Trait trait;
  float confidence;
  int timeTaken;
  
  
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
    c.leadType   = s.loadInt();
    c.trait      = (Trait) s.loadObject();
    c.confidence = s.loadFloat();
    c.timeTaken  = s.loadInt();
    return c;
  }
  
  
  void saveClue(Session s) throws Exception {
    s.saveObject(crime     );
    s.saveObject(role    );
    s.saveObject(match     );
    s.saveInt   (leadType  );
    s.saveObject(trait     );
    s.saveFloat (confidence);
    s.saveInt   (timeTaken );
  }
  
  
  void assignEvidence(
    Trait trait, int leadType, float confidence, int timeTaken
  ) {
    this.trait      = trait     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
    this.timeTaken  = timeTaken ;
  }
  
  
  void confirmMatch(Object match, int leadType, int timeTaken) {
    this.match      = match    ;
    this.leadType   = leadType ;
    this.confidence = 1.0f     ;
    this.timeTaken  = timeTaken;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    if (trait != null) return
      Lead.LEAD_DESC[leadType]+" indicates "+
      role.entryKey()+" has trait: "+trait
    ;
    return "";
  }
}





