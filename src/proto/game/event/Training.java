

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;

import java.awt.Image;



public class Training extends Task {
  
  
  
  Room room;
  Skill trained;
  Skill talking;
  
  
  public Training(Skill trained, Skill talking, Room room) {
    super(
      TIME_MEDIUM, room.base.world(),
      trained, 0
    );
    this.room    = room   ;
    this.trained = trained;
    this.talking = talking;
  }
  
  
  public Training(Session s) throws Exception {
    super(s);
    room    = (Room ) s.loadObject();
    trained = (Skill) s.loadObject();
    talking = (Skill) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(room   );
    s.saveObject(trained);
    s.saveObject(talking);
  }
  
  
  protected void onCompletion() {
    //
    //  TODO:  Rates of XP and relations-gain need to be balanced.
    //  TODO:  Allow for the possibility of more efficient solo training
    //         under particular circumstances?
    
    final Skill chatWith = Rand.yes() ? talking : PersonStats.SUASION;
    float maxLevel = 0, numPeers = assigned.size() - 1;
    
    for (Person p : assigned) {
      maxLevel = Nums.max(maxLevel, p.stats.levelFor(trained));
    }
    
    for (Person p : assigned()) {
      float trainBonus = 0;
      //
      //  If you're training in a group, then you gain a bonus for whoever has
      //  the highest skill-rating, and for successfully performing a needed
      //  conversation skill.
      if (numPeers > 0) {
        float ownLevel = p.stats.levelFor(trained);
        if (performTest(chatWith, p, MEDIUM_DC)) trainBonus += 0.5f;
        trainBonus += (maxLevel + MEDIUM_DC) * 0.5f / (ownLevel + MEDIUM_DC);
      }
      //
      //  Otherwise you gain XP at a fixed rate-
      float gainedXP = 1;
      gainedXP *= (1 + trainBonus) / 10f;
      p.stats.gainXP(trained, gainedXP);
      //
      //  Then you boost any related relationships.
      for (Person o : assigned()) if (o != p) {
        p.history.incBond(o, (1f + trainBonus) / 100);
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
    //  TODO:  Get a better variety of these.
    return trained.icon();
  }
  
  
  public String choiceInfo() {
    return "Training "+trained;
  }
  
  
  public String activeInfo() {
    return "Training "+trained+" in "+room;
  }
  
  
  public String helpInfo() {
    return trained.description;
  }
  
  
  public String testInfo() {
    return "";
  }
  
  
  public TaskView createView(MainView parent) {
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
      icon(), "Task complete: "+activeInfo(),
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
}










