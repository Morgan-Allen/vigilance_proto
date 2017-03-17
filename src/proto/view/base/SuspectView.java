

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;


public class SuspectView extends UINode {
  
  
  final MapInsetView mapRefers;
  
  
  public SuspectView(UINode parent, MapInsetView map, Box2D viewBounds) {
    super(parent, viewBounds);
    this.mapRefers = map;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    MissionsView MV = (MissionsView) this.parent;
    Base player = mainView.player();
    Person agent = mainView.rosterView.selectedPerson();
    Element suspect = (Element) MV.activeFocus;

    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "CASE FILE FOR: "+suspect, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    for (Lead lead : player.leads.leadsFor(suspect)) {
      g.setColor(Color.LIGHT_GRAY);
      Image icon = lead.icon();
      String desc = ""+lead.choiceInfo(agent);
      
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, lead)) {
        g.setColor(Color.GRAY);
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          if (agent.assignments().includes(lead)) agent.removeAssignment(lead);
          else agent.addAssignment(lead);
        }
      }
      
      ViewUtils.renderAssigned(
        lead.assigned(), vx + across + 300, vy + down + 20, surface, g
      );
      
      down += 40;
    }
    
    return true;
  }
}





