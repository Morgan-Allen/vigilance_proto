

package proto.game.person;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Image;



//  TODO:  Show progress in the task based on skill level, and redefine
//  completion-criteria.


public class TaskResearch extends Task {
  
  
  final Place room;
  final Tech developed;
  float progress = 0;
  
  
  public TaskResearch(Tech developed, Place room, Base base) {
    super(base, TIME_LONG);
    this.room      = room;
    this.developed = developed;
  }
  
  
  public TaskResearch(Session s) throws Exception {
    super(s);
    room      = (Place) s.loadObject();
    developed = (Tech ) s.loadObject();
    progress = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(room);
    s.saveObject(developed);
    s.saveFloat(progress);
  }
  
  
  
  /**  Task performance and completion-
    */
  protected void onFailure() {
  }
  
  
  protected void onSuccess() {
    base.addTech(developed);
  }
  
  
  public Element targetElement(Person p) {
    return room;
  }
  
  
  protected Attempt configAttempt(Series <Person> attempting) {
    Attempt attempt = new Attempt(this);
    attempt.setupFromArgsList(10, developed.researchArgs());
    attempt.setAssigned(attempting);
    return attempt;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return developed.icon();
  }
  
  
  public String choiceInfo(Person p) {
    String info = "Researching "+developed;
    return info;
  }
  
  
  public float taskDaysRemaining(Person p) {
    return developed.researchTime() * 1f / World.HOURS_PER_DAY;
  }
  
  
  public String activeInfo() {
    return "Researching "+developed+" in "+room;
  }
  
  
  public String helpInfo() {
    return developed.helpInfo();
  }
  
  
  public String testInfo(Person p) {
    String info = super.testInfo(p);
    return info;
  }
  
  
  protected void presentMessage() {
    /*
    final World world = base.world();
    final Series <String> logs = world.events.extractLogInfo(this);
    StringBuffer s = new StringBuffer();
    
    for (Person p : assigned) {
      s.append(p.name());
      if (p != assigned.last()) s.append(" and ");
    }
    s.append(" attempted to make "+made+".");
    
    if (success()) {
      s.append(" They were successful.");
    }
    else {
      s.append(" They encountered difficulties.");
    }
    
    for (String info : logs) {
      s.append("\n");
      s.append(info);
    }
    
    world.view().queueMessage(new MessageView(
      world.view(),
      icon(), "Task complete: "+activeInfo(),
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
    //*/
  }
  
}












