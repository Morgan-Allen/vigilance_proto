 

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
  final public Object subject;
  List <Clue> clues = new List();
  
  
  
  CaseFile(Base base, Object subject) {
    this.base    = base   ;
    this.subject = subject;
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    base    = (Base  ) s.loadObject();
    subject = (Object) s.loadObject();
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
    recordClue(clue, null, true);
  }
  
  
  public void recordClue(Clue clue, EventEffects effects, boolean display) {
    for (Clue prior : clues) if (prior.makesRedundant(clue)) {
      ///I.say("\nClue was redundant: "+clue.longDescription(base));
      return;
    }
    clues.add(clue);
    
    if (display) {
      MessageUtils.presentClueMessage(base.world().view(), clue, effects);
    }
  }
  
  
  public void updateEvidenceFrom(CaseFile other) {
    for (Clue clue : other.clues) {
      clues.include(clue);
    }
  }
  
  
  
  /**  Determining location (which constrains whether leads can be followed.)
    */
  public Element subjectAsElement() {
    if (! (subject instanceof Element)) return null;
    return (Element) subject;
  }
  
  
  public Element knownLocation() {
    Element subject = subjectAsElement();
    if (subject == null) return null;
    
    if (subject.isPlace() || subject.isRegion()) {
      return subject;
    }
    for (Clue clue : clues) if (clue.isLocationClue()) {
      if (clue.location.isPlace() && clue.nearRange == 0) {
        return (Place) clue.location;
      }
    }
    if (subject.isPerson()) {
      return ((Person) subject).resides();
    }
    return null;
  }
  
  
  public boolean subjectAtKnownLocation() {
    Element subject = subjectAsElement();
    if (subject == null) return false;
    
    if (subject.isPlace() || subject.isRegion()) return true;
    return knownLocation() == subject.place();
  }
  
  
  public boolean subjectIsHideout() {
    for (Clue clue : clues) {
      if (clue.plot.complete()) continue;
      if (clue.confirmed && clue.role == Plot.ROLE_HIDEOUT) return true;
    }
    return false;
  }
}




