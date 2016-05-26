

package proto.view;

import proto.common.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;




public class WorldView extends UINode {
  
  final World world;
  
  AreasView  baseView  ;
  RoomView   roomView  ;
  RegionView regionView;
  
  RosterView rosterView;
  PersonView personView;
  
  Image alertMarker, selectCircle, selectSquare;
  
  MessageView messageShown = null;
  List <MessageView> messageQueue = new List();
  
  
  
  public WorldView(World world) {
    super();
    this.world = world;
    
    int fullHigh = 750, fullWide = 1200;
    this.relBounds.set(0, 0, fullWide, fullHigh);
    
    int paneDown = 20, paneHigh = fullHigh - (20 + 20);
    
    rosterView = new RosterView(this, new Box2D(
      0 + 10, 20,
      320   , fullHigh - 40
    ));
    personView = new PersonView(this, new Box2D(
      0 + 10, 20,
      320   , fullHigh - 40
    ));
    addChildren(rosterView, personView);
    
    baseView = new AreasView(this, new Box2D(
      320, paneDown,
      400, paneHigh
    ));
    roomView = new RoomView(this, new Box2D(
      720, paneDown,
      480, paneHigh
    ));
    regionView = new RegionView(this, new Box2D(
      720, paneDown,
      480, paneHigh
    ));
    addChildren(baseView, roomView, regionView);
    
    final String
      MAPS_DIR = "media assets/city map/",
      ACTS_DIR = "media assets/action view/",
      MNUI_DIR = "media assets/main UI/"
    ;
    baseView.mapView.loadMapImages(
      MAPS_DIR+"city_map.png",
      MAPS_DIR+"city_districts_key.png"
    );
    alertMarker   = Kind.loadImage(MAPS_DIR+"alert_symbol.png" );
    selectCircle  = Kind.loadImage(ACTS_DIR+"select_circle.png");
    selectSquare  = Kind.loadImage(MNUI_DIR+"select_square.png");
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
  protected void updateAndRender(Surface surface, Graphics2D g) {
    if (rosterView.selected() != null) {
      personView.visible = true ;
      rosterView.visible = false;
    }
    else {
      personView.visible = false;
      rosterView.visible = true ;
    }
    
    final MessageView topMessage = messageQueue.first();
    if (topMessage != messageShown) {
      if (topMessage != null) {
        topMessage.attachAt(vw / 2, vh / 2);
        setChild(topMessage, true);
      }
      if (messageShown != null) {
        setChild(messageShown, false);
      }
      messageShown = topMessage;
    }
    
    super.updateAndRender(surface, g);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    
    final int wide = surface.getWidth(), high = surface.getHeight();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, wide, high);
    
    g.setColor(Color.WHITE);
    int across = 320;
    
    String timeString = ViewUtils.getTimeString(world);
    g.drawString("Time: "+timeString, across, 15);
    
    boolean hoverS = surface.tryHover(across + 360 - 150, 0, 50, 15, "Save");
    g.setColor(hoverS ? Color.YELLOW : Color.BLUE);
    g.drawString("Save"  , across + 360 - 150, 15);
    
    boolean hoverR = surface.tryHover(across + 360 - 100, 0, 70, 15, "Reload");
    g.setColor(hoverR ? Color.YELLOW : Color.BLUE);
    g.drawString("Reload", across + 360 - 100 , 15);
    
    boolean hoverQ = surface.tryHover(across + 360 - 30, 0, 50, 15, "Quit");
    g.setColor(hoverQ ? Color.YELLOW : Color.BLUE);
    g.drawString("Quit", across + 360 - 30 , 15);
    
    if (hoverS && surface.mouseClicked()) {
      world.performSave();
    }
    if (hoverR && surface.mouseClicked()) {
      world.reloadFromSave();
    }
    if (hoverQ && surface.mouseClicked()) {
      world.performSaveAndQuit();
    }
  }
  
}











