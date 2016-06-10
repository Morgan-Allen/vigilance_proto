

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
  
  
  RegionView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  

  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    District nation = mainView.areaView.selectedNation();
    if (nation == null) return;
    
    //Image portrait = nation.region.view.portrait;
    g.setColor(Color.WHITE);
    g.drawString(nation.region.name, vx + 20, vy + 20);
    //int portW = vw - 120, portH = (int) (portW / 2.5f);
    
    int down = 50;
    for (District.Stat stat : District.CIVIC_STATS) {
      g.drawString(stat+": ", vx + 30, vy + down);
      int max = nation.longTermValue(stat);
      int current = (int) nation.currentValue(stat);
      g.drawString(""+current+"/"+max, vx + 180, vy + down);
      down += 20;
    }
    
    down += 5;
    for (District.Stat stat : District.SOCIAL_STATS) {
      g.drawString(stat+": ", vx + 80, vy + down);
      int current = (int) nation.currentValue(stat);
      g.drawString(""+current, vx + 180, vy + down);
      down += 20;
    }
    
    int income = 0;// nation.baseIncome();
    g.drawString("Income: ", vx + 30, vy + down);
    g.drawString("Expense: ", vx + 130, vy + down);
    
    /*
    //
    //  TODO:  Give a more comprehensive readout here.  Then you'll have a more
    //  reliable basis for making investment decisions.
    int trustPercent = (int) nation.currentValue(District.TRUST   );
    int crimePercent = (int) nation.currentValue(District.VIOLENCE);
    int income       = (int) nation.currentValue(District.INCOME  );
    
    g.drawString("Trust: "   +trustPercent+"%" , vx + 30, vy + 50);
    g.drawString("Violence: "+crimePercent+"%" , vx + 30, vy + 65);
    g.drawString("Income: "  +income           , vx + 30, vy + 80);
    //*/
    
    renderFacilities(nation, surface, g);
    renderLeads(nation, surface, g);
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
  }
  
  
  void renderFacilities(
    final District d, final Surface surface, final Graphics2D g
  ) {
    final int maxF = d.maxFacilities();
    int across = 240, down = 10;
    
    final Vars.Int hoverSlot = new Vars.Int(-1);
    
    for (int n = 0; n < maxF; n++) {
      final int      slot  = n;
      final Facility built = d.builtInSlot(n);
      
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
        void whenClicked() {
          presentBuildOptions(d, slot, surface, g);
        }
        void whenHovered() {
          if (built != null) hoverSlot.val = slot;
        }
      };
      button.refers = built+"_slot_"+n;
      button.updateAndRender(surface, g);
      
      across += 60 + 10;
    }
    
    if (hoverSlot.val != -1) {
      g.setColor(Color.LIGHT_GRAY);
      
      final int      slot  = hoverSlot.val;
      final Facility built = d.builtInSlot(slot);
      final Person   owns  = d.ownerForSlot(slot);
      final float    prog  = d.buildProgress(slot);
      
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
    District d, int slotID, Surface surface, Graphics2D g
  ) {
    final BuildOptionsView options = new BuildOptionsView(mainView, d, slotID);
    mainView.queueMessage(options);
  }
  
  
  void renderLeads(District d, Surface surface, Graphics2D g) {
    g.setColor(Color.LIGHT_GRAY);
    int down = vy + 200;
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
  }
  
  
  
}










