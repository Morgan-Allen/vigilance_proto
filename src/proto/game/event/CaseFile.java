 

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.base.*;



public class CaseFile implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  final public Object subject;
  List <Clue> clues = new List();
  
  
  
  CaseFile(Base base, Object subject) {
    this.base    = base   ;
    this.subject = subject;
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base) s.loadObject();
    subject =        s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      clues.add(Clue.loadClue(s));
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base   );
    s.saveObject(subject);
    
    s.saveInt(clues.size());
    for (Clue c : clues) c.saveClue(s);
  }
  
  
  
  /**  Recording and transferring evidence-
    */
  public void recordClue(Clue clue) {
    for (Clue prior : clues) {
      if (prior.matchesType(clue)) return;
    }
    clues.add(clue);
    MessageUtils.presentMessageFor(clue, base.world().view());
  }
  
  
  public void updateEvidenceFrom(CaseFile other) {
    for (Clue clue : clues) {
      other.clues.include(clue);
    }
  }
  
}




