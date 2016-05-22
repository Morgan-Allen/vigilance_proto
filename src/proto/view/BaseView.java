

package proto.view;
import proto.game.world.*;
import proto.util.*;

import java.awt.Graphics2D;



public class BaseView {
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  Object selectedArea;
  //Room selectedRoom;
  
  
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
        if (room == parent.lastSelected) selectedArea = null;
        parent.setSelection(selectedArea);
      }
      
      down += 60 + 10;
    }
    
    
    /*
    Base base = world.base();
    
    int offX = 23, offY = 399, slotX = 0, slotY = 0, index = 0;
    int x, y, w, h;
    Room roomHovered = null;
    
    for (Room r : base.rooms()) {
      slotX = index % Base.SLOTS_WIDE;
      slotY = index / Base.SLOTS_WIDE;
      index++;
      x = offX + (slotX * 82);
      y = offY + (slotY * 40);
      w = 72;
      h = 35;
      
      Image sprite = r.buildProgress() < 1 ?
        constructMark : r.blueprint().sprite
      ;
      if (r != null && r.blueprint() != Blueprint.NONE) g.drawImage(
        sprite, x, y, w, h, null
      );
      if (surface.mouseIn(x, y, w, h)) {
        roomHovered = r;
      }
      if (r == roomHovered || r == selectedRoom) {
        g.drawImage(selectCircle, x - 10, y - 5, w + 20, h + 10, null);
      }
      renderAssigned(r.visitors(), x + 5, y + 5, surface, g);
    }
    
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
  
}





