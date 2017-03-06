

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class BaseLeads {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  Table <Object, CaseFile> files = new Table();
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadTable(files);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveTable(files);
  }
  
  
  public void updateLeads() {
  }
  
  
  
  /**  Assorted utility methods-
    */
  public CaseFile caseFor(Object subject) {
    CaseFile match = files.get(subject);
    if (match != null) return match;
    final CaseFile file = new CaseFile(base, subject);
    files.put(subject, file);
    return file;
  }
  
  
  public Series <Clue> cluesFor(
    Crime crime, Object match, Crime.RoleType roleID
  ) {
    Batch <Clue> matches = new Batch();
    CaseFile file = caseFor(match);
    
    for (Clue c : file.clues) {
      if (crime  != null && crime  != c.crime ) continue;
      if (match  != null && match  != c.match ) continue;
      if (roleID != null && roleID != c.roleID) continue;
      matches.add(c);
    }
    return matches;
  }
  
  
  public Series <Crime> involvedIn(Object subject) {
    Batch <Crime> matches = new Batch();
    CaseFile file = caseFor(subject);
    
    for (Clue c : file.clues) {
      matches.include(c.crime);
    }
    return matches;
  }
  
  
  public float evidenceForInvolvement(Crime crime, Object subject) {
    CaseFile file = caseFor(subject);
    float evidence = 0;
    
    for (Clue c : file.clues) if (c.crime == crime) {
      evidence += c.confidence;
    }
    return evidence;
  }
  
  
  
}




