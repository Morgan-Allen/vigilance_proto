


package proto.view;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;




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
  BaseView   baseView  ;
  PersonView personView;
  
  RosterView rosterView;
  Image alertMarker, selectCircle, constructMark;
  
  List <MessageView> messageQueue = new List();
  
  
  
  public WorldView(World world) {
    this.world = world;
    
    rosterView = new RosterView(this, new Box2D(20 , 20, 320, 560));
    
    personView = new PersonView(this, new Box2D(320, 20, 880, 560));
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
  
  
  public void queueMessage(MessageView message) {
    messageQueue.add(message);
    world.pauseMonitoring();
  }
  
  
  public void dismissMessage(MessageView message) {
    messageQueue.remove(message);
  }
  
  
  
  /**  Rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    final int wide = surface.getWidth(), high = surface.getHeight();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, wide, high);

    MessageView message = messageQueue.first();
    surface.setMouseFocus(message);
    
    rosterView.renderTo(surface, g);
    //  TODO:  The toggling-criteria here could use some review...
    if (lastSelected instanceof Person) {
      personView.renderTo(surface, g);
    }
    else {
      mapView   .renderTo(surface, g);
      regionView.renderTo(surface, g);
      baseView  .renderTo(surface, g);
    }
    
    g.setColor(Color.WHITE);
    String timeString = ViewUtils.getTimeString(world);
    g.drawString("Time: "+timeString, 320, 15);
    
    boolean hoverS = surface.mouseIn(320 + 360 - 160, 0, 80, 15, this);
    g.setColor(hoverS ? Color.YELLOW : Color.BLUE);
    g.drawString("Save (S)"  , 320 + 360 - 160, 15);
    
    boolean hoverR = surface.mouseIn(320 + 360 - 80, 0, 80, 15, this);
    g.setColor(hoverR ? Color.YELLOW : Color.BLUE);
    g.drawString("Reload (R)", 320 + 360 - 80, 15);
    
    if (hoverS && surface.mouseClicked(this)) {
      world.performSave();
    }
    if (hoverR && surface.mouseClicked(this)) {
      world.reloadFromSave();
    }
    
    if (message != null) {
      message.attachTo(this, 400, 250);
      message.renderTo(surface, g);
    }
  }
  
}











