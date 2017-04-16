

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Graphics2D;



public class MissionsViewLinksView extends UINode {
  
  
  public MissionsViewLinksView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    MissionsView parent = mainView.missionView;
    Object focus = parent.priorFocus();
    if (! (focus instanceof Element)) return false;
    Element suspect = (Element) focus;
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "KNOWN ASSOCIATES FOR "+suspect, 40, null
    );
    for (AssocResult r : getAssociates(suspect, player)) {
      if (r.associate != null) draw.addEntry(
        r.associate.icon(), r.associate.name()+" ("+r.label+")", 25,
        r.associate
      );
      if (r.plot != null) draw.addEntry(
        r.plot.icon(), r.label+" "+r.plot.nameForCase(player), 25,
        r.plot
      );
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
  
  
  static class AssocResult {
    Element associate;
    Plot    plot;
    String  label;
  }
  
  
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
    List <AssocResult> list = new List();
    
    if (suspect.isPerson()) {
      Person perp = (Person) suspect;
      for (Plot plot : player.leads.involvedIn(suspect, true)) {
        addAssociate(list, null, plot, "Suspect In");
      }
      if (addAssociate(list, perp.resides(), null, "Residence")) {
        for (Person p : perp.resides().residents()) if (p != perp) {
          addAssociate(list, p, null, HistoryView.bondDescription(perp, p));
        }
      }
      for (Element e : perp.history.sortedBonds()) if (e.isPerson()) {
        addAssociate(list, e, null, HistoryView.bondDescription(perp, e));
      }
    }
    
    if (suspect.isPlace()) {
      Place site = (Place) suspect;
      for (Plot plot : player.leads.involvedIn(suspect, true)) {
        addAssociate(list, null, plot, "Scene For");
      }
      addAssociate(list, site.region(), null, "Region");
      for (Person p : site.residents()) {
        addAssociate(list, p, null, "Resident");
      }
    }
    
    if (suspect.isRegion()) {
      Region area = (Region) suspect;
      for (Plot plot : player.leads.activePlotsForRegion(area)) {
        addAssociate(list, null, plot, "Area For");
      }
      for (Place p : area.buildSlots()) {
        if (addAssociate(list, p, null, "Address")) {
          for (Person r : p.residents()) {
            addAssociate(list, r, null, "Resident");
          }
        }
      }
    }
    
    return list;
  }
  
}



