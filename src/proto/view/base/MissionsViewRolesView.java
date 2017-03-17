

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
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "CASE FILE: "+plot, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    for (Plot.Role role : player.leads.knownRolesFor(plot)) {
      Series <Clue> clues = player.leads.cluesFor(plot, null, role, false);
      Series <Element> suspects = player.leads.suspectsFor(role, plot);
      Image icon = null;
      String desc = null;
      Element match = suspects.size() == 1 ? suspects.first() : null;
      boolean canFollow = suspects.size() <= 4 && clues.size() > 0;
      
      if (match != null) {
        icon = match.icon();
        desc = role+": "+match.name();
      }
      else {
        icon = MissionsView.MYSTERY_IMAGE;
        desc = role+": Unknown";
        
        if (clues.size() > 0) {
          desc += "\n  ";
          for (Clue c : clues) desc += c.traitDescription()+" ";
          if (! canFollow) desc += "\n  (numerous suspects)";
          else desc += "\n  ("+suspects.size()+" suspects)";
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
        
        if (surface.mouseClicked() && canFollow) {
          if (match != null) parent.setActiveFocus(match, true);
          else               parent.setActiveFocus(role , true);
        }
      }
      
      down += entryHigh + 5;
    }
    
    Series <Clue> clues = player.leads.cluesFor(plot, null, null, true);
    if (! clues.empty()) {
      String desc = "\nLatest Evidence:\n  "+clues.first();
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across, vy + down, vw - (across + 10), 100
      );
    }
    
    return true;
  }
}


