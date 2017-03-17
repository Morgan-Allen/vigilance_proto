

package proto.view.base;
import proto.game.event.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class MissionsViewRolesView extends UINode {
  
  
  final MissionsView parent;
  
  
  public MissionsViewRolesView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    Base player = mainView.player();
    Plot plot = (Plot) parent.activeFocus;
    Plot.Role hovered = null;
    boolean canFollowHovered = false;
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "CASE FILE: "+plot, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    class RoleView {
      Plot.Role role;
      Series <Clue> clues;
      Series <Element> suspects;
    }
    
    List <RoleView> roles = new List <RoleView> () {
      protected float queuePriority(RoleView r) {
        if (r.suspects.size() == 1) return 1;
        return 0;
      }
    };
    for (Plot.Role role : player.leads.knownRolesFor(plot)) {
      RoleView r = new RoleView();
      r.role = role;
      r.suspects = player.leads.suspectsFor(role, plot);
      r.clues = player.leads.cluesFor(plot, null, role, false);
      roles.queueAdd(r);
    }
    
    for (RoleView r : roles) {
      Plot.Role role = r.role;
      Image icon = null;
      String desc = null;
      Element match = r.suspects.size() == 1 ? r.suspects.first() : null;
      boolean canFollow = r.suspects.size() <= 4 && r.clues.size() > 0;
      
      if (match != null) {
        icon = match.icon();
        desc = role+": "+match.name();
      }
      else {
        icon = MissionsView.MYSTERY_IMAGE;
        desc = role+": Unknown";
        
        if (r.clues.size() > 0) {
          desc += "\n  ";
          for (Clue c : r.clues) desc += c.traitDescription()+" ";
          if (! canFollow) desc += "\n  (numerous suspects)";
          else desc += "\n  ("+r.suspects.size()+" suspects)";
        }
        else {
          desc += "\n  (no evidence)";
        }
      }
      
      int entryHigh = 40;
      g.setColor(Color.LIGHT_GRAY);
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, entryHigh
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, entryHigh, role)) {
        g.drawRect(vx + across, vy + down, vw - 20, entryHigh);
        hovered = role;
        canFollowHovered = canFollow;
        
        if (surface.mouseClicked() && canFollow) {
          if (match != null) parent.setActiveFocus(match, true);
          else               parent.setActiveFocus(role , true);
        }
      }
      
      down += entryHigh + 5;
    }
    
    Image icon = MissionsView.FILE_IMAGE;
    String desc = "View All Evidence";
    boolean hoveredEvidence = false;
    g.setColor(Color.LIGHT_GRAY);
    
    g.drawImage(icon, vx + across, vy + down, 40, 40, null);
    ViewUtils.drawWrappedString(
      desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
    );
    if (surface.tryHover(vx + across, vy + down, vw - 20, 40, "associates")) {
      g.setColor(Color.GRAY);
      g.drawRect(vx + across, vy + down, vw - 20, 40);
      hoveredEvidence = true;
      if (surface.mouseClicked()) {
        parent.setActiveFocus(MissionsView.PLOT_CLUES, true);
      }
    }
    
    down += 45;
    
    String hoverDesc = "";
    if (hoveredEvidence) {
      hoverDesc = "Review all evidence assembled on this case.";
    }
    else if (hovered == null) {
      hoverDesc = "Click on a role to see more information on suspects.";
    }
    else if (canFollowHovered) {
      Series <Clue> clues = player.leads.cluesFor(plot, null, hovered, true);
      hoverDesc = "Latest Evidence:\n  "+clues.first();
    }
    else {
      hoverDesc = "You will need to gather evidence from other suspects "+
      "to identify this party.";
    }
    
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, vw - (across + 10), 100
    );
    down += 100;
    
    return true;
  }
}







