

package proto.view.world;
import java.awt.Color;
import java.awt.Graphics2D;

import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;



public class ProgressOptionsView extends UINode {
  
  
  final StringButton monitorButton, saveButton, loadButton, quitButton;
  
  
  public ProgressOptionsView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int across = 5, down = 0;
    
    monitorButton = new StringButton(
      "", new Box2D(across, down, viewBounds.xdim() - 10, 20), this
    ) {
      protected void whenClicked() {
        final World world = mainView.world();
        if (world.monitorActive()) world.pauseMonitoring();
        else                       world.beginMonitoring();
      }
      protected void updateAndRender(Surface surface, Graphics2D g) {
        final boolean active = mainView.world().monitorActive();
        this.label = active ? "Pause Monitoring" : "Resume Monitoring";
        super.updateAndRender(surface, g);
      }
    };
    addChildren(monitorButton);
    
    down = (int) viewBounds.ydim() - 20;
    
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









