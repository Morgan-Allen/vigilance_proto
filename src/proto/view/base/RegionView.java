

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
  
  
  //final ActivityView parent;
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
    
    final Base base = mainView.world().playerBase();
    g.setColor(Color.WHITE);
    g.drawString(region.kind().name(), vx + 20, vy + 20);
    
    int down = 50;
    for (Region.Stat stat : Region.CIVIC_STATS) {
      g.drawString(stat+": ", vx + 30, vy + down);
      int max = region.longTermValue(stat);
      int current = (int) region.currentValue(stat);
      g.drawString(""+current+"/"+max, vx + 180, vy + down);
      down += 20;
    }
    
    down += 5;
    for (Region.Stat stat : Region.SOCIAL_STATS) {
      g.drawString(stat+": ", vx + 80, vy + down);
      int current = (int) region.currentValue(stat);
      g.drawString(""+current, vx + 180, vy + down);
      down += 20;
    }
    
    int income = region.incomeFor(base), expense = region.expensesFor(base);
    g.drawString("Income: " +income , vx + 30 , vy + down);
    g.drawString("Expense: "+expense, vx + 130, vy + down);
    
    renderFacilities(region, surface, g);
    ///renderLeads(region, surface, g);
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    
    return true;
  }
  
  
  void renderFacilities(
    final Region d, final Surface surface, final Graphics2D g
  ) {
    final int maxF = d.maxFacilities();
    int across = 240, down = 10;
    final Vars.Int hoverSlot = new Vars.Int(-1);
    
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
        protected void whenHovered() {
          if (built != null) hoverSlot.val = slotID;
        }
      };
      button.refers = built+"_slot_"+slotID;
      if (prog < 1 && built != null) button.attachOverlay(IN_PROGRESS);
      button.renderNow(surface, g);
      
      down += 60 + 10;
    }
    
    if (hoverSlot.val != -1) {
      g.setColor(Color.LIGHT_GRAY);
      
      final Place     slot  = d.buildSlot(hoverSlot.val);
      final PlaceType built = slot.kind();
      final Base      owns  = slot.owner();
      final float     prog  = slot.buildProgress();
      
      String desc = "";
      desc += built.name();
      if (prog < 1) desc += " ("+((int) (prog * 100))+"% complete)";
      
      desc += "\nOwned by: "+owns;
      desc += "\n\n";
      desc += built.statInfo();
      
      ViewUtils.drawWrappedString(
        desc, g,
        vx + 240, vy + 10 + 60 + 10,
        vw - 240, 200 - 70
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










