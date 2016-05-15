

package proto.game.scene;
import java.awt.Image;

import proto.common.Session;



public class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Investigation parent;
  final public int ID;
  final public Object origin, reveals;
  boolean followed;
  
  
  public Lead(
    String name, String info,
    Investigation parent, int ID, Object origin, Object reveals,
    Object... args
  ) {
    super(name, info, args);
    this.parent  = parent ;
    this.ID      = ID     ;
    this.origin  = origin ;
    this.reveals = reveals;
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    parent   = (Investigation) s.loadObject();
    ID       = s.loadInt   ();
    origin   = s.loadObject();
    reveals  = s.loadObject();
    followed = s.loadBool  ();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(parent  );
    s.saveInt   (ID      );
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
  
  
  public boolean followed() {
    return followed;
  }
  
  
  public boolean canFollow() {
    return parent.known.includes(origin);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return parent.imageFor(this);
  }
  
}


