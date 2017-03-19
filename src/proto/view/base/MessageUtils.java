

package proto.view.base;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.view.common.*;
import proto.util.*;



public class MessageUtils {
  
  
  public static void presentClueMessage(
    Clue clue, MainView view, EventReport report
  ) {
    StringBuffer header = new StringBuffer(), desc = new StringBuffer();
    
    if (clue.isReport()) {
      header.append("CRIME REPORT: "+clue.plot.name());
      desc.append(clue.longDescription());
    }
    else if (clue.isTipoff()) {
      header.append("TIPOFF");
      desc.append(clue.longDescription());
    }
    else {
      header.append("EVIDENCE FOUND");
      desc.append(clue.longDescription());
    }
    if (report != null) {
      Region region = clue.place().region();
      float trust = report.trustEffect, deter = report.deterEffect;
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
  
}


