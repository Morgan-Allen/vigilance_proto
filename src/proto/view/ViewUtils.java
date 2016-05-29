

package proto.view;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;



public class ViewUtils {
  
  
  static void drawWrappedString(
    String s, Graphics2D g, int x, int y, int w, int h
  ) {
    StringTokenizer t = new StringTokenizer(s, " \n", true);
    final List <String> tokens = new List();
    while (t.hasMoreTokens()) tokens.add(t.nextToken());
    
    final FontMetrics metrics = g.getFontMetrics();
    int down = 0, downMore = down;
    
    while (! tokens.empty()) {
      String line = "", lineMore = line;
      
      while (! tokens.empty()) {
        final String token = tokens.first();
        
        if (token.equals(" " )) { tokens.removeFirst(); continue; }
        if (token.equals("\n")) { tokens.removeFirst(); break   ; }
        
        lineMore = line + token + " ";
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
  
  
  
  /**  Rendering persons and stats-
    */
  static void renderAssigned(
    Series <Person> assigned, int atX, int atY,
    Surface surface, Graphics2D g
  ) {
    int x = atX - 20, y = atY - 20;
    g.setColor(Color.YELLOW);
    final MainView parent = surface.game.world().view();
    
    int moveX = 20;
    if (assigned.size() > 3) {
      moveX = 40 / (assigned.size() - 1);
    }
    
    for (Person p : assigned) {
      boolean hovered = surface.tryHover(x, y, 20, 20, p);
      g.drawImage(p.kind().sprite(), x, y, 20, 20, null);
      
      if (hovered) {
        g.drawOval(x, y, 20, 20);
        if (surface.mouseClicked()) {
          parent.rosterView.setSelection(p);
        }
      }
      x -= moveX;
    }
  }
  
  
  static void renderStatBar(
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
  
  
  
  /**  Time and date utilities-
    */
  final static DateFormat
    DISPLAY_FORMAT = new SimpleDateFormat("HH:mm EEE, MMM d, yyyy");
  static Date
    DEFAULT_INIT_DATE = new Date();
  static {
    try { DEFAULT_INIT_DATE = DISPLAY_FORMAT.parse("00:00 Mon, Jul 9, 1984"); }
    catch (Exception e) {}
  }
  
  static String getTimeString(World world) {
    
    long timeMS = 0;
    timeMS += world.timeDays();
    timeMS *= 24;
    timeMS += world.timeHours();
    timeMS *= 60;
    timeMS += world.timeMinutes();
    timeMS *= 60 * 1000;
    timeMS += DEFAULT_INIT_DATE.getTime();
    
    final Date date = new Date();
    date.setTime(timeMS);
    
    return DISPLAY_FORMAT.format(date);
  }
}















