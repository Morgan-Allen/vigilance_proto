

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import java.awt.Image;




public class LeadCrimeReport extends Lead {
  
  
  public LeadCrimeReport(Base base, Event about) {
    super(base, Task.TIME_SHORT, about, new Object[0]);
  }
  
  
  public LeadCrimeReport(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public Place targetLocation() {
    return ((Event) subject).targetLocation();
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
    return "News Report";
  }
  
  
  public String helpInfo() {
    return "Police have reported the following major crime: "+subject;
  }
  
  
  public String activeInfo() {
    //  NOTE:  Reports are handed off in complete form, so there's no need to
    //  pursue them actively...
    return "";
  }
  
  
  public Image icon() {
    return ((Event) subject).targetLocation().icon();
  }
}







