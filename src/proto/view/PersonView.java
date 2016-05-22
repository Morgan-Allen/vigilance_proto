

package proto.view;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class PersonView {
  
  final static Object[] STAT_DISPLAY_COORDS = {
    INTELLECT, 0, 0,
    REFLEX   , 0, 1,
    SOCIAL   , 0, 2,
    STRENGTH , 0, 3,
    
    ENGINEERING  , 0, 5 ,
    INFORMATICS  , 0, 6 ,
    PHARMACY     , 0, 7 ,
    ANATOMY      , 0, 8 ,
    LAW_N_FINANCE, 0, 9 ,
    THE_OCCULT   , 0, 10,
    
    LANGUAGES    , 1, 0 ,
    QUESTION     , 1, 1 ,
    DISGUISE     , 1, 2 ,
    SUASION      , 1, 3 ,
    
    STEALTH      , 1, 5 ,
    SURVEILLANCE , 1, 6 ,
    VEHICLES     , 1, 7 ,
    MARKSMAN     , 1, 8 ,
    
    INTIMIDATE   , 1, 10,
    GYMNASTICS   , 1, 11,
    CLOSE_COMBAT , 1, 12,
    STAMINA      , 1, 13,
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
    
    Stat hovered = null;
    
    for (Stat t : ALL_STATS) {
      int index = Visit.indexOf(t, STAT_DISPLAY_COORDS);
      if (index == -1) continue;
      
      int level = person.stats.levelFor(t);
      int x = (Integer) STAT_DISPLAY_COORDS[index + 1];
      int y = (Integer) STAT_DISPLAY_COORDS[index + 2];
      x *= 150;
      y *=  20;
      Color forT = Color.WHITE;
      
      if (surface.mouseIn(vx + x + 20, vy + y + 120 + 10, 150, 20, this)) {
        hovered = t;
        forT = Color.YELLOW;
      }
      
      g.setColor(forT);
      g.drawString(t.name  , vx + x + 20      , vy + y + 120 + 25);
      g.drawString(""+level, vx + x + 20 + 100, vy + y + 120 + 25);
    }
    
    if (hovered != null) {
      g.setColor(Color.LIGHT_GRAY);
      
      String desc = "";
      if (hovered.roots.length == 0) {
        
      }
      else {
        desc += "\n  Bonus from: ";
        for (Stat r : hovered.roots) {
          desc += r;
          if (r != Visit.last(hovered.roots)) desc += " plus ";
        }
      }
      desc = hovered.description + desc;
      
      ViewUtils.drawWrappedString(
        desc, g, vx + 20, vy + 120 + (15 * 20), 300, 100
      );
    }
  }
  
  
  
}










