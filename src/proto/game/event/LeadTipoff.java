

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
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
  

  protected float evidenceLevel() {
    return CaseFile.LEVEL_TIPOFF;
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
    return "An anonymous tip";
  }
  
  
  public Image icon() {
    return ((Element) subject).kind.sprite();
  }
}



