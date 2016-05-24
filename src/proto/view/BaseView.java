

package proto.view;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Graphics2D;



public class BaseView {
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  private Object selectedArea;
  private Assignment selectedTask;
  
  
  BaseView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent;
    this.viewBounds = viewBounds;
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    
    final Base base = parent.world.base();

    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    int down = 60;
    
    for (Room room : base.rooms()) if (room != null) {
      g.drawImage(room.icon(), vx + 10, vy + 20 + down, 60, 60, null);
      boolean hover = surface.mouseIn(vx + 10, vy + 20 + down, 60, 60, this);
      
      if (hover || selectedArea == room) {
        g.drawImage(parent.selectSquare, vx + 10, vy + 20 + down, 60, 60, null);
      }
      
      if (hover && surface.mouseClicked(this)) {
        selectedArea = room;
      }
      
      ViewUtils.renderAssigned(
        room.visitors(), vx + 60 + 10, vy + down + 20 + 60, surface, g
      );
      down += 60 + 10;
    }
    
    
    /*
    Base base = world.base();
    
    int offX = 23, offY = 399, slotX = 0, slotY = 0, index = 0;
    int x, y, w, h;
    Room roomHovered = null;
    
    if (roomHovered != null && surface.mouseClicked) {
      this.selectedRoom = roomHovered;
      this.lastSelected = roomHovered;
    }
    
    offX =  5;
    offY = 15;
    String report = "";
    report += " Week: "+world.currentTime();
    report += " Funds: "+base.currentFunds()+"";
    report += " Income: "+base.income()+"";
    /*
    int senseChance = (int) (base.sensorChance() * 100);
    report += " Sensors: "+senseChance+"%";
    report += " Engineering: "+base.engineerForce();
    report += " Research: "+base.researchForce();
    //*/
    /*
    g.setColor(Color.BLUE);
    g.drawString(report, offX, offY);
    
    offX =  5;
    offY = 30;
    report = "";
    report += " Power: "+base.powerUse()+"/"+base.maxPower();
    report += " Life Support: "+base.supportUse()+"/"+base.maxSupport();
    report += " Maintenance: "+base.maintenance()+"";
    g.setColor(Color.BLUE);
    g.drawString(report, offX, offY);
    //*/
  }
  
  
  void setSelection(Object selected) {
    this.selectedArea = selected;
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
    final Person active = parent.rosterView.selected();
    if (active == null) return;
    final Assignment oldTask = active.assignment();
    
    if (active != null && oldTask != null) {
      oldTask.setAssigned(active, false);
    }
    if (active != null && task != null && task != oldTask) {
      task.setAssigned(active, true);
    }
  }
  
  
  Assignment selectedTask() {
    return selectedTask;
  }
}





