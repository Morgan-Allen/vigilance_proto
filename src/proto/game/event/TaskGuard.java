

package proto.game.event;
import java.awt.Image;

import proto.common.*;
import proto.game.world.*;



public class TaskGuard extends Task {
  
  
  Event event;
  
  
  
  public TaskGuard(Base base, Event event) {
    super(base, Task.TIME_SHORT, new Object[0]);
    this.event = event;
  }
  
  
  public TaskGuard(Session s) throws Exception {
    super(s);
    event = (Event) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(event);
  }
  
  
  public Place targetLocation() {
    return event.place();
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




