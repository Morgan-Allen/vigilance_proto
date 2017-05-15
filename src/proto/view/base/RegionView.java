

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
    Region    region = mapRefers.selectedRegion();
    Base      player = mainView.player();
    CasesView parent = mainView.casesView;
    Person    agent  = mainView.rosterView.selectedPerson();
    if (region == null) return false;
    
    g.drawString(region.kind().name(), vx + 20, vy + 20);
    
    Region.Stat hovered = null;
    int across = 10, down = 50;
    
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
    
    
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    
    draw.addEntry(null, "Facilities:", 20, null);
    for (Place p : region.buildSlots()) if (p != null) {
      draw.addEntry(p.icon(), p.name(), 40, p);
    }
    
    /*
    draw.addEntry(null, "Leads:", 20, null);
    for (Lead lead : player.leads.leadsFor(region)) {
      draw.addEntry(lead.icon(), lead.choiceInfo(agent), 20, lead);
    }
    //*/
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










