 

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.base.*;



public class CaseFile implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  final public Plot subject;
  List <Clue> clues = new List();
  
  
  
  CaseFile(Base base, Plot subject) {
    this.base    = base   ;
    this.subject = subject;
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base) s.loadObject();
    subject = (Plot) s.loadObject();
    s.loadObjects(clues);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(base   );
    s.saveObject(subject);
    s.saveObjects(clues);
  }
  
  
  
  /**  Recording and transferring evidence-
    */
  public void recordClue(Clue clue, Lead source, int time, Place place) {
    recordClue(clue, source, null, time, place, null, true);
  }
  
  
  public void recordClue(Clue clue, Lead.Type type, int time, Place place) {
    recordClue(clue, null, type, time, place, null, true);
  }
  
  
  public void recordClue(
    Clue clue, Lead source, int time, Place place, boolean report
  ) {
    recordClue(clue, source, null, time, place, null, report);
  }
  
  
  public void recordClue(
    Clue clue, Lead source, Lead.Type type, int time, Place place,
    EventEffects effects, boolean display
  ) {
    for (Clue prior : clues) {
      if (! prior.makesRedundant(clue)) continue;
      return;
    }
    
    if (source != null) clue.confirmSource(source, time, place);
    else                clue.confirmSource(type  , time, place);
    clues.add(clue);
    base.leads.newClues.add(clue);
    
    if (display) {
      MessageUtils.presentClueMessage(base.world().view(), clue, effects);
    }
  }
  
  
  public void updateCluesFrom(CaseFile other) {
    for (Clue clue : other.clues) {
      clues.include(clue);
    }
  }
  
  
  public Series <Clue> clues() {
    return clues;
  }
  
  
  public void wipeRecord() {
    clues.clear();
  }
}





