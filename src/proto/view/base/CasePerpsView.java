

package proto.view.base;
import proto.common.*;
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
    MapView parent = mainView.mapView;
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
    MapView parent = mainView.mapView;
    Object focus = parent.priorFocus();
    if (! (focus instanceof Plot)) return false;
    Plot plot = (Plot) focus;
    //
    //  Create a list-display, and render the header plus entries for each
    //  suspect:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "SUSPECTS FOR "+role+" IN "+CasesFX.nameFor(plot, player), 40, null
    );
    for (Element suspect : player.leads.suspectsFor(role, plot)) {
      draw.addEntry(suspect.icon(), labelFor(suspect), 40, suspect);
    }
    draw.performVerticalDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  If one is selected, zoom to that element:
    if (draw.clicked) {
      parent.setActiveFocus(draw.hovered, false);
    }
    parent.infoArea.setScrollheight(down);
    return true;
  }
  
  
  boolean renderSuspect(Element suspect, Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base    player   = mainView.player();
    MapView parent   = mainView.mapView;
    Person  agent    = mainView.rosterView.selectedPerson();
    String hoverDesc = "";
    //
    //  Create a list-display, and render the header, latest clue, entries for
    //  each possible lead, and an option to view associates-
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(suspect.icon(), labelFor(suspect), 25, null);
    
    String traitDesc = "";
    for (Trait t : suspect.traits()) {
      traitDesc += "("+t+") ";
    }
    draw.addEntry(null, traitDesc, 40, null);
    draw.performVerticalDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  Then render associates:
    draw.clearEntries();
    draw.addEntry(null, "Associates:", 20, null);
    draw.performVerticalDraw(across, down, this, surface, g);
    down = draw.down;
    draw.clearEntries();
    for (AssocResult r : getAssociates(suspect, player)) {
      if (r.associate != null) {
        draw.addEntry(r.associate.icon(), null, 40, r);
      }
      if (r.plot != null) {
        draw.addEntry(r.plot.icon(), null, 40, r);
      }
    }
    draw.performHorizontalDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  Then give suitable tool-tips and click-responses for associates:
    if (draw.hovered instanceof AssocResult) {
      AssocResult a = (AssocResult) draw.hovered;
      if (a.associate != null) {
        hoverDesc = a.associate.name()+" ("+a.label+")";
        hoverDesc += "\n\nClick to view this associate.";
        if (draw.clicked) {
          parent.setActiveFocus(a.associate, false);
        }
      }
      if (a.plot != null) {
        hoverDesc = a.label+" "+CasesFX.nameFor(a.plot, player);
        hoverDesc += "\n\nClick to view this case.";
        if (draw.clicked) {
          parent.setActiveFocus(a.plot, false);
        }
      }
    }
    //
    //  Then render investigation options-
    draw.clearEntries();
    draw.addEntry(null, "Investigation Options:", 20, null);
    if (! player.leads.atKnownLocation(suspect)) {
      draw.addEntry(null, "    None- whereabouts unknown.", 20, null);
    }
    draw.addEntry(
      MapView.FILE_IMAGE, "View All Evidence", 20,
      MapView.PLOT_CLUES
    );
    draw.performVerticalDraw(across, down, this, surface, g);
    down = draw.down;
    
    if (draw.hovered == MapView.PLOT_CLUES) {
      hoverDesc = "Review all evidence assembled on this suspect.";
      if (draw.clicked) {
        parent.setActiveFocus(MapView.PLOT_CLUES, false);
      }
    }
    
    draw.clearEntries();
    for (Lead lead : player.leads.leadsFor(suspect)) {
      draw.addEntry(lead.icon(), null, 60, lead);
    }
    draw.performHorizontalDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  Then give suitable tool-tips and click-responses for leads:
    if (draw.hovered instanceof Lead) {
      Lead lead = (Lead) draw.hovered;
      hoverDesc = lead.choiceInfo(agent)+"\n\n"+lead.testInfo(agent);
      if (draw.clicked) {
        if (agent.assignments().includes(lead)) agent.removeAssignment(lead);
        else agent.addAssignment(lead);
      }
    }
    if (hoverDesc.length() == 0) {
      Series <Clue> clues = player.leads.cluesFor(suspect, true);
      if (clues.empty()) {
        if (suspect.isPerson()) {
          hoverDesc = "No current leads on this individual.";
        }
        if (suspect.isPlace()) {
          hoverDesc = "No current leads on this facility.";
        }
      }
      else {
        Clue first = clues.first();
        hoverDesc = CasesFX.longDescription(first, player);
      }
    }
    
    //
    //  Then render hover-information and set the final scroll-height:
    down += 5;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, vw - (across + 10), 150
    );
    down += 150;
    parent.infoArea.setScrollheight(down);
    return true;
  }
  
  
  private String labelFor(Element suspect) {
    if (suspect.isPerson()) {
      return suspect.name()+" ("+suspect.kind().name()+")";
    }
    if (suspect.isPlace()) {
      return suspect.name();
    }
    return suspect.name();
  }

  
  static class AssocResult {
    Element associate;
    Plot    plot;
    String  label;
  }
  
  
  private Element lastAssoc = null;
  private List <AssocResult> assocs;
  
  
  private boolean addAssociate(List list, Element a, Plot p, String label) {
    if (a == null && p == null) return false;
    AssocResult r = new AssocResult();
    r.associate = a;
    r.plot      = p;
    r.label     = label;
    list.add(r);
    return true;
  }
  
  
  private Series <AssocResult> getAssociates(Element suspect, Base player) {
    if (lastAssoc == suspect && assocs != null) return assocs;
    
    lastAssoc = suspect;
    List <AssocResult> list = assocs = new List();

    Place   lastSeen = player.leads.lastKnownLocation(suspect);
    boolean atSeen   = player.leads.atKnownLocation(suspect);
    
    if (suspect.isPerson()) {
      Person perp = (Person) suspect;
      if (lastSeen != null && lastSeen != perp.resides()) {
        String desc = atSeen ? "Current Location" : "Last Known Location";
        addAssociate(list, lastSeen, null, desc);
      }
      if (addAssociate(list, perp.resides(), null, "Residence")) {
        for (Person p : perp.resides().residents()) if (p != perp) {
          addAssociate(list, p, null, HistoryView.bondDescription(perp, p));
        }
      }
      for (Element e : perp.history.sortedBonds()) if (e.isPerson()) {
        addAssociate(list, e, null, HistoryView.bondDescription(perp, e));
      }
      for (Plot plot : player.leads.involvedIn(suspect, true)) {
        addAssociate(list, null, plot, "Suspect In");
      }
    }
    
    if (suspect.isPlace()) {
      Place site = (Place) suspect;
      Person leader = site.base().leader();
      
      if (leader != null && ! leader.isHero()) {
        addAssociate(list, leader, null, "Owner");
      }
      for (Person p : site.residents()) if (p != leader) {
        addAssociate(list, p, null, "Resident");
      }
      for (Plot plot : player.leads.involvedIn(suspect, true)) {
        addAssociate(list, null, plot, "Scene For");
      }
    }
    
    return list;
  }
}









