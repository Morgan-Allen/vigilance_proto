

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class PersonInsetView extends UINode {
  
  
  public PersonInsetView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;

    Person person = mainView.rosterView.selectedPerson();
    if (person == null) return false;
    
    g.setColor(Color.WHITE);
    g.drawImage(person.kind().sprite(), vx, vy, 120, 120, null);
    
    g.drawString("Codename: "+person.name(), vx + 125, vy + 20);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      person.history.summary(), g, vx + 125, vy + 20, vw - (120 + 10), 150
    );
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    
    return true;
  }
  
  
}














