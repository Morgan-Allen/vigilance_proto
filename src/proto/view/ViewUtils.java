

package proto.view;
//import proto.game.person.Person;
//import proto.util.Series;
import proto.util.*;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.util.StringTokenizer;



public class ViewUtils {
  
  
  
  static void drawWrappedString(
    String s, Graphics2D g, int x, int y, int w, int h
  ) {
    StringTokenizer t = new StringTokenizer(s, " \n");
    final List <String> tokens = new List();
    while (t.hasMoreTokens()) tokens.add(t.nextToken());
    
    final FontMetrics metrics = g.getFontMetrics();
    int down = 0, downMore = down;
    
    while (! tokens.empty()) {
      String line = "", lineMore = line;
      
      while (! tokens.empty()) {
        lineMore = line + tokens.first() + " ";
        int lineWide = metrics.stringWidth(lineMore);
        if (lineWide > w) break;
        line = lineMore;
        tokens.removeFirst();
      }
      
      downMore += metrics.getHeight();
      if (downMore > h) break;
      g.drawString(line, x, y + down);
      down = downMore;
    }
  }
  
  
  /*
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
  
  //*/
}




