

package proto.view.base;
import proto.game.world.*;
import proto.game.event.*;
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
      desc.append(CaseFX.longDescription(clue, player));
    }
    else if (clue.isTipoff()) {
      header.append("TIPOFF");
      desc.append(CaseFX.longDescription(clue, player));
    }
    else {
      header.append("EVIDENCE FOUND");
      desc.append(CaseFX.longDescription(clue, player));
    }
    if (effects != null) {
      Region region = clue.place().region();
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
  
  
  
  public static void presentTrialMessage(MainView view, Trial trial) {
    int time = trial.world().timing.totalHours();
    int days = (trial.timeBegins() - time) / World.HOURS_PER_DAY;
    
    StringBuffer s = new StringBuffer();
    s.append(I.list(trial.accused().toArray(Person.class)));
    s.append(" are scheduled for trial in "+days+" days time.");
    
    view.queueMessage(new MessageView(
      view, CasesView.TRIAL_IMAGE, "Trial Scheduled", s.toString(),
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
      view, CasesView.TRIAL_IMAGE, "Trial Concluded", s.toString(),
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
      view, CasesView.TRIAL_IMAGE, "Trial Concluded", s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
}







