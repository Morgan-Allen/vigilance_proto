

package proto.view;
import proto.game.scene.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RegionView extends UINode {
  
  
  Lead selectedLead;
  
  
  RegionView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  

  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    Nation nation = mainView.areaView.selectedNation();
    if (nation == null) return;
    
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
    int down = vy + portH + 50;
    boolean canAssign = mainView.rosterView.selected() != null;
    boolean noEvents = true;
    
    for (Event event : mainView.world.events().active()) {
      final Series <Lead> leads = event.openLeadsFrom(nation.region);
      if (leads.empty()) continue;
      noEvents = false;
      int initDown = down;
      //
      //  First, draw the name and info for the investigation as a whole:
      g.drawString(event.name(), vx + 20, down + 15);
      down += 20;
      ViewUtils.drawWrappedString(
        event.info(), g, vx + 20, down, vw - 40, 60
      );
      down += 60;
      //
      //  Then, draw info for any individual leads:
      //  TODO:  Should be using attachment/detachment here?
      for (Lead l : leads) {
        TaskView view = l.createView(parent);
        view.relBounds.set(vx, vy + down, vw, 60);
        view.updateAndRender(surface, g);
        down += 60 + 10;
      }
      down += 20;
      g.setColor(Color.GRAY);
      g.drawRect(vx + 10, initDown, vw - 20, down - initDown);
    }
    
    if (noEvents) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "You have no intelligence on any major crimes in this area.",
        g, vx + 25, down + 20, vw - 30, 150
      );
    }
    else if (! canAssign) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select an agent from your roster to perform assignments.",
        g, vx + 25, down + 20, vw - 30, 150
      );
    }
  }
  
}












