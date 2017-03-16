

package proto.view.base;
import proto.common.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class CasesView extends UINode {
  
  
  final static Image
    ALERT_IMAGE = Kind.loadImage("media assets/city map/alert_symbol.png");
  
  
  final MapInsetView mapRefers;
  
  
  public CasesView(UINode parent, MapInsetView map, Box2D viewBounds) {
    super(parent, viewBounds);
    this.mapRefers = map;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Base player = mainView.player();
    MissionsView MV = (MissionsView) this.parent;
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "OPEN CASES", g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    for (final Plot plot : player.leads.knownPlots()) {
      Image icon = plot.icon();
      if (icon == null) icon = ALERT_IMAGE;
      String desc = ""+plot;
      
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, plot)) {
        g.setColor(Color.GRAY);
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          MV.setActiveFocus(plot, true);
        }
      }
      
      across += 45;
    }
    
    return true;
  }
  
}





