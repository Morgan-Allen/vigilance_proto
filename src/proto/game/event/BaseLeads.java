

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class BaseLeads {
  
  
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
    I.say("Creating case file for: "+subject);
    return file;
  }
  
  
  Series <Clue> concerning(Crime crime, Element subject, int roleID) {
    Batch <Clue> matches = new Batch();
    CaseFile file = caseFor(subject);
    
    for (Clue c : file.clues) {
      if (crime   != null && crime   != c.crime  ) continue;
      if (subject != null && subject != c.subject) continue;
      if (roleID  != -1   && roleID  != c.roleID ) continue;
      matches.add(c);
    }
    return matches;
  }
  
}




