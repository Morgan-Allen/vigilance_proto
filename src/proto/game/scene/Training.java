

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.I;
import proto.view.MessageView;
import proto.view.TaskView;
import proto.view.WorldView;

import static proto.game.person.PersonStats.*;

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
    StringBuffer s = new StringBuffer();
    
    //  TODO:  You need a more generalised method of recording ongoing events
    //  and presenting those to the user in a bulletin.
    
    //  TODO:  Rates of XP and relations-gain need to be balanced.
    
    for (Person p : assigned()) {
      
      int oldL = p.stats.levelFor(trained);
      p.stats.gainXP(trained, 1);
      int newL = p.stats.levelFor(trained);
      
      for (Person o : assigned()) if (o != p) {
        p.bonds.incBond(o, 1f / 100);
      }
      
      if (oldL != newL) {
        s.append("\n"+p+" went to level "+newL);
      }
    }
    
    if (s.length() > 0) {
      final String record = s.toString();
      s = new StringBuffer();
      
      for (Person p : assigned) {
        s.append(p.name());
        if (p != assigned.last()) s.append(" and ");
      }
      s.append(" trained their "+trained);
      s.append(record);
      
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
    return room.icon();
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
  }
}










