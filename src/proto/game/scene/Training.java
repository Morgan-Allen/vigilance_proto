

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;

import java.awt.Image;



public class Training extends Task {
  
  Room room;
  Skill trained;


  public Training(Skill trained, Room room) {
    super(
      "Training "+trained.name,
      "Training "+trained.name,
      TIME_MEDIUM, room.base.world(),
      trained, 0
    );
    this.room    = room   ;
    this.trained = trained;
  }
  
  
  public Training(Session s) throws Exception {
    super(s);
    room    = (Room) s.loadObject();
    trained = (Skill) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(room   );
    s.saveObject(trained);
  }
  
  
  protected void onCompletion() {
    //  TODO:  Rates of XP and relations-gain need to be balanced.
    
    for (Person p : assigned()) {
      p.stats.gainXP(trained, 1);
      
      for (Person o : assigned()) if (o != p) {
        p.bonds.incBond(o, 1f / 100);
      }
    }
    
    presentMessage(world);
    resetTask();
  }
  
  
  protected void onFailure() {
  }
  
  
  protected void onSuccess() {
  }
  
  
  public Object targetLocation() {
    return room;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return trained.icon();
  }
  
  
  public String description() {
    return name()+" in "+room.name();
  }
  
  
  public TaskView createView(WorldView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    return view;
  }
  
  
  protected void presentMessage(final World world) {
    final Series <String> logs = world.events().extractLogInfo(this);
    if (logs.empty()) return;
    
    StringBuffer s = new StringBuffer();

    for (Person p : assigned) {
      s.append(p.name());
      if (p != assigned.last()) s.append(" and ");
    }
    s.append(" trained their "+trained);
    
    for (String info : logs) {
      s.append("\n");
      s.append(info);
    }

    world.view().queueMessage(new MessageView(
      world.view(),
      icon(), "Task complete: "+name,
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
}










