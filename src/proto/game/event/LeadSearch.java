

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import java.awt.Image;




public class LeadSearch extends Lead {
  
  
  Place searched;
  
  
  public LeadSearch(Base base, Lead prior, Place searched) {
    super(base, Task.TIME_SHORT, prior.subject, searched, new Object[0]);
    this.searched = searched;
  }
  
  
  public LeadSearch(Session s) throws Exception {
    super(s);
    searched = (Place) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(searched);
  }
  
  
  public Place targetLocation() {
    return searched;
  }
  
  
  

  public void updateAssignment() {
    super.updateAssignment();
    
    //  TODO:  You need to alert the player if the person tailed visits a new
    //  location or undertakes a new task that is related to an open case.
    
    /*
    if (tailed.assignment() instanceof Event) {
      
    }
    if (base.leads.hasOpenLead(tailed.location())) {
      
    }
    //*/
  }
  

  protected void onCompletion() {
    resetTask();
  }
  
  
  protected void onSuccess() {
  }
  
  
  protected void onFailure() {
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String activeInfo() {
    return null;
  }
  
  
  public String helpInfo() {
    return null;
  }
  
  
  public Image icon() {
    return null;
  }
  
  
  public String choiceInfo() {
    return null;
  }
}




