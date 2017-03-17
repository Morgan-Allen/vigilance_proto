

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
    
    for (Plot.Role role : player.leads.knownRolesFor(plot)) {
      Series <Element> suspects = player.leads.suspectsFor(role, plot);
      Image icon = null;
      String desc = null;
      Element match = suspects.size() == 1 ? suspects.first() : null;
      
      if (match != null) {
        icon = match.icon();
        desc = role+": "+match.name();
      }
      else {
        icon = MissionsView.MYSTERY_IMAGE;
        desc = role+": Unknown";
      }

      g.setColor(Color.LIGHT_GRAY);
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, role)) {
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          if (match != null) MV.setActiveFocus(match, true);
          else               MV.setActiveFocus(role , true);
        }
      }
      
      down += 40;
    }
    return true;
  }
}





