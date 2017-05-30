

package proto.view.base;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.view.common.*;
import proto.util.*;



public class MessageUtils {
  
  
  public static void presentClueMessage(
    MainView view, Clue clue, EventEffects effects
  ) {
    Base player = view.player();
    StringBuffer header = new StringBuffer(), desc = new StringBuffer();
    
    if (clue.isReport()) {
      header.append("CRIME REPORT: "+clue.plot().name());
      desc.append(CasesFX.longDescription(clue, player));
    }
    else if (clue.isTipoff()) {
      header.append("TIPOFF");
      desc.append(CasesFX.longDescription(clue, player));
    }
    else {
      header.append("EVIDENCE FOUND");
      desc.append(CasesFX.longDescription(clue, player));
    }
    if (effects != null) {
      Region region = effects.scene.region();
      float trust = effects.trustEffect, deter = effects.deterEffect;
      desc.append("\n"+region+" Trust "     +I.signNum((int) trust)+"%");
      desc.append("\n"+region+" Deterrence "+I.signNum((int) deter)+"%");
    }
    
    view.queueMessage(new MessageView(
      view, clue.icon(), header.toString(), desc.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  public static void presentColdTrailMessage(
    MainView view, Lead lead
  ) {
    view.queueMessage(new MessageView(
      view, lead.icon(), "Target Lost",
      "The location of "+lead.focus+" is no longer known.",
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  public static void presentColdCaseMessage(
    MainView view, Plot cold, int plotState
  ) {
    String desc = "";
    if (plotState == Plot.STATE_SPOOKED) desc =
      "Word on the street is that "+cold.base().faction()+" have got wind of "+
      "your investigation into "+CasesFX.nameFor(cold, view.player())+" and "+
      "called off the operation.  The crime will not take place, but you "+
      "won't catch the perps either.";
    else if (plotState == Plot.STATE_SUCCESS) desc =
      "Word on the street is "+cold.base().faction()+" pulled off a major "+
      "heist, but the trail will be cold by now.";
    else desc =
      "Word on the street is "+cold.base().faction()+" were planning a major "+
      "heist, but the trail will be cold by now.";
    
    view.queueMessage(new MessageView(
      view, cold.icon(), "Plot Abandoned", desc, "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  public static void presentBustMessage(
    MainView view, Scene scene, Lead lead, Plot plot
  ) {
    StringBuffer desc = new StringBuffer();
    
    Series <Person> did = scene.playerTeam();
    for (Person p : did) {
      if (p == did.first()) desc.append(""+p);
      else if (p == did.last()) desc.append(" and "+p);
      else desc.append(", "+p);
    }
    desc.append(" interrupted ");
    desc.append(plot.toString());
    desc.append(".");
    
    view.queueMessage(new MessageView(
      view, null, "Busted: "+plot.type.name, desc.toString(),
      "Begin Mission"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  
  public static void presentTrialMessage(MainView view, Trial trial) {
    int time = trial.world().timing.totalHours();
    int days = (trial.timeBegins() - time) / World.HOURS_PER_DAY;
    
    StringBuffer s = new StringBuffer();
    s.append(I.list(trial.accused().toArray(Person.class)));
    s.append(" are scheduled for trial in "+days+" days time.");
    
    view.queueMessage(new MessageView(
      view, MapView.TRIAL_IMAGE, "Trial Scheduled", s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  public static void presentSentenceMessage(
    MainView view, Trial concluded
  ) {
    StringBuffer s = new StringBuffer();
    s.append(concluded+" has concluded.");
    
    World world = concluded.world();
    for (Person p : concluded.accused()) {
      int sentence = world.council.sentenceDuration(p);
      if (sentence <= 0) {
        s.append("\n  "+p+" was released");
      }
      else {
        s.append("\n  "+p+" was sentenced to "+sentence+" days imprisonment.");
      }
    }
    
    view.queueMessage(new MessageView(
      view, MapView.TRIAL_IMAGE, "Trial Concluded", s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  public static void presentReleaseMessage(
    MainView view, Person person
  ) {
    StringBuffer s = new StringBuffer();
    s.append(person+" has been released after serving their sentence.");
    
    view.queueMessage(new MessageView(
      view, MapView.TRIAL_IMAGE, "Trial Concluded", s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
}







