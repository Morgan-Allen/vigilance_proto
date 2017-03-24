

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class HistoryView extends UINode {

  
  final static String friendDescs[] = {
    "Civil", "Friendly", "Close", "Soulmate"
  };
  final static String enemyDescs[] = {
    "Tense", "Unfriendly", "Hostile", "Nemesis"
  };
  
  
  public HistoryView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  public static String bondDescription(Person person, Element other) {
    float value = person.history.bondWith(other);
    String desc = "";
    if (value > 0) desc += friendDescs[Nums.clamp((int) (value *  4), 4)];
    else           desc += enemyDescs [Nums.clamp((int) (value * -4), 4)];
    return desc;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    Person person = this.mainView.rosterView.selectedPerson();
    if (person == null) return false;

    g.setColor(Color.WHITE);
    int down = 10;
    
    for (Element b : person.history.sortedBonds()) if (b.isPerson()) {
      Person other = (Person) b;
      float value = person.history.bondWith(other);
      
      g.drawImage(other.kind().sprite(), vx + 5, vy + down + 5, 40, 40, null);
      boolean hoverP = surface.tryHover(
        vx + 5, vy + down + 5, 40, 40, other, this
      );
      if (hoverP) {
        g.drawImage(mainView.selectCircle, vx + 5, vy + down + 5, 40, 40, null);
        if (surface.mouseClicked()) {
          mainView.rosterView.setSelectedPerson(other);
        }
      }
      
      g.setColor(Color.WHITE);
      String desc = other.name() + bondDescription(person, other);
      g.drawString(desc, vx + 5 + 40 + 4, vy + down + 5 + 15);
      
      Color tint = Color.BLUE, back = Color.DARK_GRAY;
      if (value < 0) { value *= -1; tint = Color.RED; }
      
      ViewUtils.renderStatBar(
        vx + 40 + 5 + 5, vy + down + 5 + 20, 200, 20,
        tint, back, value, false, g
      );
      
      down += 40 + 5;
    }
    
    return true;
  }
  
  
}














