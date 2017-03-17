

package proto.view.base;
import proto.common.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class MissionsViewCasesView extends UINode {
  
  
  final MissionsView parent;
  
  
  public MissionsViewCasesView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Base player = mainView.player();
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "OPEN CASES", g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    for (final Plot plot : player.leads.knownPlots()) {
      Image icon = plot.icon();
      if (icon == null) icon = MissionsView.ALERT_IMAGE;
      String desc = ""+plot;
      
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, plot)) {
        g.setColor(Color.GRAY);
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          parent.setActiveFocus(plot, true);
        }
      }
      
      down += 40 + 5;
    }
    
    return true;
  }
  
}





