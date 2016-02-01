


package proto.view;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import proto.*;
import proto.common.Kind;
import proto.game.person.Ability;
import proto.game.person.Person;
import proto.game.scene.Scene;
import proto.game.world.Assignment;
import proto.game.world.Base;
import proto.game.world.Blueprint;
import proto.game.world.Nation;
import proto.game.world.Room;
import proto.game.world.World;
import proto.util.*;




public class WorldView {
  
  
  final static String IMG_DIR = "media assets/world map/";
  
  final World world;
  
  Nation selectedNation;
  Person selectedPerson;
  Room   selectedRoom;
  Object lastSelected;
  
  Image mapImage, alertMarker, selectCircle, constructMark;
  
  
  public WorldView(World world) {
    this.world = world;
    loadMedia();
  }
  
  
  void loadMedia() {
    mapImage     = Kind.loadImage(IMG_DIR+"world_map_image.png");
    alertMarker  = Kind.loadImage(IMG_DIR+"alert_symbol.png"   );
    selectCircle = Kind.loadImage(IMG_DIR+"select_circle.png"  );
    constructMark = Kind.loadImage(Blueprint.IMG_DIR+"under_construction.png");
  }
  

  void renderTo(Surface surface, Graphics2D g) {
    Nation nations[] = world.nations();
    
    
    //
    //  Draw the background image first!
    g.drawImage(mapImage, 0, 0, surface.getWidth(), surface.getHeight(), null);
    Nation nationHovered = null;
    Person personHovered = null;
    
    //
    //  Then draw the nations of the world on the satellite map.
    int viewWide = surface.getWidth(), viewHigh = surface.getHeight();
    float mapWRatio = 1, mapHRatio = 1;
    mapWRatio *= mapImage.getWidth (null) * 1f / viewWide;
    mapHRatio *= mapImage.getHeight(null) * 1f / viewHigh;
    
    int mX = surface.mouseX, mY = surface.mouseY;
    mX *= mapWRatio;
    mY *= mapHRatio;
    int pixVal = ((BufferedImage) mapImage).getRGB(mX, mY);
    for (Nation n : nations) if (n.region.view.colourKey == pixVal) {
      nationHovered = n;
    }
    
    if (nationHovered != null && surface.mouseClicked) {
      this.selectedNation = nationHovered;
      this.lastSelected   = nationHovered;
    }
    
    renderOutline(selectedNation, surface, g, mapWRatio, mapHRatio);
    renderOutline(nationHovered , surface, g, mapWRatio, mapHRatio);
    
    for (Nation n : nations) if (n.currentMission() != null) {
      Scene crisis = n.currentMission();
      int x = (int) (n.region.view.centerX / mapWRatio);
      int y = (int) (n.region.view.centerY / mapHRatio);
      g.drawImage(alertMarker, x - 25, y - 25, 50, 50, null);
      renderAssigned(crisis.playerTeam(), x - 25, y + 15, surface, g);
    }
    
    //
    //  Then draw the various base-facilities:
    renderBase(surface, g);
    
    //
    //  Then draw the monitor-status active at the moment-
    int x = (viewWide / 2) - 65, y = 330;
    Batch <Scene> missions = world.missions();
    
    if (world.monitorActive()) {
      g.setColor(Color.ORANGE);
      g.drawString("Monitoring...", x, y);
    }
    else if (! missions.empty()) {
      g.setColor(Color.RED);
      for (Scene m : missions) {
        g.drawString("Crisis: "+m.name(), x, y);
        y -= 20;
      }
    }
    else {
      g.setColor(Color.BLUE);
      g.drawString("Monitoring paused...", x, y);
    }
    
    //
    //  And finally, draw the roster for the current base!
    Base base = world.base();
    int offX = 360, offY = 360;
    int maxAcross = 300, across = 0, down = 0, size = 64;
    
    for (Person p : base.roster()) {
      x = offX + across;
      y = offY + down;
      g.drawImage(p.kind().sprite(), x, y, size, size, null);
      g.setColor(Color.BLUE);
      g.drawString("("+(base.rosterIndex(p) + 1)+")", x, y + 15);
      
      if (surface.mouseIn(x, y, size, size)) {
        personHovered = p;
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      
      //  TODO:  Render differently for different assignments!
      
      if (p.assignment() != null) {
        g.drawImage(alertMarker, x + size - 20, y + size - 20, 20, 20, null);
      }
      across += size;
      if (across >= maxAcross) { across = 0; down += size; }
    }
    if (personHovered != null && surface.mouseClicked) {
      this.selectedPerson = personHovered;
      this.lastSelected   = personHovered;
    }
  }
  
  
  void renderBase(Surface surface, Graphics2D g) {
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
    int senseChance = (int) (base.sensorChance() * 100);
    report += " Sensors: "+senseChance+"%";
    report += " Engineering: "+base.engineerForce();
    report += " Research: "+base.researchForce();
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
  }
  
  
  void renderOutline(
    Nation n, Surface surface, Graphics2D g, float mapWRatio, float mapHRatio
  ) {
    if (n == null || n.region.view.outline == null) return;
    RegionView r = n.region.view;
    int
      x = (int) (r.outlineX / mapWRatio),
      y = (int) (r.outlineY / mapHRatio),
      w = (int) (r.outline.getWidth (null) / mapWRatio),
      h = (int) (r.outline.getHeight(null) / mapHRatio);
    g.drawImage(r.outline, x, y, w, h, null);
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
    final Nation n = this.selectedNation;
    final Person p = this.selectedPerson;
    final Room   r = this.selectedRoom;
    
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
            if (picks.freeForAssignment()) r.addVisitor(picks);
            else if (picks.assignment() == r) r.removeVisitor(picks);
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
      Scene crisis = n.currentMission();
      String crisisName = crisis == null ? "None" : crisis.name();
      
      s.append("\nRegion:  "+n.region.name);
      s.append("\nFunding: "+n.funding()+"K");
      s.append("\nTrust:   "+trustPercent+"%");
      s.append("\nCrime:   "+crimePercent+"%");
      s.append("\nLeague Member: "+n.member());
      s.append("\n\nCurrent crisis: "+crisisName);
      
      if (n.currentMission() != null) {
        final Scene m = n.currentMission();
        final String THREAT_DESC[] = {
          "None", "Mild", "Moderate", "Severe", "Impossible"
        };
        int descL = Nums.clamp((int) (m.dangerLevel() * 5 / 2f), 5);
        s.append("\n  Threat level: "+THREAT_DESC[descL]);
        
        s.append("\n  Enemy forces:");
        if (m.othersTeam().size() > 0) {
          Tally <Kind> kinds = new Tally();
          for (Person o : m.othersTeam()) kinds.add(1, o.kind());
          for (Kind k : kinds.keys()) {
            s.append("\n    ");
            int num = (int) kinds.valueFor(k);
            if (num == 1) s.append(k.name());
            else s.append(num+"x "+k.name());
          }
        }
        else s.append("\n  No Intel");
        
        s.append("\n  Team Selected (Press 1-9):");
        for (Person t : m.playerTeam()) {
          s.append("\n    "+t.name());
        }
        Person picks = null;
        for (int i = 1; i <= 9; i++) {
          if (print.isPressed((char) ('0' + i))) {
            picks = base.atRosterIndex(i -1);
          }
        }
        if (picks != null) {
          if (picks.freeForAssignment()) m.addToTeam(picks);
          else if (picks.assignment() == m) m.removePerson(picks);
        }
      }
    }
    
    else if (p != null && p == lastSelected) {
      s.append("\nCodename: "+p.name());
      int HP = (int) (p.maxHealth() - (p.injury() + p.stun()));
      s.append("\n  Health: "+HP+"/"+p.maxHealth());
      if (p.stun() > 0) s.append(" (Stun "+(int) p.stun()+")");
      if (! p.alive()) s.append("\n  Dead");
      else if (! p.conscious()) s.append("\n  Unconscious");
      s.append("\nAbilities: ");
      for (Ability a : p.stats.listAbilities()) {
        int level = (int) p.stats.levelFor(a);
        s.append("\n  "+a.name);
        s.append(" ("+level+")");
      }
      
      Assignment assigned = p.assignment();
      String assignName = assigned == null ? "None" : assigned.name();
      s.append("\n\nAssignment: "+assignName);
    }
    
    s.append("\n");
    if (! world.assignedToMissions().empty()) {
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
}






