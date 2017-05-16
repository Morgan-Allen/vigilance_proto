

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
    
    MapInsetView mapRefers = mainView.casesView.mapView;
    Region  region = mapRefers.selectedRegion();
    Base    player = mainView.player();
    MapView parent = mainView.casesView;
    Person  agent  = mainView.rosterView.selectedPerson();
    if (region == null) return false;
    
    Region.Stat hovered = null;
    int across = 10, down = 10;
    
    g.setColor(Color.WHITE);
    g.drawString(region.kind().name(), vx + across + 0, vy + down + 0);
    down = 30;
    
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
    
    down = 30;
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
    
    down += 10;
    for (int i : Visit.range(0, descVals.length)) {
      g.drawString(""+sumDescs[i]    , vx + across + 20 , vy + down);
      g.drawString(""+descVals[i]+"m", vx + across + 160, vy + down);
      down += 20;
    }
    g.drawRect(vx + across + 20, vy + down - 36, 240, 0);
    
    
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    draw.addEntry(null, "Facilities:", 20, null);
    
    //  TODO:  Use slot-IDs here instead, and arrange horizontally.
    
    for (Place p : region.buildSlots()) if (p != null) {
      draw.addEntry(p.icon(), p.name(), 40, p);
    }
    
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    
    
    String hoverDesc = "";
    if (hovered != null) {
      hoverDesc = hovered.description;
    }
    else if (draw.hovered instanceof Lead) {
      Lead lead = (Lead) draw.hovered;
      hoverDesc = lead.testInfo(agent);
      if (draw.clicked) {
        if (agent.assignments().includes(lead)) agent.removeAssignment(lead);
        else agent.addAssignment(lead);
      }
    }
    else if (draw.hovered instanceof Place) {
      Place built = (Place) draw.hovered;
      float prog = built.buildProgress();
      
      hoverDesc = "";
      hoverDesc += built.kind().defaultInfo();
      hoverDesc += "\nInvestor: "+built.owner().faction().name;
      if (prog < 1) hoverDesc += " ("+((int) (prog * 100))+"% complete)";
      
      if (draw.clicked) {
        //parent.setActiveFocus(draw.hovered, false);
      }
    }
    
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, 275, 250
    );
    down += 250;
    
    //parent.casesArea.setScrollheight(down);
    return true;
  }
  
  
  void presentBuildOptions(
    Region d, int slotID, Surface surface, Graphics2D g
  ) {
    final BuildOptionsView options = new BuildOptionsView(mainView, d, slotID);
    mainView.queueMessage(options);
  }
  
  
  
}










