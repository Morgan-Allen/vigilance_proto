

package proto.view.common;
import proto.util.*;
import java.awt.Color;
import java.awt.Graphics2D;



public class ScrollArea extends UINode {
  
  
  int barWidth = 20;
  int barOffset = 0;
  int scrollHeight;
  float relativeScroll;
  UINode scrollPane;
  
  
  public ScrollArea(UINode parent, Box2D bounds) {
    super(parent, bounds);
    this.clipContent = true;
  }
  
  
  public void setScrollheight(int height) {
    this.scrollHeight = height;
  }
  
  
  public void attachScrollPane(UINode kid, int scrollHeight) {
    if (scrollPane != null && kid != scrollPane) {
      setChild(scrollPane, false);
    }
    
    this.scrollHeight = scrollHeight;
    this.scrollPane   = kid;
    
    if (scrollPane != null) {
      int diffY = Nums.max(0, scrollHeight - vh);
      int offY = (int) (relativeScroll * diffY);
      int paneH = Nums.max(scrollHeight, vh);
      scrollPane.relBounds.set(0, 0 - offY, vw - barWidth, paneH);
      setChild(scrollPane, true);
    }
  }
  
  
  protected void renderAfterKids(Surface surface, Graphics2D g) {
    attachScrollPane(scrollPane, scrollHeight);
    
    float scrollRatio = vh * 1f / scrollHeight;
    if (scrollRatio >= 1) { barOffset = 0; relativeScroll = 0; return; }
    
    int barHigh = (int) (vh * scrollRatio);
    int x = vx + vw - (barWidth + 1), y = vy + barOffset;
    relativeScroll = barOffset * 1f / (vh - (barHigh + 1));
    
    g.setColor(Color.BLUE);
    g.drawRect(x, y, barWidth, barHigh);
    
    if (surface.tryHover(x, y, barWidth, barHigh, "scroll_bar_"+this)) {
      g.setColor(Color.YELLOW);
      g.drawRect(x, y, barWidth, barHigh);
      
      if (surface.mouseDown()) {
        int moveY = surface.moveY();
        barOffset -= moveY;
        barOffset = Nums.clamp(barOffset, vh - barHigh);
      }
    }
  }
  
}


