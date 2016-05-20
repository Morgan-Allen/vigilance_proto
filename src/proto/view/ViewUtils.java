

package proto.view;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.FontMetrics;
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
        if (lineWide > w) break;
        line = lineMore;
        tokens.removeFirst();
      }
      
      downMore += metrics.getHeight();
      if (downMore > h) break;
      down = downMore;
      g.drawString(line, x, y + down);
    }
  }
  
  
  void renderAssigned(
    Series <Person> assigned, int atX, int atY,
    Surface surface, Graphics2D g
  ) {
    int x = atX, y = atY;
    for (Person p : assigned) {
      g.drawImage(p.kind().sprite(), x, y, 20, 20, null);
      x += 20;
    }
  }
  
  
  
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















