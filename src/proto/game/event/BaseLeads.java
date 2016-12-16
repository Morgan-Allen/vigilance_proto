

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class BaseLeads {
  
  
  final Base base;
  List <CaseFile> files = new List();
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadObjects(files);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjects(files);
  }
  
  
  public void updateInvestigations() {
    for (CaseFile file : files) for (Lead lead : file.investigationOptions()) {
      if (lead.assigned().empty()) continue;
      lead.updateAssignment();
    }
  }
  
  
  
  /**  Assorted utility methods-
    */
  public CaseFile caseFor(Object subject) {
    for (CaseFile file : files) if (file.subject == subject) {
      return file;
    }
    final CaseFile file = new CaseFile(base, subject);
    files.add(file);
    I.say("Creating case file for: "+subject+", location: "+file.trueLocation());
    return file;
  }
  
  
  public Event threateningEvent(Element subject) {
    for (CaseFile file : files) {
      Event e = file.threatens(subject);
      if (e != null) return e;
    }
    return null;
  }
  
  
  public Batch <CaseFile> casesForRegion(Region region) {
    final Batch <CaseFile> cases = new Batch();
    for (CaseFile file : files) if (file.lastSuspectEvent() != null) {
      Place seen = file.trueLocation();
      if (seen != null && seen.region() == region) cases.add(file);
    }
    return cases;
  }
  
}











