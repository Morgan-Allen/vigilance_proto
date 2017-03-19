

package proto.view.base;
import proto.game.event.*;
import proto.view.common.*;



public class MessageUtils {
  
  
  public static void presentMessageFor(Clue clue, MainView view) {
    view.queueMessage(new MessageView(
      view, clue.icon(), "New Evidence", clue.longDescription(), "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        mainView.dismissMessage(this);
      }
    });
  }
  
  
  
  
  
}