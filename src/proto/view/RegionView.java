

package proto.view;
import proto.game.scene.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RegionView {
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  Lead selectedLead;
  
  
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
    int leadID = 0, personID, down = vy + portH + 50, across;
    
    for (Event event : parent.world.events().active()) {
      
      final Series <Lead> leads = event.openLeadsFrom(nation.region);
      if (leads.empty()) continue;
      //
      //  First, draw the name and info for the investigation as a whole:
      int initDown = down, LIS = 60;  // Lead image size...
      
      g.drawString(event.name(), vx + 20, down + 15);
      down += 20;
      ViewUtils.drawWrappedString(
        event.info(), g, vx + 20, down + 15, vw - 40, 60
      );
      down += 60;
      //
      //  Then, draw info for any individual leads:
      for (Lead l : leads) {
        //
        //  Draw the icon and description for this particular lead-
        Image leadImg = event.imageFor(l);
        g.drawImage(leadImg, vx + 20, down, LIS, LIS, null);
        
        ViewUtils.drawWrappedString(
          l.info(), g, vx + 20 + 60 +5, down + 15, vw - 85, 45
        );
        g.drawString(l.testInfo(), vx + 20 + 60 +5, down + 45 + 15);
        //
        //  Draw the highlight/selection rectangle, and toggle selection if
        //  clicked-
        final boolean hovered = surface.mouseIn(
          vx + 20 -5, down - 5, vw + 10 - 40, 60 + 10
        );
        if (selectedLead == l || hovered) {
          if (l != selectedLead) g.setColor(Color.GRAY);
          g.drawRect(vx + 20 -5, down - 5, vw + 10 - 40, 60 + 10);
        }
        if (surface.mouseClicked && hovered) {
          boolean selected = parent.lastSelected == l;
          selectedLead = selected ? null : l;
          parent.setSelection(selectedLead);
        }
        //
        //  Finally, draw any persons assigned to this lead...
        personID = 0;
        across = vx + 250;
        for (Person p : l.assigned()) {
          g.drawImage(p.kind().sprite(), across, down + 45, 20, 20, null);
          across += 25;
        }
        //
        //  ...and move down for the next lead.
        leadID++;
        down += 60 + 10;
      }
      
      g.setColor(Color.GRAY);
      g.drawRect(vx + 10, initDown, vw - 20, down - initDown);
    }
  }
  
}












