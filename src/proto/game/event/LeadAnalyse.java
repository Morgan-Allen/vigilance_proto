

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import java.awt.Image;



public class LeadAnalyse extends Lead {
  
  
  Item analysed;
  
  
  public LeadAnalyse(Base base, Item analysed) {
    super(base, Task.TIME_SHORT, analysed, new Object[0]);
    this.analysed = analysed;
  }
  
  
  public LeadAnalyse(Session s) throws Exception {
    super(s);
    analysed = (Item) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(analysed);
  }
  
  
  public Place targetLocation() {
    return analysed.place();
  }
  
  
  

  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    
    //  TODO:  You need to alert the player if the person tailed visits a new
    //  location or undertakes a new task that is related to an open case.
    
    /*
    if (tailed.assignment() instanceof Event) {
      
    }
    if (base.leads.hasOpenLead(tailed.location())) {
      
    }
    //*/
    return true;
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
  
  
  public String choiceInfo(Person p) {
    return null;
  }
}



