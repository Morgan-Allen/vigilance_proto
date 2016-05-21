

package proto.view;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class PersonView {
  
  
  final static Object[] STAT_DISPLAY_COORDS = {
    PERCEPTION, 0, 0,
    EVASION   , 0, 1,
    SOCIAL    , 0, 2,
    COMBAT    , 0, 3,
  };
  
  final WorldView parent;
  final Box2D viewBounds;
  
  
  PersonView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent;
    this.viewBounds = viewBounds;
  }
  
  

  void renderTo(Surface surface, Graphics2D g) {
    
    final Person person = (Person) parent.lastSelected;
    
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    
    g.setColor(Color.WHITE);
    g.drawImage(person.kind().sprite(), vx, vy, 120, 120, null);
    
    g.drawString("Codename: "+person.name(), vx + 125, vy + 20);
    ViewUtils.drawWrappedString(
      person.history.summary(), g, vx + 125, vy + 20, vw - (120 + 10), 100
    );
    //g.drawRect(vx + 125, vy + 20, vw - (120 + 10), 100);
    
    for (Trait t : ALL_STATS) {
      int index = Visit.indexOf(t, STAT_DISPLAY_COORDS);
      if (index == -1) continue;
      
      float level = person.stats.levelFor(t);
      int x = (Integer) STAT_DISPLAY_COORDS[index + 1];
      int y = (Integer) STAT_DISPLAY_COORDS[index + 2];
      x *= 100;
      y *=  20;
      g.drawString(t.name    , vx + x + 20     , vy + y + 120 + 25);
      g.drawString(": "+level, vx + x + 20 + 75, vy + y + 120 + 25);
    }
    
  }
  
  
  
}










