

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;



public class BaseLeads {
  
  
  final Base base;
  List <Object> openLeads = new List();
  List <Object> shutLeads = new List();
  
  
  BaseLeads(Base base) {
    this.base = base;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(openLeads);
    s.loadObjects(shutLeads);
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(openLeads);
    s.saveObjects(shutLeads);
  }
  
  
  
  
  public void addLead(Session.Saveable lead) {
    openLeads.include(lead);
  }
  
  
  public void closeLead(Session.Saveable lead) {
    openLeads.remove(lead);
    shutLeads.add(lead);
  }
  
  
  public boolean hasOpenLead(Object lead) {
    return openLeads.includes(lead);
  }
  
  
  
  
  //  TODO:  Examine where this should belong.
  public Series <Task> getInvestigationTasks(Object origin) {
    Batch <Task> leads = new Batch();
    
    //  TODO:  Having obtained leads, however, you still need to generate
    //  associated tasks for investigation.
    
    //  In addition, there may be multiple methods for investigating a single
    //  object (e.g, you could either tail, persuade or intimidate a suspect,
    //  stake out or guard a building, bug or sabotage a vehicle, swab or dust
    //  for clues, etc..)  And not all methods will yield immediate dividends.
    
    //  So... each of these methods of investigation will reveal different
    //  leads.  I think.
    
    //  Each Kind of element should yield different methods of investigation.
    
    
    //  As a general rule... whenever a given object becomes (or is shown to be)
    //  involved in an ongoing criminal plot, then it should be added as a lead.
    
    if (origin instanceof Item || origin instanceof Clue) {
      //  TODO:  Forensics.
      
    }
    
    if (origin instanceof Person) {
      //  TODO:  Tailing, Guard, Dialog, Interrogate/Detain.
      
    }
    
    if (origin instanceof Place) {
      //  TODO:  Stake-out, Guard, Search/Inspect.
      
    }
    
    if (origin instanceof Event) {
      
    }
    
    return leads;
  }
}








