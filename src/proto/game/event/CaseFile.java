 

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
  final Plot subject;
  List <Clue> clues = new List();
  int caseID;
  
  
  
  CaseFile(Base base, Plot subject) {
    this.base    = base   ;
    this.subject = subject;
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base) s.loadObject();
    subject = (Plot) s.loadObject();
    s.loadObjects(clues);
    caseID = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject (base   );
    s.saveObject (subject);
    s.saveObjects(clues  );
    s.saveInt    (caseID );
  }
  
  
  public int caseID() {
    return caseID;
  }
  
  
  
  /**  Recording and transferring evidence-
    */
  public void recordClue(Clue clue, Lead source, int time, Place found) {
    recordClue(clue, source, null, time, found, null, true);
  }
  
  
  public void recordClue(Clue clue, LeadType type, int time, Place found) {
    recordClue(clue, null, type, time, found, null, true);
  }
  
  
  public void recordClue(
    Clue clue, Lead source, int time, Place found, boolean report
  ) {
    recordClue(clue, source, null, time, found, null, report);
  }
  
  
  public void recordClue(
    Clue clue, LeadType type, int time, Place found, boolean report
  ) {
    recordClue(clue, null, type, time, found, null, report);
  }
  
  
  public boolean recordClue(
    Clue clue, Lead source, LeadType type, int time, Place found,
    EventEffects effects, boolean display
  ) {
    if (source != null) clue.confirmSource(source, time, found);
    else                clue.confirmSource(type  , time, found);
    
    if (isRedundant(clue)) return false;
    
    clues.add(clue);
    base.leads.newClues.add(clue);
    clue.plot().incCluesCount(clue.role(), 1);
    
    if (display) {
      MessageUtils.presentClueMessage(base.world().view(), clue, effects);
    }
    return true;
  }
  
  
  public boolean isRedundant(Clue clue) {
    for (Clue prior : clues) {
      if (prior.makesRedundant(clue)) return true;
    }
    return false;
  }
  
  
  public void updateCluesFrom(CaseFile other) {
    for (Clue clue : other.clues) {
      clues.include(clue);
    }
  }
  
  
  public Plot subject() {
    return subject;
  }
  
  
  public Series <Clue> clues() {
    return clues;
  }
  
  
  public void wipeRecord() {
    clues.clear();
  }
}





