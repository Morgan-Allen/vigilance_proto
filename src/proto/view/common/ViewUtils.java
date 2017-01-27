

package proto.view.common;
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
  
  
  
  /**  Rendering persons and stats-
    */
  public static void renderAssigned(
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
  
  
  
  /**  Time and date utilities-
    */
  public static String getTimeString(World world) {
    int minutes = world.timing.timeMinutes();
    int hours   = world.timing.timeHours  ();
    int day     = world.timing.dayInMonth ();
    int month   = world.timing.monthInYear();
    int year    = world.timing.timeYears  ();
    String hourString = ""+I.lengthen(hours  , 2, true);
    String minsString = ""+I.lengthen(minutes, 2, true);
    String monthName = Timing.MONTH_NAMES[month];
    return hourString+":"+minsString+", "+monthName+" "+day+", "+year;
  }
}















