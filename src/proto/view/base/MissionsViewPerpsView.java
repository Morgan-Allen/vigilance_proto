

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class MissionsViewPerpsView extends UINode {
  
  
  public MissionsViewPerpsView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    MissionsView parent = mainView.missionView;
    if (parent.activeFocus instanceof Plot.Role) {
      renderSuspects((Plot.Role) parent.activeFocus, surface, g);
    }
    if (parent.activeFocus instanceof Element) {
      renderSuspect((Element) parent.activeFocus, surface, g);
    }
    return true;
  }
  
  
  void renderSuspects(Plot.Role role, Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    MissionsView parent = mainView.missionView;
    Plot plot = (Plot) parent.focusOfType(Plot.class);
    //
    //  Create a list-display, and render the header plus entries for each
    //  suspect:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "SUSPECTS FOR "+role+" IN "+plot.nameForCase(player), 40, null
    );
    for (Element e : player.leads.suspectsFor(role, plot)) {
      draw.addEntry(e.icon(), e.name(), 40, e);
    }
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  If one is selected, zoom to that element:
    if (draw.clicked) {
      parent.setActiveFocus(draw.hovered, true);
    }
    
    parent.casesArea.setScrollheight(down);
  }
  
  
  void renderSuspect(Element suspect, Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    MissionsView parent = mainView.missionView;
    Person agent = mainView.rosterView.selectedPerson();
    Series <Clue> clues = player.leads.cluesFor(suspect, true);
    //
    //  Create a list-display, and render the header, latest clue, entries for
    //  each possible lead, and an option to view associates-
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      suspect.icon(), "CASE FILE FOR: "+suspect, 40, null
    );
    if (! clues.empty()) {
      draw.addEntry(null, clues.first().longDescription(player), 100, null);
    }
    for (Lead lead : player.leads.leadsFor(suspect)) {
      draw.addEntry(lead.icon(), lead.choiceInfo(agent), 40, lead);
    }
    if (suspect.isRegion()) {
      Region area = (Region) suspect;
      boolean noCrimes = true;
      draw.addEntry(null, "Ongoing Crimes: ", 25, null);
      for (Plot plot : player.leads.knownPlotsForRegion(area)) {
        draw.addEntry(plot.icon(), plot.nameForCase(player), 40, plot);
        noCrimes = false;
      }
      if (noCrimes) {
        draw.addEntry(null, "  No known leads", 25, null);
      }
    }
    else draw.addEntry(
      MissionsView.MYSTERY_IMAGE, "View Associates", 40,
      MissionsView.PERP_LINKS
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
      Series <Clue> forPlot = player.leads.cluesFor(plot, true);
      Clue top = forPlot.first();
      if (top != null) {
        hoverDesc = "Latest Evidence:\n  "+top.longDescription(player);
      }
      else {
        hoverDesc = "No Evidence";
      }
      if (draw.clicked) {
        parent.setActiveFocus(plot, true);
      }
    }
    else if (draw.hovered == MissionsView.PERP_LINKS) {
      hoverDesc = "View persons and places associated with this suspect.";
      if (draw.clicked) {
        parent.setActiveFocus(MissionsView.PERP_LINKS, true);
      }
    }
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, vw - (across + 10), 200
    );
    down += 200;
    
    parent.casesArea.setScrollheight(down);
  }
  
  
}











