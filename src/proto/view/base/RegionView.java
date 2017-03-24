

package proto.view.base;
import proto.common.*;
import proto.game.event.*;
import proto.game.world.*;

import proto.util.*;
import proto.view.common.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RegionView extends UINode {
  
  
  final static Image
    NOT_BUILT = Kind.loadImage(
      "media assets/tech icons/state_not_built.png"
    ),
    IN_PROGRESS = Kind.loadImage(
      "media assets/tech icons/state_in_progress.png"
    );
  
  
  final MapInsetView mapRefers;
  
  
  public RegionView(UINode parent, MapInsetView map, Box2D viewBounds) {
    super(parent, viewBounds);
    this.mapRefers = map;
  }
  
  

  /**  Actual rendering methods-
    */
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    final Region region = mapRefers.selectedRegion();
    if (region == null) {
      //  TODO:  Render some help-text instead!
      return false;
    }
    
    g.drawString(region.kind().name(), vx + 20, vy + 20);
    
    Region.Stat hovered = null;
    
    int down = 50;
    for (Region.Stat stat : Region.CIVIC_STATS) {
      if (surface.tryHover(vx, vy + down - 10, 120, 20, stat, this)) {
        hovered = stat;
        g.setColor(Color.YELLOW);
      }
      else g.setColor(Color.WHITE);
      g.drawString(stat+": ", vx + 30, vy + down);
      int current = (int) region.longTermValue(stat);
      g.drawString(""+current+"/10", vx + 120, vy + down);
      down += 20;
    }
    
    down = 50;
    for (Region.Stat stat : Region.SOCIAL_STATS) {
      if (surface.tryHover(vx + 160, vy + down - 10, 120, 20, stat, this)) {
        hovered = stat;
        g.setColor(Color.YELLOW);
      }
      else g.setColor(Color.WHITE);
      g.drawString(stat+": ", vx + 160, vy + down);
      int current = (int) region.currentValue(stat);
      g.drawString(""+current, vx + 250, vy + down);
      down += 20;
    }
    
    renderFacilities(region, surface, g);
    
    if (hovered != null) {
      g.setColor(Color.LIGHT_GRAY);
      String desc = hovered.description;
      ViewUtils.drawWrappedString(
        desc, g, vx + 25, vy + 190, 275, 500
      );
    }
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    
    return true;
  }
  
  
  void renderFacilities(
    final Region d, final Surface surface, final Graphics2D g
  ) {
    final int maxF = d.maxFacilities();
    int across = 25, down = 125;
    int hoverSlotID = -1;
    
    for (int n = 0; n < maxF; n++) {
      final Place     slot  = d.buildSlot(n);
      final PlaceType built = slot == null ? null : slot.kind();
      final float     prog  = slot == null ? 0 : slot.buildProgress();
      final int slotID = n;
      
      Image icon = null;
      if (built == null) {
        icon = NOT_BUILT;
      }
      else {
        icon = built.icon();
      }
      
      final ImageButton button = new ImageButton(
        icon, new Box2D(across, down, 60, 60), this
      ) {
        protected void whenClicked() {
          presentBuildOptions(d, slotID, surface, g);
        }
      };
      button.refers = built+"_slot_"+slotID;
      if (prog < 1 && built != null) button.attachOverlay(IN_PROGRESS);
      button.renderNow(surface, g);
      if (surface.wasHovered(button.refers)) hoverSlotID = n;
      
      across += 60 + 10;
    }
    
    if (hoverSlotID != -1 && d.buildSlot(hoverSlotID) == null) {
      g.setColor(Color.LIGHT_GRAY);
      String desc = "Vacant Site\n  (Click to begin construction)";
      ViewUtils.drawWrappedString(
        desc, g, vx + 25, vy + 190, 275, 500
      );
    }
    else if (hoverSlotID != -1) {
      g.setColor(Color.LIGHT_GRAY);
      
      final Place     slot  = d.buildSlot(hoverSlotID);
      final PlaceType built = slot.kind();
      final Base      owns  = slot.owner();
      final float     prog  = slot.buildProgress();
      
      String desc = "";
      desc += built.defaultInfo();
      desc += "\nInvestor: "+owns.faction().name;
      if (prog < 1) desc += " ("+((int) (prog * 100))+"% complete)";
      
      ViewUtils.drawWrappedString(
        desc, g, vx + 25, vy + 190, 275, 500
      );
    }
  }
  
  
  void presentBuildOptions(
    Region d, int slotID, Surface surface, Graphics2D g
  ) {
    final BuildOptionsView options = new BuildOptionsView(mainView, d, slotID);
    mainView.queueMessage(options);
  }
  
  
  
}










