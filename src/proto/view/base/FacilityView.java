

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class FacilityView extends UINode {
  
  
  public FacilityView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    return true;
  }
  
  
}














