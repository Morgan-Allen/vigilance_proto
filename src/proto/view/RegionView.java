

package proto.view;
import proto.game.scene.Investigation;
import proto.game.scene.Lead;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RegionView {
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  
  RegionView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
  }
  

  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    Nation nation = parent.mapView.selectedNation;
    if (nation == null) return;
    
    final int
      vx = (int) viewBounds.xpos(),
      vy = (int) viewBounds.ypos(),
      vw = (int) viewBounds.xdim(),
      vh = (int) viewBounds.ydim()
    ;
    Image portrait = nation.region.view.portrait;
    
    g.setColor(Color.WHITE);
    g.drawString(nation.region.name, vx + 20, vy + 20);
    
    int portW = vw - 40, portH = (int) (portW / 2.5f);
    g.drawImage(portrait, vx + 20, vy + 40, portW, portH, null);
    
    int trustPercent = (int) (nation.trustLevel() * 100);
    int crimePercent = (int) (nation.crimeLevel() * 100);
    
    g.drawString("Trust: "+trustPercent+"%", vx + 30, vy + 50);
    g.drawString("Crime: "+crimePercent+"%", vx + 30, vy + 65);
    g.drawString("Wealth: "+nation.funding(), vx + 30, vy + 80);
    
    
    g.setColor(Color.LIGHT_GRAY);
    int leadID = 0, down = vy + portH + 50;
    for (Investigation event : parent.world.events().active()) {
      for (Lead l : event.leadsFrom(nation.region)) if (l.canFollow()) {
        Image leadImg = event.imageFor(l);
        g.drawImage(leadImg, vx + 20, down, 80, 80, null);
        g.drawString(l.name(), vx + 20, down + 90);
        
        if (surface.mouseIn(vx + 20, down, 80, 80)) {
          g.drawImage(parent.selectCircle, vx + 20, down, 80, 80, null);
        }
        
        leadID++;
        down += 80 + 10;
      }
    }
  }
  
}












