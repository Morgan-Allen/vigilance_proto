

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import proto.game.person.*;
import java.awt.Image;




public class LeadTail extends Lead {
  
  
  Person tailed;
  Event involved;
  
  
  public LeadTail(Base base, Lead prior, Person tailed) {
    super(base, Task.TIME_INDEF, prior.subject, tailed, new Object[0]);
    this.tailed = tailed;
  }
  
  
  public LeadTail(Session s) throws Exception {
    super(s);
    tailed   = (Person) s.loadObject();
    involved = (Event ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(tailed  );
    s.saveObject(involved);
  }
  
  
  public Place targetLocation() {
    return tailed.place();
  }
  
  
  public void updateAssignment() {
    super.updateAssignment();
    
    final Object task = tailed.assignment();
    if (task instanceof Event) involved = (Event) task;
    
    if (involved != null) {
      attemptTask();
    }
    else if (hoursSoFar() > Task.TIME_LONG) {
      setCompleted(false);
    }
  }
  
  
  protected void onSuccess() {
    I.say("Tailing succeeded!");
    //
    //  TODO:  Present a message for success.
    base.leads.closeLead(this, true);
  }
  
  
  protected void onFailure() {
    I.say("Tailing failed!");
    //
    //  TODO:  Present a message for failure.
    base.leads.closeLead(this, false);
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String choiceInfo() {
    return "Tail "+subject;
  }
  
  
  public String helpInfo() {
    return
      "After tailing "+subject+", they appear to be involved in plans to "+
      involved+".  This could be a chance to catch them red-handed.";
  }
  
  
  public String activeInfo() {
    return "Tailing "+tailed;
  }
  
  
  public Image icon() {
    return tailed.kind().sprite();
  }
}










