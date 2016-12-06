

package proto.view.common;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public abstract class StringButton extends UINode {
  
  
  public String label;
  public boolean toggled = false, valid = true;
  public Object refers = this;
  
  
  public StringButton(String label, Box2D bounds, UINode parent) {
    super(parent, bounds);
    this.label = label;
  }
  
  
  public StringButton(String label, int x, int y, int w, int h, UINode parent) {
    this(label, new Box2D(x, y, w, h), parent);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    final boolean hovered = surface.tryHover(vx, vy, vw, vh, refers) && valid;
    
    if      (toggled) g.setColor(Color.GREEN );
    else if (hovered) g.setColor(Color.YELLOW);
    else if (! valid) g.setColor(Color.GRAY  );
    else              g.setColor(Color.BLUE  );
    g.drawString(label, vx + 5, vy + 15);
    g.drawRect(vx, vy, vw, vh);
    
    if (hovered) whenHovered();
    if (hovered && surface.mouseClicked()) whenClicked();
    
    return true;
  }
  
  
  protected void whenHovered() {
    return;
  }
  
  
  protected abstract void whenClicked();
  
}




