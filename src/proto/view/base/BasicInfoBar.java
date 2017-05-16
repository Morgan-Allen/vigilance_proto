

package proto.view.base;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class BasicInfoBar extends UINode {
  
  
  StringButton monitorButton;
  
  
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









