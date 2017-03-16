

package proto.view.base;
import proto.game.event.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class RolesView extends UINode {
  
  
  final MapInsetView mapRefers;
  
  
  public RolesView(UINode parent, MapInsetView map, Box2D viewBounds) {
    super(parent, viewBounds);
    this.mapRefers = map;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    MissionsView MV = (MissionsView) this.parent;
    Base player = mainView.player();
    Plot plot = (Plot) MV.activeFocus;
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "CASE FILE: "+plot, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    //  TODO:  Also iterate across all known roles, even if the perp hasn't
    //  been identified yet.
    
    for (Element e : player.leads.knownInvolvedIn(plot)) {
      g.setColor(Color.LIGHT_GRAY);
      Image icon = e.icon();
      String desc = ""+e.name();
      
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, e)) {
        g.setColor(Color.GRAY);
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          MV.setActiveFocus(e, true);
        }
      }
      
      down += 40;
    }
    return true;
  }
  
}



