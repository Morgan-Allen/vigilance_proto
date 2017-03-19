

package proto.view.base;
import proto.game.event.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Image;



public class MissionsViewCluesView extends UINode {
  
  
  final MissionsView parent;
  
  
  public MissionsViewCluesView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    Plot plot = (Plot) parent.focusOfType(Plot.class);
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "EVIDENCE FOR "+plot.nameForCase(player), 40, null
    );
    for (Clue clue : player.leads.cluesFor(plot, null, null, true)) {
      Image icon = clue.icon();
      if (icon == null) icon = MissionsView.MYSTERY_IMAGE;
      draw.addEntry(icon, clue.longDescription(), 40, null);
    }
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    
    return true;
  }
  
}



