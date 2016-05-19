

package proto.game.scene;
import java.awt.Image;
import proto.common.Session;



public class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Event parent;
  final public int ID;
  final public Object origin, reveals;
  
  
  public Lead(
    String name, String info,
    Event parent, int ID, Object origin, Object reveals,
    int timeHours,
    Object... args
  ) {
    super(name, info, timeHours, args);
    this.parent  = parent ;
    this.ID      = ID     ;
    this.origin  = origin ;
    this.reveals = reveals;
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    parent   = (Event) s.loadObject();
    ID       = s.loadInt   ();
    origin   = s.loadObject();
    reveals  = s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(parent  );
    s.saveInt   (ID      );
    s.saveObject(origin  );
    s.saveObject(reveals );
  }
  
  
  
  /**  Follow-up and execution-
    */
  protected void onSuccess() {
    if (! parent.checkFollowed(this, true)) return;
  }
  
  
  protected void onFailure() {
    if (! parent.checkFollowed(this, false)) return;
  }
  
  
  public boolean open() {
    return parent.known.includes(origin) && ! complete();
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return parent.imageFor(this);
  }
  
}


