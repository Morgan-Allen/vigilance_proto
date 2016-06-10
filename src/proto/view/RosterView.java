

package proto.view;
import proto.util.*;
import proto.game.world.*;
import proto.common.Kind;
import proto.game.person.*;

import java.awt.*;
import java.awt.image.*;



public class RosterView extends UINode {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  final static Image
    NOT_ASSIGNED  = Kind.loadImage(
      "media assets/action view/assignment_blank.png"
    ),
    ASSIGN_OKAY   = Kind.loadImage(
      "media assets/action view/select_okay.png"
    ),
    ASSIGN_FORBID = Kind.loadImage(
      "media assets/action view/select_illegal.png"
    );
  
  
  final Color statColors[] = new Color[4];
  //final Image statIcons [] = new Image[4];
  
  private Person selectedPerson;
  
  
  RosterView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    statColors[0] = new Color(0.0f, 0.0f, 1.0f);
    statColors[1] = new Color(0.0f, 1.0f, 0.0f);
    statColors[2] = new Color(1.0f, 1.0f, 0.5f);
    statColors[3] = new Color(1.0f, 0.5f, 1.0f);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    Base base = mainView.world.base();
    Assignment assignTo = mainView.areaView.selectedTask();
    
    Image selectCircle = mainView.selectCircle;
    int across = 0, down = 15, size = 75, sizeA = 25, pad = 25, x, y;
    
    Person personHovered = null;
    Person assignHovered = null;
    
    for (Person p : base.roster()) {
      final int personID = base.rosterIndex(p);
      final boolean canAssign = assignTo != null && p.canAssignTo(assignTo);
      
      int nextAcross = across + size + pad;
      if (nextAcross >= vw) {
        across = 0;
        down += size + pad + 50;
      }
      
      x = vx + across + 10;
      y = vy + down + 10;
      across += size + pad;
      
      g.drawImage(p.kind().sprite(), x, y, size, size, null);
      
      g.setColor(Color.LIGHT_GRAY);
      g.drawString(p.name(), x, y - 5);
      
      if (surface.tryHover(x, y, size, size, p)) {
        personHovered = p;
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      if (selectedPerson == p) {
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      
      final Assignment a = p.assignment();
      Image forA = a == null ? null : a.icon();
      
      final boolean hoverA = surface.tryHover(
        x, y + size - sizeA, sizeA, sizeA, "Roster_"+personID
      );
      g.drawImage(forA, x, y + size - sizeA, sizeA, sizeA, null);
      if (assignTo != null) {
        if (a == assignTo) forA = ASSIGN_OKAY;
        else if (! canAssign) forA = ASSIGN_FORBID;
        g.drawImage(forA, x, y + size - sizeA, sizeA, sizeA, null);
      }
      if (hoverA && canAssign) {
        g.drawImage(selectCircle, x, y + size - sizeA, sizeA, sizeA, null);
        assignHovered = p;
      }
      
      int MH = p.maxHealth(), MS = p.maxStress();
      int inj = (int) p.injury();//, fat = (int) p.stun();
      int str = (int) p.stress();
      float injLevel = (MH - inj) * 1f / MH, strLevel = str * 1f / MS;
      
      ViewUtils.renderStatBar(
        x, y + size + 5, size, 5,
        Color.BLUE, Color.DARK_GRAY, injLevel, false, g
      );
      ViewUtils.renderStatBar(
        x, y + size + 10, size, 5,
        Color.GRAY, Color.DARK_GRAY, strLevel, false, g
      );
    }
    
    if (assignHovered != null && surface.mouseClicked() && assignTo != null) {
      final Person p = assignHovered;
      final Assignment o = p.assignment();
      if (o != null    ) o       .setAssigned(p, false);
      if (o != assignTo) assignTo.setAssigned(p, true );
    }
    if (personHovered != null && surface.mouseClicked()) {
      if (selectedPerson == personHovered) personHovered = null;
      setSelection(personHovered);
    }
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
  }
  
  
  void setSelection(Person selected) {
    selectedPerson = selected;
  }
  
  
  Person selected() {
    return selectedPerson;
  }
}



