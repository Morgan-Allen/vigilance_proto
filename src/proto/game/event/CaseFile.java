 

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
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
    s.loadObjects(clues);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base   );
    s.saveObject(subject);
    s.saveObjects(clues);
  }
  
  
  
  /**  Recording and transferring evidence-
    */
  public void recordClue(Clue clue) {
    recordClue(clue, null);
  }
  
  
  public void recordClue(Clue clue, EventEffects effects) {
    for (Clue prior : clues) if (prior.makesRedundant(clue)) {
      ///I.say("\nClue was redundant: "+clue.longDescription(base));
      return;
    }
    clues.add(clue);
    MessageUtils.presentClueMessage(clue, base.world().view(), effects);
  }
  
  
  public void updateEvidenceFrom(CaseFile other) {
    for (Clue clue : other.clues) {
      clues.include(clue);
    }
  }
  
}




