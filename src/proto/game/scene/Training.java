

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.I;
import proto.view.MessageView;

import static proto.game.person.PersonStats.*;

import java.awt.Image;



public class Training extends Task {
  
  Room room;
  Stat trained;


  public Training(Stat trained, Room room) {
    super(
      "Training "+trained.name,
      "Train "+trained.name,
      TIME_MEDIUM, room.base.world(),
      trained, 0
    );
    this.room    = room   ;
    this.trained = trained;
  }
  
  
  public Training(Session s) throws Exception {
    super(s);
    room    = (Room) s.loadObject();
    trained = (Stat) s.loadObject();
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
    
    for (Person p : assigned()) {
      int oldL = p.stats.levelFor(trained);
      p.stats.gainXP(trained, 1);
      int newL = p.stats.levelFor(trained);
      if (oldL != newL) {
        s.append("\n"+p+" went to level "+newL);
      }
      
      I.say("Gained 1 XP: "+p+" in "+trained);
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
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return room.icon();
  }
  
  
  protected void presentMessage(final World world) {
  }
}








