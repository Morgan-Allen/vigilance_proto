

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;
import java.awt.Image;



public class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Event parent;
  final public int ID;
  final public Object origin, reveals;
  
  
  public Lead(
    String name, String info,
    Event parent, int ID, Object origin, Object reveals,
    int timeHours, Object... args
  ) {
    super(name, info, timeHours, parent.world, args);
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
  protected void presentMessage(final World world) {
    
    //  TODO:  Move this out to the View directory!
    StringBuffer s = new StringBuffer();
    
    for (Person p : assigned) {
      s.append(p.name());
      if (p != assigned.last()) s.append(" and ");
    }
    s.append(" tested their ");
    for (Trait t : tested) {
      s.append(t);
      if (t != Visit.last(tested)) s.append(" and ");
    }
    s.append(".");
    if (success()) s.append("  They were successful.");
    else           s.append("  They had no luck."    );
    
    if (success) for (Lead l : parent.openLeadsFrom(reveals)) {
      s.append("\nNew lead: ");
      s.append(l.info);
    }
    //  TODO:  If no fresh leads are uncovered, say as much.
    
    for (String action : parent.actionLog()) {
      s.append("\n");
      s.append(action);
    }
    parent.wipeActionLog();
    
    world.view().queueMessage(new MessageView(
      icon(), "Task complete: "+name,
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
  
  
  public Image icon() {
    return parent.imageFor(this);
  }
}





