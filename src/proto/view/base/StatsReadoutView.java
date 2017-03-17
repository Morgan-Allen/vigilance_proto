
package proto.view.base;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;




public class StatsReadoutView extends UINode {
  
  
  
  public StatsReadoutView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    final World world = mainView.world();
    final Base  base  = world.playerBase();
    
    g.setColor(Color.WHITE);
    String timeString = "Time: "+world.timing.currentTimeString();
    g.drawString(timeString, vx + 10, vy + 25);
    
    String cashString = "";
    cashString += "Public/Private Funds: ";
    cashString += base.finance.publicFunds()+"/";
    cashString += base.finance.secretFunds()+"";
    g.drawString(cashString, vx + 260, vy + 25);
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    
    return true;
  }
}







