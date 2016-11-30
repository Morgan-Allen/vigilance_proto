

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.I;
import proto.game.person.*;
import java.awt.Image;




public class TaskTail extends Task {
  
  
  Person tailed;
  
  
  public TaskTail(Base base, Person tailed) {
    super(base, Task.TIME_SHORT, new Object[0]);
    this.tailed = tailed;
  }
  
  
  public TaskTail(Session s) throws Exception {
    super(s);
    tailed = (Person) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(tailed);
  }
  
  
  public Place targetLocation() {
    return tailed.place();
  }
  
  
  

  public void updateAssignment() {
    super.updateAssignment();
    
    //  TODO:  You need to alert the player if the person tailed visits a new
    //  location or undertakes a new task that is related to an open case.
  }
  
  
  protected void onSuccess() {
    I.say("Tailing succeeded!");
    
    //  TODO:  Present a message for either success or failure.
    
    Event involved = base.world().events.nextEventInvolving(tailed);
    if (involved != null) base.leads.addLead(involved);
    base.leads.closeLead(tailed);
  }
  
  
  protected void onFailure() {
    I.say("Tailing failed!");
    
    base.leads.closeLead(tailed);
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String choiceInfo() {
    return "Tail "+tailed;
  }
  
  
  public String activeInfo() {
    return "Tailing "+tailed;
  }
  
  
  public String helpInfo() {
    return "Follow "+tailed+" for any sign of criminal activity.";
  }
  
  
  public Image icon() {
    return tailed.kind().sprite();
  }
}






