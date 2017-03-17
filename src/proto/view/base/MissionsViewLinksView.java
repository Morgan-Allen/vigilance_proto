

package proto.view.base;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



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
      draw.addEntry(
        r.associate.icon(), r.associate.name()+" ("+r.label+")", 40,
        r.associate
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
    String label;
  }
  
  
  private boolean addAssociate(List list, Element a, String label) {
    if (a == null) return false;
    AssocResult r = new AssocResult();
    r.associate = a;
    r.label = label;
    list.add(r);
    return true;
  }
  
  
  private Series <AssocResult> getAssociates(Element suspect, Base player) {
    List <AssocResult> list = new List();
    
    if (suspect.isPerson()) {
      Person perp = (Person) suspect;
      if (addAssociate(list, perp.resides(), "Residence")) {
        for (Person p : perp.resides().residents()) if (p != perp) {
          addAssociate(list, p, "Colleague");
        }
      }
    }
    if (suspect.isPlace()) {
      Place site = (Place) suspect;
      addAssociate(list, site.region(), "Region");
      for (Person p : site.residents()) {
        addAssociate(list, p, "Resident");
      }
    }
    if (suspect.isRegion()) {
      Region area = (Region) suspect;
      for (Place p : area.buildSlots()) {
        addAssociate(list, p, "Address");
      }
    }
    
    return list;
  }
  
}



