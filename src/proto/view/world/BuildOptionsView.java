

package proto.view.world;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Color;



public class BuildOptionsView extends MessageView {
  
  
  final District d;
  final int slotID;
  Blueprint selected = null;
  
  
  BuildOptionsView(UINode parent, District d, int slotID) {
    super(
      parent, null, "Build Blueprint", "",
      "Begin Construction", "Cancel"
    );
    this.d      = d;
    this.slotID = slotID;
    selected    = d.slotType(slotID);
  }
  
  
  protected void renderContent(Surface surface, Graphics2D g, int optionsSize) {
    
    int minWide = 20, across = minWide, maxWide = (vw / 2) - minWide;
    int down = 50;
    final int BS = 50;
    
    //
    //  TODO:  Allow for purchase, salvage, or redevelopment!
    
    for (final Blueprint f : d.facilitiesAvailable()) {
      
      final int nextAcross = across + BS + 5;
      if (nextAcross >= maxWide) { across = minWide; down += BS + 5; }
      
      final ImageButton button = new ImageButton(
        f.icon(), new Box2D(across, down, BS, BS), this
      ) {
        protected void whenClicked() {
          if (selected == f) selected = null;
          else selected = f;
        }
      };
      button.refers = f;
      button.toggled = selected == f;
      button.renderNow(surface, g);
      
      across += BS + 5;
      if (across >= maxWide) { across = minWide; down += BS + 5; }
    }
    
    down += BS + 5;
    g.setColor(Color.LIGHT_GRAY);
    
    String info =
      "Select a facility to view information and order construction."
    ;
    if (selected != null) info = selected.info();
    
    ViewUtils.drawWrappedString(
      info, g,
      vx + maxWide + 5, vy + 10,
      (vw / 2) - 5, vh - (50 + 10 + optionsSize)
    );
  }
  
  
  protected boolean optionValid(int optionID) {
    //
    //  Cannot confirm construction if no facility is selected.
    if (optionID == 0) {
      final Base base = d.world().playerBase();
      if (selected == null) return false;
      if (d.slotType(slotID) == selected) return false;
      if (! selected.canBuild(base, d)) return false;
    }
    return true;
  }
  
  
  protected void whenClicked(String option, int optionID) {
    //
    //  If confirmed, begin construction.
    if (optionID == 0 && selected != null) {
      final Base base = d.world().playerBase();
      d.beginConstruction(selected, optionID, base);
      mainView.dismissMessage(this);
    }
    //
    //  If cancelled, go back.
    if (optionID == 1) {
      mainView.dismissMessage(this);
    }
  }
  
}







