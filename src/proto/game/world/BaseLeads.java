

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;



public class BaseLeads {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  static class Lead {
    Object origin;
    List <Task> followOptions = new List();
    boolean open;
  }
  Table <Object, Lead> allLeads = new Table();
  List <Lead> leadsList = new List();
  
  
  
  BaseLeads(Base base) {
    this.base = base;
  }
  
  
  void loadState(Session s) throws Exception {
    
    for (int n = s.loadInt(); n-- > 0;) {
      Lead l = new Lead();
      l.origin = s.loadObject();
      s.loadObjects(l.followOptions);
      l.open = s.loadBool();
      allLeads.put(l.origin, l);
      leadsList.add(l);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    
    s.saveInt(allLeads.size());
    for (Lead l : leadsList) {
      s.saveObject(l.origin);
      s.saveObjects(l.followOptions);
      s.saveBool(l.open);
    }
  }
  
  
  
  /**  Processing open and dead leads-
    */
  public void addLead(Session.Saveable origin) {
    final Lead match = allLeads.get(origin);
    if (match != null) closeLead(origin);
    
    Lead lead = new Lead();
    lead.origin = origin;
    addInvestigationOptions(origin, lead.followOptions);
    allLeads.put(origin, lead);
    leadsList.add(lead);
    
    //  TODO:  You need to present a pop-up message for the sake of alerting
    //  the player.
    if (base == base.world.playerBase()) {
      base.world.pauseMonitoring();
    }
  }
  
  
  public void closeLead(Object origin) {
    final Lead match = allLeads.get(origin);
    if (match == null) return;
    match.open = false;
    allLeads.remove(origin);
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
  
  
  public Series <Object> openLeadsAround(Region region) {
    final Batch <Object> leads = new Batch();
    for (Lead lead : leadsList) {
      if (lead.origin instanceof Element) {
        final Element e = (Element) lead.origin;
        if (e.region() == region) leads.add(e);
      }
      if (lead.origin instanceof Event) {
        final Event e = (Event) lead.origin;
        if (e.place().region() == region) leads.add(e);
      }
    }
    return leads;
  }
  
  
  
  /**  Handling tasks associated with a particular lead-
    */
  void updateInvestigations() {
    for (Lead lead : leadsList) {
      for (Task task : lead.followOptions) {
        if (task.assigned().empty()) continue;
        task.updateAssignment();
      }
    }
  }
  
  
  public Series <Task> investigationOptions(Object origin) {
    final Lead match = allLeads.get(origin);
    return match == null ? null : match.followOptions;
  }
  
  
  //  TODO:  Examine where this should belong?
  private void addInvestigationOptions(Object origin, Series <Task> options) {
    
    //  In addition, there may be multiple methods for investigating a single
    //  object (e.g, you could either tail, persuade or intimidate a suspect,
    //  stake out or guard a building, bug or sabotage a vehicle, swab or dust
    //  for clues, etc..)  And not all methods will yield immediate dividends.
    
    if (origin instanceof Item || origin instanceof Clue) {
      //  TODO:  Forensics.
    }
    
    if (origin instanceof Person) {
      //  TODO:  Tailing, Guard, Dialog, Interrogate/Detain.
      final Person person = (Person) origin;
      Task tailing = new TaskTail(base, person);
      options.add(tailing);
    }
    
    if (origin instanceof Place) {
      //  TODO:  Stake-out, Guard, Search/Inspect.
    }
    
    if (origin instanceof Event) {
      final Event event = (Event) origin;
      final boolean dangerous = event.type.isDangerous(event);
      
      if (dangerous && ! event.complete()) {
        Task guarding = new TaskGuard(base, event);
        options.add(guarding);
      }
      if (dangerous && event.complete()) {
        Task searching = new TaskSearch(base, event.place());
        options.add(searching);
      }
      
      //  TODO:  Allow a search for *all* completed events?
      
      if (event.planStep() != null && ! dangerous) {
        for (Element e : event.planStep().gives()) {
          Person carries = (Person) e.parentOfType(Kind.TYPE_PERSON);
          if (carries == null) continue;
          Task tailing = new TaskTail(base, carries);
          options.add(tailing);
        }
        closeLead(event);
      }
    }
  }
}



