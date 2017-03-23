

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Graphics2D;



public class MissionsViewLinksView extends UINode {
  
  
  final MissionsView parent;
  
  
  public MissionsViewLinksView(MissionsView parent, Box2D bounds) {
    super(parent, bounds);
    this.parent = parent;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Extract basic game-references first:
    Base player = mainView.player();
    Element perp = (Element) parent.focusOfType(Element.class);
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    draw.addEntry(
      null, "KNOWN ASSOCIATES FOR "+perp, 40, null
    );
    for (AssocResult r : getAssociates(perp, player)) {
      if (r.associate != null) draw.addEntry(
        r.associate.icon(), r.associate.name()+" ("+r.label+")", 40,
        r.associate
      );
      if (r.plot != null) draw.addEntry(
        r.plot.icon(), r.label+" "+r.plot.nameForCase(player), 40,
        r.plot
      );
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
      if (addAssociate(list, perp.resides(), null, "Residence")) {
        for (Person p : perp.resides().residents()) if (p != perp) {
          addAssociate(list, p, null, "Colleague");
        }
      }
      for (Element e : perp.history.sortedBonds()) if (e.isPerson()) {
        float bond = perp.history.bondWith(e);
        addAssociate(list, e, null, (bond > 0 ? "Friend " : "Enemy ")+bond);
      }
      for (Plot plot : player.leads.involvedIn(suspect, true)) {
        addAssociate(list, null, plot, "Suspect In");
      }
    }
    
    if (suspect.isPlace()) {
      Place site = (Place) suspect;
      addAssociate(list, site.region(), null, "Region");
      for (Person p : site.residents()) {
        addAssociate(list, p, null, "Resident");
      }
      for (Plot plot : player.leads.involvedIn(suspect, true)) {
        addAssociate(list, null, plot, "Scene For");
      }
    }
    
    if (suspect.isRegion()) {
      Region area = (Region) suspect;
      for (Place p : area.buildSlots()) {
        addAssociate(list, p, null, "Address");
      }
      for (Plot plot : player.leads.knownPlotsForRegion(area)) {
        addAssociate(list, null, plot, "Area For");
      }
    }
    
    return list;
  }
  
}



