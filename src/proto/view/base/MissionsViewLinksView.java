

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
    Base player = mainView.player();
    Element perp = (Element) parent.focusOfType(Element.class);
    
    int across = 10, down = 10;
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      "KNOWN ASSOCIATES FOR "+perp, g, vx + across, vy + down, vw - 20, 40
    );
    down += 40;
    
    for (AssocResult r : getAssociates(perp, player)) {
      g.setColor(Color.LIGHT_GRAY);
      Image icon = r.associate.icon();
      String desc = r.associate.name()+" ("+r.label+")";
      
      g.drawImage(icon, vx + across, vy + down, 40, 40, null);
      ViewUtils.drawWrappedString(
        desc.toString(), g, vx + across + 60, vy + down, vw - 60, 40
      );
      
      if (surface.tryHover(vx + across, vy + down, vw - 20, 40, r.associate)) {
        g.setColor(Color.GRAY);
        g.drawRect(vx + across, vy + down, vw - 20, 40);
        
        if (surface.mouseClicked()) {
          parent.setActiveFocus(r.associate, true);
        }
      }
      
      down += 45;
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



