

package proto.view.base;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
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
  
  
  public RegionView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  
  /**  Actual rendering methods-
    */
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    MapInsetView mapRefers = mainView.mapView.mapView;
    Region  region = mapRefers.selectedRegion();
    Base    player = mainView.player();
    MapView parent = mainView.mapView;
    if (region == null) return false;
    
    Region.Stat hovered = null;
    int across = 10, down = 10;
    Region.Stat ref = null;
    
    //
    //  First render the name of the region:
    g.setColor(Color.WHITE);
    g.drawString(region.kind().name(), vx + across + 0, vy + down + 0);
    down += 20;
    
    //
    //  Then render deterrence:
    ref = Region.DETERRENCE;
    g.setColor(Color.WHITE);
    if (surface.tryHover(vx + 20, vy + down - 10, 120, 20, ref, this)) {
      hovered = ref;
      g.setColor(Color.YELLOW);
    }
    g.drawString("Deterrence:", vx + across + 20, vy + down);
    
    float deterLevel = region.longTermValue(Region.DETERRENCE) / 100f;
    ViewUtils.renderStatBar(
      vx + across + 100, vy + down - 10,
      180, 10, Color.YELLOW, Color.DARK_GRAY, deterLevel, false, g
    );
    down += 20;
    
    //
    //  Then render trust:
    ref = Region.TRUST;
    g.setColor(Color.WHITE);
    if (surface.tryHover(vx + 20, vy + down - 10, 120, 20, ref, this)) {
      hovered = ref;
      g.setColor(Color.YELLOW);
    }
    g.drawString("Trust:", vx + across + 20, vy + down);
    
    float trustLevel = region.longTermValue(Region.TRUST) / 100f;
    ViewUtils.renderStatBar(
      vx + across + 100, vy + down - 10,
     180, 10, Color.BLUE, Color.DARK_GRAY, trustLevel, false, g
    );
    down += 20;
    
    //
    //  Then render other civic stats:
    down = 70;
    
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
    
    Region.IncomeReport r = region.incomeReport(player);
    String sumDescs[] = {
      "Revenue:",
      "Violence:",
      "Corruption:",
      "Expenses:",
      "Total Profit:"
    };
    Object descVals[] = {
      r.income,
      0 - r.lossViolence,
      0 - r.lossCorruption,
      0 - r.expense,
      r.profit
    };
    
    down = 70;
    for (int i : Visit.range(0, descVals.length)) {
      if (surface.tryHover(vx + 160, vy + down - 10, 120, 20, null, this)) {
        hovered = null;
        g.setColor(Color.YELLOW);
      }
      else g.setColor(Color.WHITE);
      g.drawString(""+sumDescs[i]    , vx + across + 160, vy + down);
      g.drawString(""+descVals[i]+"m", vx + across + 240, vy + down);
      down += 20;
    }
    g.drawRect(vx + across + 20, vy + down - 36, 270, 0);
    
    
    g.setColor(Color.WHITE);
    g.drawString("Facilities", vx + across + 10, vy + down + 10);
    down += 15;
    
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    for (Integer slotID : Visit.range(0, region.maxFacilities())) {
      Place p = region.buildSlot(slotID);
      Image icon = p == null ? NOT_BUILT : p.icon();
      Object entry = draw.addEntry(icon, null, 60, slotID);
      if (p != null && p.buildProgress() < 1) {
        draw.attachOverlay(entry, IN_PROGRESS);
      }
    }
    draw.performHorizontalDraw(across + 20, down, this, surface, g);
    down = draw.down + 10;
    
    
    String hoverDesc = "";
    if (hovered != null) {
      hoverDesc = hovered.description;
    }
    else if (draw.hovered instanceof Integer) {
      int   slotID = (Integer) draw.hovered;
      Place built  = region.buildSlot(slotID);
      Base  owner  = built == null ? null : built.base();
      
      if (built != null) {
        float prog = built.buildProgress();
        hoverDesc = "";
        hoverDesc += built.kind().defaultInfo();
        hoverDesc += "\nInvestor: "+built.base().faction().name;
        if (prog < 1) hoverDesc += " ("+((int) (prog * 100))+"% complete)";
      }
      else {
        hoverDesc = "Vacant Site (click to begin construction)";
      }
      
      if (draw.clicked && owner == player.world().council.city()) {
        presentCivicMessage(region, slotID);
      }
      else if (draw.clicked && owner == null) {
        presentBuildOptions(region, slotID);
      }
      else if (draw.clicked && owner != player) {
        presentRivalMessage(region, slotID);
      }
      else if (draw.clicked && owner == player) {
        presentDemolishDialog(region, slotID);
      }
    }
    
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, 275, 250
    );
    down += 250;
    
    parent.infoArea.setScrollheight(down);
    return true;
  }
  
  
  void presentCivicMessage(final Region d, final int slotID) {
    final MessageView dialog = new MessageView(
      this, null, "Civic Structure",
      "This structure is owned by the city council, and cannot be demolished "+
      "or refurbished.",
      "Cancel"
    ) {
      protected void whenClicked(String option, int optionID) {
        if (optionID == 0) {
          mainView.dismissMessage(this);
        }
      }
    };
    mainView.queueMessage(dialog);
  }
  
  
  void presentRivalMessage(final Region d, final int slotID) {
    final MessageView dialog = new MessageView(
      this, null, "Civic Structure",
      "This structure is owned by a rival crime boss.  Look for any signs of "+
      "criminal involvement on their part, and build a case toward forcing "+
      "them out of business.",
      "Cancel"
    ) {
      protected void whenClicked(String option, int optionID) {
        if (optionID == 0) {
          mainView.dismissMessage(this);
        }
      }
    };
    mainView.queueMessage(dialog);
  }
  
  
  void presentDemolishDialog(final Region d, final int slotID) {
    final MessageView dialog = new MessageView(
      this, null, "Confirm Demolition?",
      "In order to begin construction of new facilities, you will have to "+
      "demolish the current structure.  Are you sure you want to do this?",
      "Proceed",
      "Cancel"
    ) {
      protected void whenClicked(String option, int optionID) {
        if (optionID == 0) {
          mainView.dismissMessage(this);
          presentBuildOptions(d, slotID);
        }
        if (optionID == 1) {
          mainView.dismissMessage(this);
        }
      }
    };
    mainView.queueMessage(dialog);
  }
  
  
  void presentBuildOptions(Region d, int slotID) {
    final BuildOptionsView options = new BuildOptionsView(mainView, d, slotID);
    mainView.queueMessage(options);
  }
  
  
  
}










