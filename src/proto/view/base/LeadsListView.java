

package proto.view.base;
import proto.game.event.*;
import proto.game.world.*;
import proto.util.Box2D;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



//  TODO:  This needs to allow scrolling over longer lists.


public class LeadsListView extends UINode {
  
  
  final MissionsView parent;
  
  
  public LeadsListView(MissionsView parent, Box2D viewBounds) {
    super(parent, viewBounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    g.setColor(Color.LIGHT_GRAY);
    
    int across = 10, down = 10;
    boolean noEvents = true;
    Base played = mainView.world().playerBase();
    Region region = parent.mapView.selectedRegion();
    
    if (region == null) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select a district to view information on ongoing crimes and "+
        "possible leads.",
        g, vx + 25, vy + down + 20, vw - 30, 150
      );
      return true;
    }
    
    g.setColor(Color.WHITE);
    g.drawString(region.kind().name(), vx + across, vy + down);
    down += 20;
    
    /*
    Lead hovered = null;
    
    for (CaseFile file : played.leads.casesForRegion(region)) {
      //
      //  Firstly, draw an illustrative icon for the lead we've picked up and
      //  some basic info on how it was acquired.
      Image icon = file.icon();
      g.drawImage(icon, vx + 15, vy + down + 5, 40, 40, null);
      int initDown = down;
      
      g.setColor(Color.LIGHT_GRAY);
      StringBuffer desc = new StringBuffer();
      file.shortDescription(desc);
      
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + 60, vy + down, vw - 80, 60
      );
      down += 60;
      //
      //  Then, render the options for pursuing the investigation further:
      for (Lead option : file.investigationOptions()) {
        TaskView view = option.createView(mainView);
        view.showIcon = false;
        view.relBounds.set(vx, vy + down, vw, 20);
        view.renderNow(surface, g);
        down += view.relBounds.ydim() + 10;
        noEvents = false;
        if (surface.wasHovered(option)) hovered = option;
      }
      //
      //  Finally, box it all in with a nice border-
      g.setColor(Color.DARK_GRAY);
      g.drawRect(vx + 10, vy + initDown, vw - 20, down - initDown);
    }
    
    if (hovered != null) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        hovered.testInfo(),
        g, vx + 25, vy + down + 20, vw - 30, 150
      );
    }
    else if (noEvents) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "You have no active leads on criminal activity in this district.",
        g, vx + 25, vy + down + 20, vw - 30, 150
      );
    }
    else if (true) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select agents from the roster, then click on a lead to assign them "+
        "to the task.  Click again to unassign.",
        g, vx + 25, vy + down + 20, vw - 30, 150
      );
    }
    //*/
    
    return true;
  }
}


