

package proto.view.base;
import proto.game.world.*;
import proto.game.person.*;
import proto.view.common.*;

import java.awt.Graphics2D;




public class RosterPickView extends UINode {
  
  
  Assignment assignTo;
  
  
  public RosterPickView(UINode parent, Assignment assignTo) {
    super(parent);
    this.assignTo = assignTo;
  }
  
  
  
  //  TODO:  Use a ClickMenu for this?   ...Maybe.
  
  
  protected void updateAndRender(Surface surface, Graphics2D g) {
    
    int prefWide = 0;
    for (Person p : mainView.player().roster()) {
      prefWide += 30 + 10;
    }
    
    relBounds.set(10, 10, prefWide, 30 + 10);
    
    // TODO Auto-generated method stub
    super.updateAndRender(surface, g);
  }





  protected boolean renderTo(Surface surface, Graphics2D g) {
    return super.renderTo(surface, g);
  }
  
  
}









