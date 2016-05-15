

package proto.view;
import proto.util.*;
import proto.game.world.*;
import proto.common.Kind;
import proto.game.person.*;

import java.awt.*;
import java.awt.image.*;



public class RosterView {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  final static Image
    NOT_ASSIGNED = Kind.loadImage(
      "media assets/scene backgrounds/no_assignment.png"
    );
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  final Color statColors[] = new Color[4];
  final Image statIcons [] = new Image[4];
  
  Person selectedPerson;
  
  
  RosterView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
    
    statColors[0] = new Color(0.0f, 0.0f, 1.0f);
    statColors[1] = new Color(0.0f, 1.0f, 0.0f);
    statColors[2] = new Color(1.0f, 1.0f, 0.5f);
    statColors[3] = new Color(1.0f, 0.5f, 1.0f);
    
    final String imgPath = "media assets/stat icons/";
    statIcons[0] = Kind.loadImage(imgPath+"icon_intellect.png");
    statIcons[1] = Kind.loadImage(imgPath+"icon_evasion.png"  );
    statIcons[2] = Kind.loadImage(imgPath+"icon_social.png"   );
    statIcons[3] = Kind.loadImage(imgPath+"icon_combat.png"   );
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    Base base = parent.world.base();
    Person personHovered = null;
    
    Image selectCircle = parent.selectCircle;
    int
      offX      = (int) viewBounds.xpos(),
      offY      = (int) viewBounds.ypos(),
      maxAcross = (int) viewBounds.xdim(),
      across = 0, down = 15, size = 75, sizeA = 25, pad = 25, x, y
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
      
      final Assignment a = p.assignment();
      final Image forA = a == null ? NOT_ASSIGNED : a.icon();
      g.drawImage(forA, x, y + size - sizeA, sizeA, sizeA, null);
      
      /*
      int MH = p.maxHealth(), MS = p.maxStress();
      int inj = (int) p.injury(), fat = (int) p.stun();
      int str = (int) p.stress();
      
      /*
      renderStatBar(
        x + size + 05, y, 5, size + 35,
        Color.RED, Color.BLACK, (MH - inj) * 1f / MH, g
      );
      renderStatBar(
        x + size + 10, y, 5, size + 35,
        Color.LIGHT_GRAY, Color.GRAY, str * 1f / MS, g
      );
      //*/
      
      int index = 0;
      for (Trait t : PersonStats.BASE_STATS) {
        float fill = p.stats.levelFor(t) / 10f;
        renderStatBar(
          x + (index * 19), y + size + 5, 16, 20,
          statColors[index], null, fill, g
        );
        g.drawImage(
          statIcons[index], x + (index * 19), y + size + 17, 16, 16, null
        );
        index++;
      }
      
      across += size + pad;
      if (across >= maxAcross) { across = 0; down += size + pad; }
    }
    
    if (personHovered != null && surface.mouseClicked) {
      final Assignment a = parent.currentAssignment();
      final Person p = personHovered;
      final Assignment o = p.assignment();
      if (a != null) {
        if (o != null) o.setAssigned(p, false);
        if (o != a   ) a.setAssigned(p, true );
      }
      else parent.setSelection(selectedPerson = personHovered);
    }
  }
  
  
  void renderStatBar(
    int x, int y, int w, int h, Color c, Color b, float fill, Graphics g
  ) {
    if (b != null) {
      g.setColor(b);
      g.fillRect(x, y, w, h);
    }
    
    int barH = (int) (h * fill);
    g.setColor(c);
    g.fillRect(x, y + h - barH, w, barH);
  }
}










