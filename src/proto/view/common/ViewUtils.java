

package proto.view.common;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.StringTokenizer;



public class ViewUtils {
  
  
  public static void drawWrappedString(
    String s, Graphics2D g, int x, int y, int w, int h
  ) {
    if (s == null || g == null) return;
    
    StringTokenizer t = new StringTokenizer(s, " \n", true);
    final List <String> tokens = new List();
    while (t.hasMoreTokens()) tokens.add(t.nextToken());
    
    final FontMetrics metrics = g.getFontMetrics();
    int down = 0, downMore = down;
    
    while (! tokens.empty()) {
      String line = "", lineMore = line;
      
      while (! tokens.empty()) {
        final String token = tokens.first();
        if (token.equals("\n")) { tokens.removeFirst(); break; }
        
        lineMore = line + token;
        int lineWide = metrics.stringWidth(lineMore);
        if (lineWide >= w) break;
        
        line = lineMore;
        tokens.removeFirst();
      }
      
      downMore += metrics.getHeight();
      if (downMore > h) break;
      
      down = downMore;
      g.drawString(line, x, y + down);
    }
  }
  
  
  
  /**  List-display utilities:
    */
  public static class ListDraw {
    
    static class Entry {
      Image icon;
      String label;
      int down;
      Object refers;
    }
    List <Entry> entries = new List();
    
    public int across, down;
    public Object hovered;
    public boolean clicked;
    
    
    public void addEntry(Image icon, String label, int down, Object refers) {
      Entry e = new Entry();
      e.icon   = icon  ;
      e.label  = label ;
      e.down   = down  ;
      e.refers = refers;
      entries.add(e);
    }
    
    
    public void clearEntries() {
      entries.clear();
      clicked = false;
      hovered = null;
    }
    
    
    public void performDraw(
      int across, int down, UINode within, Surface surface, Graphics2D g
    ) {
      int vx = within.vx, vy = within.vy, vw = within.vw;
      
      for (Entry e : entries) {
        int downEach = e.down, iconSize = 0;
        g.setColor(Color.LIGHT_GRAY);
        if (e.refers == null) g.setColor(Color.WHITE);
        
        if (e.icon != null) {
          iconSize = e.down;
          g.drawImage(e.icon, vx + across, vy + down, iconSize, iconSize, null);
        }
        
        if (e.label != null) ViewUtils.drawWrappedString(
          e.label, g,
          vx + across + iconSize + 10, vy + down,
          vw - (iconSize + 20), downEach
        );
        
        if (e.refers != null) {
          if (surface.tryHover(
            vx + across, vy + down, vw - (across + 10), downEach,
            e.refers, within
          )) {
            hovered = e.refers;
            clicked = surface.mouseClicked(); 
          }
          
          g.setColor(hovered == e.refers ? Color.BLUE : Color.GRAY);
          g.drawRect(vx + across, vy + down, vw - (across + 10), downEach);
        }
        
        if (e.refers instanceof Assignment) renderAssigned(
          ((Assignment) e.refers).assigned(),
          vx + across + vw - 20, vy + down + downEach,
          within, surface, g
        );
        
        down += downEach + 5;
      }
      
      this.across = across;
      this.down = down;
      entries.clear();
    }
  }
  
  
  
  /**  Rendering persons and stats-
    */
  public static void renderAssigned(
    Series <Person> assigned, int atX, int atY,
    UINode within, Surface surface, Graphics2D g
  ) {
    int x = atX - 20, y = atY - 20;
    g.setColor(Color.YELLOW);
    final MainView parent = surface.game.world().view();
    
    int moveX = 20;
    if (assigned.size() > 3) {
      moveX = 40 / (assigned.size() - 1);
    }
    
    for (Person p : assigned) {
      boolean hovered = surface.tryHover(x, y, 20, 20, p, within);
      g.drawImage(p.kind().sprite(), x, y, 20, 20, null);
      
      if (hovered) {
        g.drawOval(x, y, 20, 20);
        if (surface.mouseClicked()) {
          parent.rosterView.setSelectedPerson(p);
        }
      }
      x -= moveX;
    }
  }
  
  
  public static void renderStatBar(
    int x, int y, int w, int h,
    Color tint, Color back, float fill, boolean vertical, Graphics g
  ) {
    if (back != null) {
      g.setColor(back);
      g.fillRect(x, y, w, h);
    }
    g.setColor(tint);
    
    if (vertical) {
      int barH = (int) (h * fill);
      g.fillRect(x, y + h - barH, w, barH);
    }
    else {
      int barW = (int) (w * fill);
      g.fillRect(x, y, barW, h);
    }
  }
}















