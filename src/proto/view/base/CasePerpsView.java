

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class CasePerpsView extends UINode {
  
  
  public CasePerpsView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    CasesView parent = mainView.casesView;
    Object focus = parent.activeFocus;
    if (focus instanceof Role) {
      return renderSuspects((Role) focus, surface, g);
    }
    else if (parent.activeFocus instanceof Element) {
      return renderSuspect((Element) focus, surface, g);
    }
    return true;
  }
  
  
  boolean renderSuspects(Role role, Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    CasesView parent = mainView.casesView;
    Object focus = parent.priorFocus();
    if (! (focus instanceof Plot)) return false;
    Plot plot = (Plot) focus;
    //
    //  Create a list-display, and render the header plus entries for each
    //  suspect:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "SUSPECTS FOR "+role+" IN "+CaseFX.nameFor(plot, player), 40, null
    );
    for (Element e : player.leads.suspectsFor(role, plot)) {
      draw.addEntry(e.icon(), e.name(), 40, e);
    }
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  If one is selected, zoom to that element:
    if (draw.clicked) {
      parent.setActiveFocus(draw.hovered, false);
    }
    
    parent.casesArea.setScrollheight(down);
    return true;
  }
  
  
  boolean renderSuspect(Element suspect, Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base      player   = mainView.player();
    CasesView parent   = mainView.casesView;
    Person    agent    = mainView.rosterView.selectedPerson();
    Place     lastSeen = player.leads.lastKnownLocation(suspect);
    boolean   atSeen   = player.leads.atKnownLocation(suspect);
    Series <Clue> clues = player.leads.cluesFor(suspect, true);
    //
    //  Create a list-display, and render the header, latest clue, entries for
    //  each possible lead, and an option to view associates-
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      suspect.icon(), suspect.name(), 40, null
    );
    if (clues.empty()) {
      draw.addEntry(null, "No current leads on this suspect.", 100, "");
    }
    else {
      Clue first = clues.first();
      String desc = CaseFX.longDescription(first, player);
      draw.addEntry(null, desc, 100, first.plot());
    }
    
    String descAt = null;
    if (atSeen && lastSeen != null && lastSeen != suspect) {
      descAt = "Currently At: "+lastSeen;
    }
    else if (! atSeen) {
      descAt = "Whereabouts unknown.";
      if (lastSeen != null) descAt += " Last Seen: "+lastSeen;
    }
    if (descAt != null) {
      draw.addEntry(CasesView.MYSTERY_IMAGE, descAt, 40, lastSeen);
    }
    
    for (Lead lead : player.leads.leadsFor(suspect)) {
      draw.addEntry(lead.icon(), lead.choiceInfo(agent), 20, lead);
    }
    draw.addEntry(
      CasesView.MYSTERY_IMAGE, "View Associates", 20,
      CasesView.PERP_LINKS
    );
    draw.addEntry(
      CasesView.FILE_IMAGE, "View All Evidence", 20,
      CasesView.PLOT_CLUES
    );
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  Then given suitable tool-tips and click-reponses for any encountered
    //  list-elements:
    String hoverDesc = "";
    if (draw.hovered instanceof Lead) {
      Lead lead = (Lead) draw.hovered;
      hoverDesc = lead.testInfo(agent);
      if (draw.clicked) {
        if (agent.assignments().includes(lead)) agent.removeAssignment(lead);
        else agent.addAssignment(lead);
      }
    }
    else if (draw.hovered instanceof Plot) {
      Plot plot = (Plot) draw.hovered;
      hoverDesc = "Click to see more information on this plot.";
      if (draw.clicked) {
        parent.setActiveFocus(plot, false);
      }
    }
    else if (draw.hovered == CasesView.PERP_LINKS) {
      hoverDesc = "View persons and places associated with this suspect.";
      if (draw.clicked) {
        parent.setActiveFocus(CasesView.PERP_LINKS, false);
      }
    }
    else if (draw.hovered == CasesView.PLOT_CLUES) {
      hoverDesc = "Review all evidence assembled on this suspect.";
      if (draw.clicked) {
        parent.setActiveFocus(CasesView.PLOT_CLUES, false);
      }
    }
    else if (draw.hovered instanceof Place) {
      hoverDesc = "View this location.";
      if (draw.clicked) {
        parent.setActiveFocus(draw.hovered, false);
      }
    }
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, vw - (across + 10), 150
    );
    down += 150;
    
    parent.casesArea.setScrollheight(down);
    return true;
  }
  
}











