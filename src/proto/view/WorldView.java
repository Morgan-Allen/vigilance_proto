


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
  
  MapView    mapView   ;
  RegionView regionView;
  RosterView rosterView;
  BaseView   baseView  ;
  Image alertMarker, selectCircle, constructMark;
  
  
  
  public WorldView(World world) {
    this.world = world;
    rosterView = new RosterView(this, new Box2D(20 , 20, 320, 560));
    mapView    = new MapView   (this, new Box2D(320, 20, 360, 560));
    regionView = new RegionView(this, new Box2D(680, 20, 520, 560));
    baseView   = new BaseView  (this, new Box2D(320, 20, 360, 560));
    
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
    
    //
    //  TODO:  Only render some of these at a time!
    mapView   .renderTo(surface, g);
    regionView.renderTo(surface, g);
    baseView  .renderTo(surface, g);
    rosterView.renderTo(surface, g);
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
    final Surface input = world.game().surface();
    
    final Base base = world.base();
    final Nation n = mapView.selectedNation;
    final Person p = rosterView.selectedPerson;
    final Room   r = null;// this.selectedRoom;
    
    if (n != null && n == lastSelected) {
      int trustPercent = (int) (n.trustLevel() * 100);
      int crimePercent = (int) (n.crimeLevel() * 100);
      
      s.append("\nRegion:  "+n.region.name);
      s.append("\nFunding: "+n.funding()+"K");
      s.append("\nTrust:   "+trustPercent+"%");
      s.append("\nCrime:   "+crimePercent+"%");
      s.append("\nLeague Member: "+n.member());
      
      for (Investigation event : world.events().active()) {
        for (Lead l : event.leadsFrom(n.region)) if (l.canFollow()) {
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
      if (input.isPressed('m')) {
        world.beginNextMission();
      }
    }
    else if (! world.monitorActive()) {
      s.append("\n  Press M to resume monitoring.");
      if (input.isPressed('m')) world.beginMonitoring();;
    }
    else {
      s.append("\n  Press M to pause monitoring.");
      if (input.isPressed('m')) world.stopMonitoring();
    }
    s.append("\n  Press S to save, R to reload.");
    if (input.isPressed('s')) world.performSave();
    if (input.isPressed('r')) world.reloadFromSave();
    
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
    
    s.append("\nStatistics: ");
    for (Trait stat : PersonStats.BASE_STATS) {
      int level = p.stats.levelFor(stat);
      int bonus = p.stats.bonusFor(stat);
      s.append("\n  "+stat.name+": "+level);
      if (bonus > 0) s.append(" (+"+bonus+")");
    }
    
    Assignment assigned = p.assignment();
    String assignName = assigned == null ? "None" : assigned.name();
    s.append("\n\nAssignment: "+assignName);
  }
  
  
  void describePersonOutfit(Person p, StringBuffer s) {
    final Surface input = world.game().surface();
    
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
      
      if (input.isPressed((char) ('0' + index))) {
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
    final Surface input = world.game().surface();
    
    if (input.isPressed('a')) {
      personPaneMode = PANE_ABILITIES;
    }
    if (input.isPressed('o')) {
      personPaneMode = PANE_OUTFIT;
    }
    if (input.isPressed('p')) {
      personPaneMode = PANE_PSYCH;
    }
  }
  
}












