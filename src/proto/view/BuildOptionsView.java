

package proto.view;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Color;



public class BuildOptionsView extends MessageView {
  
  
  final District d;
  final int slotID;
  Facility selected = null;
  
  
  BuildOptionsView(UINode parent, District d, int slotID) {
    super(
      parent, null, "Build Facility", "",
      "Begin Construction", "Cancel"
    );
    this.d      = d;
    this.slotID = slotID;
  }
  
  
  protected void renderContent(Surface surface, Graphics2D g, int optionsSize) {
    
    int minWide = 20, across = minWide, maxWide = vw - (minWide + 20);
    int down = 50;
    
    for (final Facility f : d.facilitiesAvailable()) {
      //
      //  TODO:  Allow for purchase, salvage, or redevelopment?
      
      final ImageButton button = new ImageButton(
        f.icon(), new Box2D(across, down, 60, 60), this
      ) {
        
        void whenClicked() {
          if (selected == f) selected = null;
          else selected = f;
        }
      };
      button.refers = f;
      button.toggled = selected == f;
      button.updateAndRender(surface, g);
      
      across += 60 + 5;
      if (across >= maxWide) { across = minWide; down += 40 + 5; }
    }
    
    down += 60 + 5;
    g.setColor(Color.LIGHT_GRAY);
    
    String info =
      "Select a facility to view information and order construction."
    ;
    if (selected != null) info = selected.info();

    ViewUtils.drawWrappedString(
      info, g, vx + 20, vy + down + 10, vw - 40, 120
    );
  }
  
  
  protected boolean optionValid(int optionID) {
    //
    //  Cannot confirm construction if no facility is selected.
    if (optionID == 0 && selected == null) return false;
    return true;
  }
  
  
  protected void whenClicked(String option, int optionID) {
    //
    //  If confirmed, begin construction.
    if (optionID == 0 && selected != null) {
      final Base base = d.world.base();
      d.beginConstruction(selected, base.leader(), slotID);
    }
    //
    //  If cancelled, go back.
    if (optionID == 1) {
      mainView.dismissMessage(this);
    }
  }
  
}







