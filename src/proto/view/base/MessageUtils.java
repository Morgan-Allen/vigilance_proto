

package proto.view.base;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.view.common.*;
import proto.util.*;



public class MessageUtils {
  
  
  public static void presentClueMessage(
    Clue clue, MainView view, EventEffects effects
  ) {
    Base player = view.player();
    StringBuffer header = new StringBuffer(), desc = new StringBuffer();
    
    if (clue.isReport()) {
      header.append("CRIME REPORT: "+clue.plot.name());
      desc.append(clue.longDescription(player));
    }
    else if (clue.isTipoff()) {
      header.append("TIPOFF");
      desc.append(clue.longDescription(player));
    }
    else {
      header.append("EVIDENCE FOUND");
      desc.append(clue.longDescription(player));
    }
    if (effects != null) {
      Region region = clue.place().region();
      float trust = effects.trustEffect, deter = effects.deterEffect;
      desc.append("\n"+region+" Trust "     +I.signNum((int) trust)+"%");
      desc.append("\n"+region+" Deterrence "+I.signNum((int) deter)+"%");
    }
    
    view.queueMessage(new MessageView(
      view, clue.icon(), header.toString(), desc.toString(), "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  public static void presentSentenceMessage() {
    
  }
  
  
  public static void presentReleaseMessage() {
    
  }
  
}









