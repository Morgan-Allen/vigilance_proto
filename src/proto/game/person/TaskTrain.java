

package proto.game.person;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;
import proto.view.common.MainView;
import proto.view.common.MessageView;
import proto.view.common.TaskView;

import java.awt.Image;



public class TaskTrain extends Task {
  
  
  final static int
    ABILITY_BASE_LEARN_TIME = World.HOURS_PER_DAY * World.DAYS_PER_WEEK
  ;
  
  Trait trained;
  Trait talking;
  
  
  public TaskTrain(Trait trained, Trait talking, Base base) {
    super(base, TIME_MEDIUM, trained, 0);
    this.trained = trained;
    this.talking = talking;
  }
  
  
  public TaskTrain(Session s) throws Exception {
    super(s);
    trained = (Trait) s.loadObject();
    talking = (Trait) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(trained);
    s.saveObject(talking);
  }
  
  
  public boolean allowsAssignment(Person p) {
    for (Trait root : trained.roots()) {
      if (p.stats.levelFor(root) <= 0) return false;
    }
    return true;
  }


  protected void onCompletion() {
    //
    //  TODO:  Rates of XP and relations-gain need to be balanced.
    //  TODO:  Allow for the possibility of more efficient solo training
    //         under particular circumstances?
    final Trait chatWith = Rand.yes() ? talking : PersonStats.PERSUADE;
    final Series <Person> active = active();
    float maxLevel = 0, numPeers = active.size() - 1;
    
    for (Person p : active) {
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
    
    presentMessage();
    resetTask();
  }
  
  
  protected void onFailure() {
  }
  
  
  protected void onSuccess() {
  }
  
  
  public Place targetLocation() {
    return base;
  }
  
  
  public Trait trained() {
    return trained;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_TRAINING;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    //  TODO:  Get a better variety of these.
    return trained.icon();
  }
  
  
  public String choiceInfo() {
    return "Learn "+trained;
  }
  
  
  public String activeInfo() {
    return "Learning technique: "+trained;
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
  
  
  protected void presentMessage() {
    final World world = base.world();
    final Series <String> logs = world.events.extractLogInfo(this);
    if (logs.empty()) return;
    
    StringBuffer s = new StringBuffer();
    
    final Series <Person> active = active();
    for (Person p : active) {
      s.append(p.name());
      if (p != active.last()) s.append(" and ");
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




