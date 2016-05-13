


package proto.view;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
//import java.awt.image.BufferedImage;




public class WorldView {
  
  
  final static String IMG_DIR = "media assets/city map/";
  final static int
    PANE_ABILITIES = 0,
    PANE_OUTFIT    = 1,
    PANE_PSYCH     = 2;
  
  final World world;
  
  Object lastSelected;
  int personPaneMode = PANE_ABILITIES;
  
  MapView mapView;
  RosterView rosterView;
  Image alertMarker, selectCircle, constructMark;
  
  
  
  public WorldView(World world) {
    this.world = world;
    mapView    = new MapView   (this, new Box2D(320, 20, 360, 560));
    rosterView = new RosterView(this, new Box2D(20 , 20, 320, 560));
    
    mapView.loadMapImages(
      IMG_DIR+"city_map.png",
      IMG_DIR+"city_districts_key.png"
    );
    alertMarker   = Kind.loadImage(IMG_DIR+"alert_symbol.png");
    selectCircle  = Kind.loadImage(IMG_DIR+"select_circle.png");
    constructMark = Kind.loadImage(Blueprint.IMG_DIR+"under_construction.png");
  }
  
  
  void setSelection(Object selected) {
    this.lastSelected = selected;
  }
  
  
  
  /**  Rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    
    final int wide = surface.getWidth(), high = surface.getHeight();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, wide, high);
    
    mapView.renderTo(surface, g);
    
    //
    //  Then draw the various base-facilities:
    renderBase(surface, g);
    
    //
    //  Then draw the monitor-status active at the moment-
    int x = (wide / 2) + 25, y = high - 10;
    Series <Investigation> events = world.events().active();
    if (world.monitorActive()) {
      g.setColor(Color.ORANGE);
      g.drawString("Monitoring...", x, y);
    }
    else if (! events.empty()) {
      g.setColor(Color.RED);
      for (Investigation event : events) {
        g.drawString("Crime: "+event.name(), x, y);
        y -= 20;
      }
    }
    else {
      g.setColor(Color.BLUE);
      g.drawString("Monitoring paused...", x, y);
    }
    
    //
    //  And finally, draw the roster for the current base!
    rosterView.renderTo(surface, g);
  }
  
  
  void renderBase(Surface surface, Graphics2D g) {
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
  
  
  void renderAssigned(
    Series <Person> assigned, int atX, int atY,
    Surface surface, Graphics2D g
  ) {
    int x = atX, y = atY;
    for (Person p : assigned) {
      g.drawImage(p.kind().sprite(), x, y, 20, 20, null);
      x += 20;
    }
  }
  
  
  String description() {
    final StringBuffer s = new StringBuffer();
    Printout print = world.game().print();
    
    final Base base = world.base();
    final Nation n = mapView.selectedNation;
    final Person p = rosterView.selectedPerson;
    final Room   r = null;// this.selectedRoom;
    
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
    
    if (n != null && n == lastSelected) {
      int trustPercent = (int) (n.trustLevel() * 100);
      int crimePercent = (int) (n.crimeLevel() * 100);
      //Scene crisis = n.currentMission();
      //String crisisName = crisis == null ? "None" : crisis.name();
      
      s.append("\nRegion:  "+n.region.name);
      s.append("\nFunding: "+n.funding()+"K");
      s.append("\nTrust:   "+trustPercent+"%");
      s.append("\nCrime:   "+crimePercent+"%");
      s.append("\nLeague Member: "+n.member());
      
      for (Investigation event : world.events().active()) {
        for (Lead l : event.leadsFrom(n.region)) {
          s.append("\n  Lead: "+l);
        }
      }
    }
    
    else if (p != null && p == lastSelected) {
      if (personPaneMode == PANE_ABILITIES) {
        describePersonAbilities(p, s);
      }
      if (personPaneMode == PANE_OUTFIT) {
        describePersonOutfit(p, s);
      }
      if (personPaneMode == PANE_PSYCH) {
        describePersonPsych(p, s);
      }
      appendPersonPaneOptions(p, s);
    }
    
    s.append("\n");
    s.append("\n  Time: "+world.currentTime());
    
    if (false) {//! world.assignedToMissions().empty()) {
      s.append("\n  Press M to begin missions.");
      if (print.isPressed('m')) {
        world.beginNextMission();
      }
    }
    else if (! world.monitorActive()) {
      s.append("\n  Press M to resume monitoring.");
      if (print.isPressed('m')) world.beginMonitoring();;
    }
    else {
      s.append("\n  Press M to pause monitoring.");
      if (print.isPressed('m')) world.stopMonitoring();
    }
    s.append("\n  Press S to save, R to reload.");
    if (print.isPressed('s')) world.performSave();
    if (print.isPressed('r')) world.reloadFromSave();
    
    return s.toString();
  }
  
  
  void describePersonAbilities(Person p, StringBuffer s) {
    
    s.append("\nCodename: "+p.name());
    int HP = (int) Nums.max(0, p.maxHealth() - (p.injury() + p.stun()));
    s.append("\n  Health: "+HP+"/"+p.maxHealth());
    int SP = (int) Nums.clamp(p.stress(), 0, p.maxStress());
    s.append("\n  Stress: "+SP+"/"+p.maxStress());
    
    if (! p.alive()) s.append(" (Dead)");
    else if (! p.conscious()) s.append(" (Unconscious)");
    else if (p.stun() > 0) s.append(" (Stun "+(int) p.stun()+")");
    
    /*
    int minD = p.stats.levelFor(PersonStats.MIN_DAMAGE);
    int rngD = p.stats.levelFor(PersonStats.RNG_DAMAGE);
    int armr = p.stats.levelFor(PersonStats.ARMOUR    );
    s.append("\n  Damage: "+minD+"-"+(minD + rngD));
    s.append(  "  Armour: "+armr);
    //*/

    s.append("\nStatistics: ");
    for (Trait stat : PersonStats.BASE_STATS) {
      int level = p.stats.levelFor(stat);
      int bonus = p.stats.bonusFor(stat);
      s.append("\n  "+stat.name+": "+level);
      if (bonus > 0) s.append(" (+"+bonus+")");
    }
    
    /*
    s.append("\nAbilities: ");
    for (Ability a : p.stats.listAbilities()) if (! a.basic()) {
      int level = (int) p.stats.levelFor(a);
      s.append("\n  Level "+level+" "+a.name);
    }
    //*/
    
    Assignment assigned = p.assignment();
    String assignName = assigned == null ? "None" : assigned.name();
    s.append("\n\nAssignment: "+assignName);
  }
  
  
  void describePersonOutfit(Person p, StringBuffer s) {
    Printout print = world.game().print();
    
    s.append("\nCodename: "+p.name());
    
    s.append("\nItems equipped:");
    for (Equipped item : p.equipment()) {
      s.append("\n  "+item);
    }
    
    s.append("\nItems available:");
    int index = 0;
    for (Equipped item : world.base().itemsAvailableFor(p)) {
      boolean equipped = p.hasEquipped(item);
      boolean canEquip = p.canEquip   (item);
      s.append("\n  "+item.name+" ("+index+")");
      if      (equipped  ) s.append(" (Equipped)");
      else if (! canEquip) s.append(" (No Slot)" );
      
      if (print.isPressed((char) ('0' + index))) {
        if      (equipped) p.removeItem(item);
        else if (canEquip) p.equipItem (item);
      }
    }
  }
  
  
  void describePersonPsych(Person p, StringBuffer s) {
    //  TODO:  FILL THIS IN!
  }
  
  
  void appendPersonPaneOptions(Person p, StringBuffer s) {
    s.append("\n\nPress A for Abilities, O for Outfit, P for Psych");
    
    Printout print = world.game().print();
    if (print.isPressed('a')) {
      personPaneMode = PANE_ABILITIES;
    }
    if (print.isPressed('o')) {
      personPaneMode = PANE_OUTFIT;
    }
    if (print.isPressed('p')) {
      personPaneMode = PANE_PSYCH;
    }
  }
  
}












