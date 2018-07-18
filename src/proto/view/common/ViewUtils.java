

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
      Image  icons[];
      String label  ;
      int    along  ;
      Object refers ;
    }
    List <Entry> entries = new List();
    
    public int across, down;
    public Object hovered;
    public boolean clicked;
    
    
    public Object addEntry(Image icon, String label, int along, Object refers) {
      Entry e = new Entry();
      e.icons  = new Image[] { icon };
      e.label  = label ;
      e.along  = along ;
      e.refers = refers;
      entries.add(e);
      return e;
    }
    
    
    public void attachOverlay(Object ref, Image layer) {
      Entry e = (Entry) ref;
      e.icons = (Image[]) Visit.appendTo(e.icons, Image.class, layer);
    }
    
    
    public void clearEntries() {
      entries.clear();
      clicked = false;
      hovered = null ;
    }
    
    
    public void performVerticalDraw(
      int across, int down, UINode within, Surface surface, Graphics2D g
    ) {
      performDraw(across, down, true, within, surface, g);
    }
    
    
    public void performHorizontalDraw(
      int across, int down, UINode within, Surface surface, Graphics2D g
    ) {
      performDraw(across, down, false, within, surface, g);
    }
    
    
    public void performDraw(
      int across, int down, boolean vertical,
      UINode within, Surface surface, Graphics2D g
    ) {
      int vx = within.vx, vy = within.vy, vw = within.vw, maxh = 0;
      this.across = across;
      this.down   = down  ;
      
      for (Entry e : entries) {
        int downEach = e.along, iconSize = 0;
        g.setColor(Color.LIGHT_GRAY);
        if (e.refers == null) g.setColor(Color.WHITE);
        
        if ((across + downEach > vw) && ! vertical) {
          across = this.across;
          down += downEach;
        }
        
        for (Image icon : e.icons) if (icon != null) {
          iconSize = e.along;
          g.drawImage(icon, vx + across, vy + down, iconSize, iconSize, null);
        }
        
        if (e.label != null) ViewUtils.drawWrappedString(
          e.label, g,
          vx + across + (vertical ? (iconSize + 10) : 0),
          vy + down   + (vertical ? 0 : (iconSize + 10)),
          vw - (iconSize + 20), downEach
        );
        
        if (e.refers != null) {
          if (surface.tryHover(
            vx + across,
            vy + down,
            vertical ? (vw - (across + 10)) : downEach,
            downEach,
            e.refers, within
          )) {
            hovered = e.refers;
            clicked = surface.mouseClicked();
          }
          
          g.setColor(hovered == e.refers ? Color.BLUE : Color.GRAY);
          g.drawRect(
            vx + across,
            vy + down,
            vertical ? (vw - (across + 10)) : downEach,
            downEach
          );
        }
        
        if (e.refers instanceof Assignment) {
          Assignment a = (Assignment) e.refers;
          Base player = within.mainView.player();
          
          if (a.assigningBase() == player) renderAssigned(
            a.assigned(),
            vx + (vertical ? (across + vw - 20) : (across + downEach)),
            vy + down + downEach + 10,
            within, surface, g
          );
        }
        
        if (vertical) {
          down += downEach + 5;
        }
        else {
          across += downEach + 5;
          maxh = Nums.max(maxh, downEach);
        }
      }
      
      this.across = across;
      this.down   = down + maxh;
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
          parent.setSelectedPerson(p);
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















