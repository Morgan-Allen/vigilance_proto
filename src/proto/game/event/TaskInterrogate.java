

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import java.awt.Image;




public class TaskInterrogate extends Task {
  
  
  Person tailed;
  
  
  public TaskInterrogate(Base base, Person tailed) {
    super(base, Task.TIME_SHORT, new Object[0]);
  }
  
  
  public TaskInterrogate(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public Place targetLocation() {
    return tailed.location();
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
