

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class BaseLeads {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  List <Lead> leadsList = new List();
  Table <Object, Lead> leadsTable = new Table();
  
  List <CaseFile> filesList = new List();
  Table <Object, CaseFile> filesTable = new Table();
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadObjects(leadsList);
    s.loadObjects(filesList);
    for (Lead     l : leadsList) leadsTable.put(l.uniqueKey, l);
    for (CaseFile f : filesList) filesTable.put(f.subject  , f);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjects(leadsList);
    s.saveObjects(filesList);
  }
  
  
  
  /**  Processing open and dead leads-
    */
  public Lead openLead(Lead lead) {
    //
    //  If a previous lead already matches the signature of this lead, and is
    //  still open, don't add it.  If it's there, but already finished, you
    //  can replace it.
    
    //I.say("Adding lead: "+lead);
    Lead prior = leadsTable.get(lead);
    if (prior != null && ! prior.complete()) {
      //I.say("  Prior lead of type isn't complete");
      return prior;
    }
    if (prior != null) {
      //I.say("  Removing prior lead");
      leadsList.remove(prior);
      recordLead(prior, false);
    }
    
    leadsList.add(lead);
    leadsTable.put(lead.uniqueKey, lead);
    recordLead(lead, true);
    return lead;
  }
  
  
  public void closeLead(Lead lead, boolean success) {
    
    if (! lead.complete()) {
      openLead(lead);
      lead.setCompleted(success);
    }
    
    if (success) for (Lead out : lead.investigationOptions()) {
      openLead(out);
    }
    //
    //  TODO:  Consider recording everything instead, as a permanent history of
    //  the case?
    else {
      leadsList.remove(lead);
      leadsTable.remove(lead.uniqueKey);
      recordLead(lead, false);
    }
    
    base.world().pauseMonitoring();
  }
  
  
  private void recordLead(Lead lead, boolean yes) {
    CaseFile from = fileOn(lead.origin), goes = fileOn(lead.subject);
    from.outgoing.toggleMember(lead, yes);
    goes.incoming.toggleMember(lead, yes);
  }
  
  
  private CaseFile fileOn(Object subject) {
    CaseFile file = filesTable.get(subject);
    if (file == null) {
      filesTable.put(subject, file = new CaseFile(subject));
      filesList.add(file);
    }
    return file;
  }
  
  
  //  TODO:  Return CaseFiles instead, and let the UI methods do all of this
  //  sorting.
  
  public Series <Lead> confirmedLeadsAround(Region region) {
    final Batch <Lead> leads = new Batch();
    for (CaseFile file : filesList) {
      //
      //  First, we check if the case has any open leads concerned with the
      //  argument region:
      boolean hasOpenLead = false;
      for (Lead l : file.outgoing) {
        if (l.targetLocation().region() != region) continue;
        if (! l.complete()) hasOpenLead = true;
      }
      //
      //  Then, we get the latest confirmed lead toward the case-subject for
      //  use a description, and add that to the list:
      Lead goes = null;
      if (hasOpenLead) for (Lead l : file.incoming) {
        if (l.complete() && l.success()) goes = l;
      }
      if (goes != null) leads.add(goes);
    }
    return leads;
  }
  
  
  public Series <Lead> openLeadsFrom(Object subject) {
    final Batch <Lead> open = new Batch();
    final CaseFile file = filesTable.get(subject);
    if (file == null) return open;
    for (Lead l : file.outgoing) if (! l.complete()) open.add(l);
    return open;
  }
  
  
  
  /**  Handling tasks associated with a particular lead-
    */
  public void updateInvestigations() {
    for (Lead task : leadsList) {
      if (task.assigned().empty()) continue;
      task.updateAssignment();
    }
  }
}











