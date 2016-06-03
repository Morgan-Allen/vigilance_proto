

package proto.view;
import proto.game.scene.*;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RegionView extends UINode {
  
  

  final static Image
    NOT_BUILT   = Kind.loadImage(
      "media assets/tech icons/state_not_built.png"
    ),
    IN_PROGRESS = Kind.loadImage(
      "media assets/tech icons/state_in_progress.png"
    );
  
  Lead selectedLead;
  int lastBuildSlot = -1;
  
  
  
  RegionView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  

  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    District nation = mainView.areaView.selectedNation();
    if (nation == null) return;
    
    Image portrait = nation.region.view.portrait;
    g.setColor(Color.WHITE);
    g.drawString(nation.region.name, vx + 20, vy + 20);
    int portW = vw - 120, portH = (int) (portW / 2.5f);
    
    //
    //  TODO:  Give a more comprehensive readout here!
    int trustPercent = (int) nation.currentValue(District.TRUST   );
    int crimePercent = (int) nation.currentValue(District.VIOLENCE);
    int income       = (int) nation.currentValue(District.INCOME  );
    
    g.drawString("Trust: "   +trustPercent+"%" , vx + 30, vy + 50);
    g.drawString("Violence: "+crimePercent+"%" , vx + 30, vy + 65);
    g.drawString("Income: "  +income           , vx + 30, vy + 80);
    
    renderFacilities(nation, surface, g);
    
    if (lastBuildSlot != -1 && false) {
      renderBuildOptions(nation, surface, g);
    }
    else {
      renderLeads(nation, surface, g);
    }
  }
  
  
  void renderFacilities(District d, Surface surface, Graphics2D g) {
    final int maxF = d.maxFacilities();
    int across = 0;
    
    for (int n = 0; n < maxF; n++) {
      final Facility built = d.builtInSlot  (n);
      final Person   owns  = d.ownerForSlot (n);
      final float    prog  = d.buildProgress(n);
      
      Image icon = null;
      if (built == null) {
        icon = NOT_BUILT;
      }
      else {
        icon = built.icon();
      }
      
      final boolean hovers = surface.tryHover(
        vx + 120 + across, vy + 20, 60, 60, built+"_slot_"+n
      );
      g.drawImage(icon, vx + 120 + across, vy + 20, 60, 60, null);
      
      if (hovers) {
        g.setColor(Color.YELLOW);
        g.drawRect(vx + 120 + across, vy + 20, 60, 60);
      }
      if (hovers && surface.mouseClicked()) {
        lastBuildSlot = n;
      }
      
      across += 60 + 10;
    }
  }
  
  
  void renderBuildOptions(District d, Surface surface, Graphics2D g) {
    
    final int n = lastBuildSlot;
    final Facility built = d.builtInSlot  (n);
    final Person   owns  = d.ownerForSlot (n);
    final float    prog  = d.buildProgress(n);
    
    int down = 100;
    
    if (built != null) {
      ViewUtils.drawWrappedString(
        built.info(), g, vx + 20, vy + down, vw - 40, 60
      );
    }
    else {
      int across = 20, maxWide = vw - 40;
      for (Facility f : d.facilitiesAvailable()) {
        g.drawImage(f.icon(), vx + across, vy + down, 60, 60, null);
        across += 60 + 10;
        if (across >= maxWide) { across = 0; down += 60 + 10; }
      }
    }
  }
  
  
  
  void renderLeads(District d, Surface surface, Graphics2D g) {
    g.setColor(Color.LIGHT_GRAY);
    int down = vy + 100;
    boolean noEvents = true;
    
    for (Event event : mainView.world.events().active()) {
      //
      //  Firstly, check to see if there's an event occurring in this region:
      if (event.region() != d.region) continue;
      final Series <Lead> leads = event.knownOpenLeads();
      if (leads.empty()) continue;
      noEvents = false;
      int initDown = down;
      //
      //  If so, draw the name and info for the investigation as a whole:
      g.drawString(event.name(), vx + 20, down + 15);
      down += 20;
      ViewUtils.drawWrappedString(
        event.info(), g, vx + 20, down, vw - 40, 60
      );
      down += 60;
      //
      //  Then, draw info for any individual leads:
      //  TODO:  Should I be using attachment/detachment here?
      for (Lead l : leads) {
        TaskView view = l.createView(parent);
        view.relBounds.set(vx, down, vw, 65);
        view.updateAndRender(surface, g);
        down += view.relBounds.ydim() + 10;
      }
      g.setColor(Color.DARK_GRAY);
      g.drawRect(vx + 10, initDown, vw - 20, down - initDown);
    }
    
    if (noEvents) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "You have no intelligence on any major crimes in this district.",
        g, vx + 25, down + 20, vw - 30, 150
      );
    }
    else if (true) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select a task, then click in the bottom-left of roster portraits to "+
        "assign agents to the task.",
        g, vx + 25, down + 20, vw - 30, 150
      );
    }
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
  }
  
  
  
}










