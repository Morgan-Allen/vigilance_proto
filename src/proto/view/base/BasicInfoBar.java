

package proto.view.base;
import proto.common.*;
import proto.game.world.Base;
import proto.game.world.World;
import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class BasicInfoBar extends UINode {
  
  
  final StringButton monitorButton, saveButton, loadButton, quitButton;
  
  
  public BasicInfoBar(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int fullWide = (int) (viewBounds.xdim());
    int across = (fullWide / 2) - 100, down = 0;
    
    monitorButton = new StringButton(
      "", new Box2D(across, down, 200, 25), this
    ) {
      protected void whenClicked() {
        mainView.setMonitoring(mainView.game().paused());
      }
      protected void updateAndRender(Surface surface, Graphics2D g) {
        final boolean paused = mainView.game().paused();
        this.label = paused ? "Resume Monitoring" : "Pause Monitoring";
        super.updateAndRender(surface, g);
      }
    };
    addChildren(monitorButton);
    
    across = across + 250;
    
    saveButton = new StringButton(
      "Save", new Box2D(across + 0, down, 50, 25), this
    ) {
      protected void whenClicked() {
        mainView.world().performSave();
      }
    };
    loadButton = new StringButton(
      "Reload", new Box2D(across + 50, down, 50, 25), this
    ) {
      protected void whenClicked() {
        mainView.world().reloadFromSave();
      }
    };
    quitButton = new StringButton(
      "Quit", new Box2D(across + 100, down, 50, 25), this
    ) {
      protected void whenClicked() {
        mainView.world().performSaveAndQuit();
      }
    };
    addChildren(saveButton, loadButton, quitButton);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    final World world = mainView.world();
    final Base  base  = world.playerBase();
    
    int across = 5, down = 20;
    
    g.setColor(Color.WHITE);
    String timeString = "Time: "+world.timing.currentTimeString();
    g.drawString(timeString, vx + across, vy + down);
    
    String cashString = "";
    cashString += "Public/Private Funds: ";
    cashString += base.finance.publicFunds()+"/";
    cashString += base.finance.secretFunds()+"";
    g.drawString(cashString, vx + across + 250, vy + down);
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    return true;
  }
  
}









