

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class CaseFile implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  //  TODO:  Consider making this specific to Crimes, rather than arbitrary
  //  objects?
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
  
  
  
  /**  Resolving possible suspects:
    */
  public void recordClue(Clue clue) {
    for (Clue prior : clues) {
      if (prior.matchesType(clue)) return;
    }
    clues.add(clue);
  }
  
  
  public Series <Element> matchingSuspects(
    Series <? extends Element> possible
  ) {
    Batch <Element> matches = new Batch();
    search: for (Element e : possible) {
      for (Clue c : clues) if (! c.matchesSuspect(e)) continue search;
      matches.add(e);
    }
    return matches;
  }
  
  
  
  /**  Extraction methods for use in sentencing-
    */
  public void updateEvidenceFrom(CaseFile other) {
    //  TODO:  Restore this!
    /*
    for (Role role : other.roles) for (Lead lead : role.evidence) {
      recordRole(role.event, role.roleID, lead);
    }
    //*/
  }
  
  
}






