

package proto.view;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public abstract class StringButton {
  
  
  final String label;
  final Object parent;
  final Box2D viewBounds;
  
  public boolean toggled = false;
  
  
  StringButton(String label, Box2D bounds, Object parent) {
    this.label      = label ;
    this.viewBounds = bounds;
    this.parent     = parent;
  }
  
  
  StringButton(String label, int x, int y, int w, int h, Object parent) {
    this(label, new Box2D(x, y, w, h), parent);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    
    boolean hovered = surface.mouseIn(viewBounds, parent);
    if (toggled) g.setColor(Color.GREEN);
    else if (hovered) g.setColor(Color.YELLOW);
    else g.setColor(Color.BLUE);
    g.drawString(label, vx + 5, vy + 15);
    g.drawRect(vx, vy, vw, vh);
    
    if (hovered && surface.mouseClicked(parent)) whenClicked();
  }
  
  
  abstract void whenClicked();
  
}




