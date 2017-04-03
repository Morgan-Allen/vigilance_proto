

package proto.view.base;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;

import proto.util.*;
import proto.view.common.*;

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
  
  final static Color statColors[] = new Color[] {
    new Color(0.0f, 0.0f, 1.0f),
    new Color(0.0f, 1.0f, 0.0f),
    new Color(1.0f, 1.0f, 0.5f),
    new Color(1.0f, 0.5f, 1.0f)
  };
  
  
  private Person selectedPerson = null;
  
  
  public RosterView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  public void setSelectedPerson(Person person) {
    this.selectedPerson = person;
  }
  
  
  public Person selectedPerson() {
    return selectedPerson;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Base base = mainView.world().playerBase();
    
    Image selectCircle = mainView.selectCircle;
    int across = 0, down = 15, size = 75, sizeA = 25, pad = 5, x, y;
    
    Person personHovered = null;
    if (selectedPerson == null) setSelectedPerson(base.roster().first());
    
    for (Person p : base.roster()) {
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
      
      if (surface.tryHover(x, y, size, size, p, this)) {
        personHovered = p;
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      if (selectedPerson == p) {
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      
      int acrossP = x;
      for (Assignment a : p.assignments()) {
        g.drawImage(a.icon(), acrossP, y + size - sizeA, sizeA, sizeA, null);
        acrossP += sizeA;
      }
      
      int MH = p.health.maxHealth(), MS = p.health.maxStress();
      int inj = (int) p.health.totalHarm();
      int str = (int) p.health.stress();
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
    
    if (personHovered != null && surface.mouseClicked()) {
      setSelectedPerson(personHovered);
    }
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    
    return true;
  }
}






