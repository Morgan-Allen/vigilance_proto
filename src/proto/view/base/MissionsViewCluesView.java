

package proto.view.base;
import proto.common.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class MissionsViewCluesView extends UINode {
  
  
  final MissionsView parent;
  
  
  public MissionsViewCluesView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Base player = mainView.player();
    Plot plot = (Plot) parent.focusOfType(Plot.class);
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "EVIDENCE FOR "+plot, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    for (Clue clue : player.leads.cluesFor(plot, null, null, true)) {
      Image icon = clue.icon();
      if (icon == null) icon = MissionsView.MYSTERY_IMAGE;
      String desc = ""+clue.longDescription();
      
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      down += 40 + 5;
    }
    
    return true;
  }
  
}



