

package proto.view;
import proto.util.*;
import proto.game.world.*;
import proto.game.person.*;

import java.awt.*;
import java.awt.image.*;



public class RosterView {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  final WorldView parent;
  final Box2D viewBounds;
  
  Person selectedPerson;
  
  
  RosterView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    Base base = parent.world.base();
    Person personHovered = null;
    
    Image selectCircle = parent.selectCircle;
    int
      offX      = (int) viewBounds.xpos(),
      offY      = (int) viewBounds.ypos(),
      maxAcross = (int) viewBounds.xdim(),
      across = 0, down = 15, size = 75, pad = 25, x, y
    ;
    
    for (Person p : base.roster()) {
      x = offX + across;
      y = offY + down;
      g.drawImage(p.kind().sprite(), x, y, size, size, null);
      
      g.setColor(Color.LIGHT_GRAY);
      g.drawString(p.name()+" ("+(base.rosterIndex(p) + 1)+")", x, y - 2);
      
      if (surface.mouseIn(x, y, size, size)) {
        personHovered = p;
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      if (selectedPerson == p) {
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      
      //  TODO:  Render differently for different assignments!
      /*
      if (p.assignment() != null) {
        g.drawImage(alertMarker, x + size - 20, y + size - 20, 20, 20, null);
      }
      //*/
      
      across += size + pad;
      if (across >= maxAcross) { across = 0; down += size + pad; }
    }
    if (personHovered != null && surface.mouseClicked) {
      parent.setSelection(selectedPerson = personHovered);
    }
  }
  
}
