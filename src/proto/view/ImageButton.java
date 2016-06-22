


package proto.view;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public abstract class ImageButton extends UINode {
  
  
  final Image icon;
  private Image overlay[] = null;
  public boolean toggled = false, valid = true;
  public Object refers = this;
  
  
  ImageButton(Image icon, Box2D bounds, UINode parent) {
    super(parent, bounds);
    this.icon = icon;
  }
  
  
  void attachOverlay(Image... overlay) {
    this.overlay = overlay;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    final boolean hovered = surface.tryHover(vx, vy, vw, vh, refers) && valid;
    
    g.drawImage(icon, vx, vy, vw, vh, null);
    if (overlay != null) for (Image over : overlay) {
      g.drawImage(over, vx, vy, vw, vh, null);
    }
    
    if (hovered || toggled) {
      g.setColor(toggled ? Color.GREEN : Color.YELLOW);
      g.drawRect(vx, vy, vw, vh);
    }
    
    if (hovered) whenHovered();
    if (hovered && surface.mouseClicked()) whenClicked();
    
    return true;
  }
  
  
  void whenHovered() {
    return;
  }
  
  
  abstract void whenClicked();
  
  
}





