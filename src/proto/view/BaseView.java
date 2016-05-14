

package proto.view;
import proto.util.*;
import java.awt.Graphics2D;



public class BaseView {
  
  
  
  BaseView(WorldView parent, Box2D viewBounds) {
    
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
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
  
  
  String describeBase() {
    /*
    if (r != null && r == lastSelected) {
      if (r.blueprint() == Blueprint.NONE) {
        s.append("\nInstall Facility (Press 1-9):");
        char key = '1';
        boolean canBuild = false;
        for (Blueprint b : Blueprint.ALL_BLUEPRINTS) {
          s.append("\n  "+b.name);
          if (! base.canConstruct(b, r.slotIndex)) continue;
          s.append(" ("+key+") (Cost "+b.buildCost()+")");
          canBuild = true;
          if (print.isPressed(key)) {
            base.beginConstruction(b, r.slotIndex);
          }
          key++;
        }
        if (! canBuild) s.append("\n  Cannot afford construction");
      }
      else {
        Blueprint b = r.blueprint();
        s.append("\nFacility: "+b.name);
        if (b.maintenance() != 0) {
          s.append("\n  Maintenance: "+b.maintenance());
        }
        if (b.lifeSupport() != 0) {
          s.append("\n  Life support: "+b.lifeSupport());
        }
        if (b.powerCost() > 0) {
          s.append("\n  Power cost: "+b.powerCost());
        }
        if (b.powerCost() < 0) {
          s.append("\n  Power supply: "+(0 - b.powerCost()));
        }
        
        if (r.buildProgress() != 1) {
          int bP = (int) (r.buildProgress() * 100);
          int ETA = base.buildETA(r.slotIndex);
          s.append("\n  Construction: "+bP+"% (ETA "+ETA+" weeks)");
          s.append("\n"+b.description);
        }
        else {
          s.append("\n"+b.description);
          s.append("\n");
          int numV = r.visitors().size(), maxV = r.blueprint().visitLimit();
          s.append("\nVisitors ("+numV+"/"+maxV+")");
          s.append(" (Press 1-9 to assign)");
          Person picks = null;
          for (int i = 1; i <= 9; i++) {
            Person q = base.atRosterIndex(i -1);
            if (! r.allowsAssignment(q)) continue;
            if (print.isPressed((char) ('0' + i))) {
              picks = q;
            }
          }
          for (Person u : r.visitors()) {
            s.append("\n  "+u.name());
          }
          if (picks != null) {
            if (picks.freeForAssignment() && r.allowsAssignment(picks)) {
              r.setAssigned(picks, true);
            }
            else if (picks.assignment() == r) r.setAssigned(picks, false);
          }
        }
        
        s.append("\n\n  Press X to salvage.");
        if (print.isPressed('x')) {
          base.beginSalvage(r.slotIndex);
        }
      }
    }
    //*/
    
    return null;
  }
  
}





