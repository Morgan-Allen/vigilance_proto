

package proto.view;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public abstract class StringButton extends UINode {
  
  
  final String label;
  public boolean toggled = false;
  
  
  StringButton(String label, Box2D bounds, UINode parent) {
    super(parent, bounds);
    this.label = label;
  }
  
  
  StringButton(String label, int x, int y, int w, int h, UINode parent) {
    this(label, new Box2D(x, y, w, h), parent);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    
    boolean hovered = surface.mouseIn(vx, vy, vw, vh, parent);
    
    if (toggled) g.setColor(Color.GREEN);
    else if (hovered) g.setColor(Color.YELLOW);
    else g.setColor(Color.BLUE);
    g.drawString(label, vx + 5, vy + 15);
    g.drawRect(vx, vy, vw, vh);
    
    if (hovered && surface.mouseClicked(parent)) whenClicked();
  }
  
  
  abstract void whenClicked();
  
}




