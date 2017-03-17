

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
  
  
  final MissionsView parent;
  
  
  public MissionsViewCasesView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Base player = mainView.player();
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
      draw.addEntry(icon, plot.name(), 40, plot);
    }
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  If one is selected, zoom to that element:
    if (draw.clicked) {
      parent.setActiveFocus(draw.hovered, true);
    }
    
    return true;
  }
  
}





