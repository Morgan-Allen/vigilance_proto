

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;


public class MissionsViewSuspectView extends UINode {
  
  
  final MissionsView parent;
  
  
  public MissionsViewSuspectView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
    this.clipContent = true;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (parent.activeFocus instanceof Plot.Role) {
      renderSuspects((Plot.Role) parent.activeFocus, surface, g);
    }
    if (parent.activeFocus instanceof Element) {
      renderSuspect((Element) parent.activeFocus, surface, g);
    }
    return true;
  }
  
  
  void renderSuspects(Plot.Role role, Surface surface, Graphics2D g) {
    Base player = mainView.player();
    Plot plot = parent.plotFocus();
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "SUSPECTS FOR "+role+" IN "+plot, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    //  TODO:  There is a lot of content to factor out here.
    
    for (Element e : player.leads.suspectsFor(role, plot)) {
      g.setColor(Color.LIGHT_GRAY);
      Image icon = e.icon();
      String desc = ""+e;

      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, e)) {
        g.setColor(Color.GRAY);
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          parent.setActiveFocus(e, true);
        }
      }
      
      down += 40;
    }
  }
  
  
  void renderSuspect(Element suspect, Surface surface, Graphics2D g) {
    Base player = mainView.player();
    Person agent = mainView.rosterView.selectedPerson();

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
  }
}





