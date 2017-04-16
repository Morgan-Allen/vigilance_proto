

package proto.view.base;
import proto.common.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;



public class ProgressOptionsView extends UINode {
  
  
  final StringButton monitorButton, saveButton, loadButton, quitButton;
  
  
  public ProgressOptionsView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int across = 0, down = 0;
    
    monitorButton = new StringButton(
      "", new Box2D(across + 5, down, 200 - 10, 20), this
    ) {
      protected void whenClicked() {
        RunGame game = mainView.game();
        if (game.paused()) game.setPaused(false);
        else               game.setPaused(true );
      }
      protected void updateAndRender(Surface surface, Graphics2D g) {
        final boolean paused = mainView.game().paused();
        this.label = paused ? "Resume Monitoring" : "Pause Monitoring";
        super.updateAndRender(surface, g);
      }
    };
    addChildren(monitorButton);
    
    down   += 25;
    across += 25;
    
    saveButton = new StringButton(
      "Save", new Box2D(across + 0, down, 50, 20), this
    ) {
      protected void whenClicked() {
        mainView.world().performSave();
      }
    };
    loadButton = new StringButton(
      "Reload", new Box2D(across + 50, down, 50, 20), this
    ) {
      protected void whenClicked() {
        mainView.world().reloadFromSave();
      }
    };
    quitButton = new StringButton(
      "Quit", new Box2D(across + 100, down, 50, 20), this
    ) {
      protected void whenClicked() {
        mainView.world().performSaveAndQuit();
      }
    };
    addChildren(saveButton, loadButton, quitButton);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    return true;
  }
  
}









