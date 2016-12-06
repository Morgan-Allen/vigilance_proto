

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class BaseLeads {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  Table <Object, Lead> allLeads = new Table();
  List <Lead> leadsList = new List();
  
  
  
  public BaseLeads(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    s.loadObjects(leadsList);
    for (Lead l : leadsList) allLeads.put(l.subject, l);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjects(leadsList);
  }
  
  
  
  /**  Processing open and dead leads-
    */
  public void leadOpened(Lead lead) {
    final Lead match = allLeads.get(lead.subject);
    if (match != null) closeLead(lead.subject);
    
    allLeads.put(lead.subject, lead);
    leadsList.add(lead);
    lead.populateInvestigationOptions();
    
    //  TODO:  You need to present a pop-up message for the sake of alerting
    //  the player.
    if (base == base.world().playerBase()) {
      base.world().pauseMonitoring();
    }
  }
  
  
  public void closeLead(Object subject) {
    final Lead match = allLeads.get(subject);
    if (match == null) return;
    match.open = false;
    allLeads.remove(subject);
    leadsList.remove(match);
    
    //  TODO:  Close up any associated investigation tasks.
  }
  
  
  public boolean hasOpenLead(Object origin) {
    final Lead match = allLeads.get(origin);
    return match != null && match.open;
  }
  
  
  
  public boolean hasOpenLeadAround(Region region) {
    return ! openLeadsAround(region).empty();
  }
  
  
  public Series <Lead> openLeadsAround(Region region) {
    final Batch <Lead> leads = new Batch();
    for (Lead lead : leadsList) {
      if (lead.targetLocation().region() == region) {
        leads.add(lead);
      }
    }
    return leads;
  }
  
  
  
  /**  Handling tasks associated with a particular lead-
    */
  public void updateInvestigations() {
    for (Lead lead : leadsList) {
      for (Task task : lead.followOptions) {
        if (task.assigned().empty()) continue;
        task.updateAssignment();
      }
    }
  }
}



