

package proto.view.base;
import proto.common.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class MissionsViewCasesView extends UINode {
  
  
  public MissionsViewCasesView(UINode parent, Box2D bound) {
    super(parent, bound);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    MissionsView parent = mainView.missionView;
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "OPEN CASES", 40, null
    );
    for (Plot plot : player.leads.knownPlots()) {
      Image icon = plot.icon();
      if (icon == null) icon = MissionsView.ALERT_IMAGE;
      draw.addEntry(icon, plot.nameForCase(player), 40, plot);
    }
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  If one is selected, zoom to that element:
    if (draw.clicked) {
      parent.setActiveFocus(draw.hovered, true);
    }
    
    parent.casesArea.setScrollheight(down);
    return true;
  }
  
}



