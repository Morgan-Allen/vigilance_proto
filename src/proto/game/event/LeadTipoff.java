

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import java.awt.Image;



public class LeadTipoff extends Lead {
  
  
  
  public LeadTipoff(Base base, Element about) {
    super(base, Task.TIME_SHORT, null, about, new Object[0]);
  }
  
  
  public LeadTipoff(Base base, Event about) {
    super(base, Task.TIME_SHORT, null, about, new Object[0]);
  }
  
  
  public LeadTipoff(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public Place targetLocation() {
    if (subject instanceof Event) {
      return ((Event) subject).place();
    }
    else {
      return ((Element) subject).place();
    }
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



