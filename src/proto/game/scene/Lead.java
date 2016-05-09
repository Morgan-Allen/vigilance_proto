

package proto.game.scene;
import proto.common.Session;



public class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Investigation parent;
  final Object origin, reveals;
  boolean followed;
  
  
  Lead(
    String name, String info,
    Investigation parent, Object origin, Object reveals,
    Object... args
  ) {
    super(name, info, args);
    this.parent  = parent ;
    this.origin  = origin ;
    this.reveals = reveals;
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    parent   = (Investigation) s.loadObject();
    origin   = s.loadObject();
    reveals  = s.loadObject();
    followed = s.loadBool  ();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(parent  );
    s.saveObject(origin  );
    s.saveObject(reveals );
    s.saveBool  (followed);
  }
  
  
  
  /**  Follow-up and execution-
    */
  protected void onSuccess() {
    if (! parent.checkFollowed(this, true)) return;
    followed = true;
  }
  
  
  protected void onFailure() {
    if (! parent.checkFollowed(this, false)) return;
    followed = false;
  }
  
}


