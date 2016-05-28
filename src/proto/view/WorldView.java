
package proto.view;

import proto.common.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;




public class WorldView extends UINode {
  
  final World world;
  
  AreasView  areaView  ;
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
    
    int fullHigh = 750, fullWide = 1200, rosterHigh = 125;
    this.relBounds.set(0, 0, fullWide, fullHigh);
    int paneDown = 20 + rosterHigh, paneHigh = fullHigh - (10 + paneDown);
    
    personView = new PersonView(this, new Box2D(
      0  , 0,
      320, fullHigh
    ));
    rosterView = new RosterView(this, new Box2D(
      320, 0,
      fullWide - 320, 120
    ));
    addChildren(personView, rosterView);
    
    areaView = new AreasView(this, new Box2D(
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
    addChildren(areaView, roomView, regionView);
    
    final String
      MAPS_DIR = "media assets/city map/",
      ACTS_DIR = "media assets/action view/",
      MNUI_DIR = "media assets/main UI/"
    ;
    areaView.mapView.loadMapImages(
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
    
    /*
    if (rosterView.selected() != null) {
      personView.visible = true;
      areaView.visible   = false;
    }
    else {
      personView.visible = false;
      areaView.visible   = true;
    }
    //*/
    
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
    g.setColor(Color.BLACK);
    g.fillRect(vx, vy, vw, vh);
  }
  
}











