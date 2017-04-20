

package proto.view.base;
import proto.game.event.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Image;



public class CaseCluesView extends UINode {
  
  
  public CaseCluesView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    CasesView parent = mainView.casesView;
    Object focus = parent.priorFocus();
    boolean forPlot = false, forSuspect = false;
    //
    //  We need to assemble/present clues a little differently, depending on
    //  the source:
    String header = "";
    Series <Clue> clues = null;
    if (focus instanceof Plot) {
      Plot plot = (Plot) focus;
      header = "EVIDENCE FOR "+CaseFX.nameFor(plot, player);
      clues = player.leads.cluesFor(plot, true);
      forPlot = true;
    }
    if (focus instanceof Element) {
      Element suspect = (Element) focus;
      header = "EVIDENCE ON "+suspect.name();
      clues = player.leads.cluesFor(suspect, true);
      forSuspect = true;
    }
    if (clues == null) return false;
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, header, 25, null
    );
    for (Clue clue : clues) {
      draw.addEntry(null, CaseFX.longDescription(clue, player), 100, clue);
    }
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    
    if (draw.hovered != null) {
      Clue picked = (Clue) draw.hovered;
      if (draw.clicked) {
        parent.setActiveFocus(picked.plot(), false);
      }
    }
    
    parent.casesArea.setScrollheight(down);
    return true;
  }
  
}



