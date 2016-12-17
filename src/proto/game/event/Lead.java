

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import proto.view.common.*;



public abstract class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Object subject;
  
  
  Lead(
    Base base, int timeHours, Object subject,
    Object... args
  ) {
    super(base, timeHours, args);
    this.subject = subject;
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    subject = s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(subject);
  }
  
  
  
  /**  Supplemental methods for override-
    */
  protected boolean matchType(Lead other) {
    return other.subject == subject && other.getClass() == getClass();
  }
  
  
  protected float evidenceLevel() {
    return CaseFile.LEVEL_EVIDENCE;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage() {
    //  TODO:  Move this out to the .view directory?
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
    
    final World world = base.world();
    for (String action : world.events.extractLogInfo(this)) {
      s.append("\n\n");
      s.append(action);
    }
    
    world.view().queueMessage(new MessageView(
      world.view(),
      icon(), "Task complete: "+choiceInfo(),
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
  
}











