


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

    final String
      MAPS_DIR = "media assets/city map/",
      ACTS_DIR = "media assets/action view/"
    ;
    mapView.loadMapImages(
      MAPS_DIR+"city_map.png",
      MAPS_DIR+"city_districts_key.png"
    );
    alertMarker   = Kind.loadImage(MAPS_DIR+"alert_symbol.png" );
    selectCircle  = Kind.loadImage(ACTS_DIR+"select_circle.png");
    constructMark = Kind.loadImage(Blueprint.IMG_DIR+"under_construction.png");
  }
  
  
  void setSelection(Object selected) {
    this.lastSelected = selected;
    I.say("Selection is: "+selected);
    if (lastSelected instanceof Assignment) {
      I.say("  Is assignment!");
    }
  }
  
  
  Assignment currentAssignment() {
    if (! (lastSelected instanceof Assignment)) return null;
    return (Assignment) lastSelected;
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
    
    float time = world.currentTime();
    g.setColor(Color.WHITE);
    g.drawString("Time: "+time+" weeks", 320, 15);
  }
}












