

package proto.view.common;
import proto.util.*;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.FontMetrics;



public abstract class ClickMenu <T> extends UINode {
  
  
  int atX, atY;
  Color background;
  Series <T> options;
  
  
  
  public ClickMenu(Series <T> options, int atX, int atY, MainView parent) {
    super(parent);
    this.options = options;
    this.atX = atX;
    this.atY = atY;
    background = new Color(0, 0, 0, 0.75f);
  }
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Image  images[] = new Image [options.size()];
    String labels[] = new String[options.size()];
    
    int totalHigh = 0, maxWide = 0;
    FontMetrics FM = g.getFontMetrics();
    
    int i = 0; for (T option : options) {
      images[i] = imageFor(option);
      labels[i] = labelFor(option);
      totalHigh += (images[i] == null) ? 20 : 40;
      maxWide = Nums.max(maxWide, FM.stringWidth(labels[i]) + 10 + 40);
      i++;
    }
    
    if (atX + maxWide   > surface.getWidth ()) atX -= maxWide  ;
    if (atY + totalHigh > surface.getHeight()) atY -= totalHigh;
    
    g.setColor(background);
    g.fillRect(atX, atY, maxWide, totalHigh);
    g.setColor(Color.WHITE);
    g.drawRect(atX, atY, maxWide, totalHigh);
    
    int x = atX, y = atY;
    this.relBounds.set(x, y, maxWide, totalHigh);
    
    //  TODO:  It might help to have a consistent set of UI classes for 'stuff
    //  that comes in a vertical list and has icons'.  Then factor that out of
    //  here and the rooms-view, et cetera.
    
    final boolean hoverMenu = surface.tryHover(
      0, 0, surface.getWidth(), surface.getHeight(), this, this
    );
    
    for (i = 0; i < options.size(); i++) {
      String label = labels[i];
      Image  image = images[i];
      T option = options.atIndex(i);
      boolean hovered = false;
      g.setColor(Color.WHITE);
      
      if (image == null) {
        hovered = surface.tryHover(x, y, maxWide, 20, option, this);
        if (hovered) g.setColor(Color.YELLOW);
        g.drawString(label, x + 5, y + 15);
        y += 20;
      }
      else {
        hovered = surface.tryHover(x, y, maxWide, 40, option, this);
        if (hovered) g.setColor(Color.YELLOW);
        g.drawImage(image, x, y, 40, 40, null);
        g.drawString(label, x + 40 + 5, y + 15);
        y += 40;
      }
      if (surface.mouseClicked() && hovered) {
        whenPicked(option, i);
        mainView.hideClickMenu(this);
      }
    }
    
    if (hoverMenu && surface.mouseClicked()) {
      mainView.hideClickMenu(this);
    }
    
    return true;
  }
  
  
  protected Image imageFor(T option) { return null; }
  protected String labelFor(T option) { return option.toString(); }
  protected abstract void whenPicked(T option, int optionID);
  
}





