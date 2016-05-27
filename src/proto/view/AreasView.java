

package proto.view;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class AreasView extends UINode {
  
  
  final MapView mapView;
  final StringButton monitorButton;
  
  private Object selectedArea;
  private Assignment selectedTask;
  
  
  AreasView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    mapView = new MapView(this, new Box2D(
      80, 60,
      viewBounds.xdim() - 80, viewBounds.ydim() - 240
    ));
    monitorButton = new StringButton(
      "Resume Monitoring", new Box2D(0, 35, viewBounds.xdim(), 20), this
    ) {
      void whenClicked() {
        final boolean active = mainView.world.monitorActive();
        if (active) mainView.world.pauseMonitoring();
        else        mainView.world.beginMonitoring();
      }
    };
    
    addChildren(mapView, monitorButton);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    
    final World world = mainView.world;
    final Base  base  = world.base();
    
    int down = 60;
    
    for (Room room : base.rooms()) if (room != null) {
      g.drawImage(room.icon(), vx + 10, vy + down, 60, 60, null);
      boolean hover = surface.tryHover(vx + 10, vy + down, 60, 60, room);
      
      if (hover || selectedArea == room) {
        g.drawImage(mainView.selectSquare, vx + 10, vy + down, 60, 60, null);
      }
      
      if (hover && surface.mouseClicked()) {
        selectedArea = room;
      }
      
      ViewUtils.renderAssigned(
        room.visitors(), vx + 60 + 10, vy + down + 60, surface, g
      );
      down += 60 + 10;
    }
    
    //  TODO:  Include a funding report!
    
    /*
    offX =  5;
    offY = 15;
    String report = "";
    report += " Week: "+world.currentTime();
    report += " Funds: "+base.currentFunds()+"";
    report += " Income: "+base.income()+"";
    
    g.setColor(Color.BLUE);
    g.drawString(report, offX, offY);
    //*/
  }
  
  
  void setSelection(Object selected) {
    final Object old = selectedArea;
    this.selectedArea = selected;
    if (old != selected) selectedTask = null;
  }
  
  
  Room selectedRoom() {
    if (selectedArea instanceof Room) return (Room) selectedArea;
    return null;
  }
  
  
  Nation selectedNation() {
    if (selectedArea instanceof Nation) return (Nation) selectedArea;
    return null;
  }
  
  
  void setSelectedTask(Assignment task) {
    this.selectedTask = task;
    /*
    final Person active = mainView.rosterView.selected();
    if (active == null) return;
    final Assignment oldTask = active.assignment();
    
    if (active != null && oldTask != null) {
      oldTask.setAssigned(active, false);
    }
    if (active != null && task != null && task != oldTask) {
      task.setAssigned(active, true);
    }
    //*/
  }
  
  
  Assignment selectedTask() {
    return selectedTask;
  }
}





