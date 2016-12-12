

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import java.awt.Image;



public class LeadTipoff extends Lead {
  
  
  public LeadTipoff(Base base, Element about) {
    super(base, Task.TIME_SHORT, about, new Object[0]);
  }
  
  
  public LeadTipoff(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public Place targetLocation() {
    return ((Element) subject).place();
  }
  
  
  protected void onSuccess() {
  }
  
  
  protected void onFailure() {
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String choiceInfo() {
    return "Anonymous Tip";
  }
  
  
  public String helpInfo() {
    return
      "Street contacts claim that "+subject+" has been recruited for an "+
      "upcoming criminal operation.";
  }
  
  
  public String activeInfo() {
    //  NOTE:  Tipoffs are handed off in complete form, so there's no need to
    //  pursue them actively...
    return "Tipoff on "+subject;
  }
  
  
  public Image icon() {
    return ((Element) subject).kind.sprite();
  }
}



