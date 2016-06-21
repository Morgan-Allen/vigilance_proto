
package proto.view;

import proto.common.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;




public class MainView extends UINode {
  
  final World world;
  
  SceneView  sceneView ;
  UINode     mainUI    ;
  
  AreasView  areaView  ;
  RoomView   roomView  ;
  RegionView regionView;
  
  RosterView rosterView;
  PersonView personView;
  
  Image alertMarker, selectCircle, selectSquare;
  
  UINode messageShown = null;
  List <UINode> messageQueue = new List();
  ClickMenu clickMenu = null;
  
  
  
  public MainView(World world) {
    super();
    this.world = world;
    
    int fullHigh = 750, fullWide = 1200, rosterHigh = 125;
    this.relBounds.set(0, 0, fullWide, fullHigh);
    int paneDown = 20 + rosterHigh, paneHigh = fullHigh - (10 + paneDown);
    
    sceneView = new SceneView(this, new Box2D(
      0, 0, fullWide, fullHigh
    ));
    mainUI = new UINode(this, new Box2D(
      0, 0, fullWide, fullHigh
    )) {
      void renderTo(Surface surface, Graphics2D g) {
      }
    };
    addChildren(sceneView, mainUI);
    
    personView = new PersonView(mainUI, new Box2D(
      0  , 0,
      320, fullHigh
    ));
    rosterView = new RosterView(mainUI, new Box2D(
      320, 0,
      fullWide - 320, 120
    ));
    mainUI.addChildren(personView, rosterView);
    
    areaView = new AreasView(mainUI, new Box2D(
      320, paneDown,
      400, paneHigh
    ));
    roomView = new RoomView(mainUI, new Box2D(
      720, paneDown,
      480, paneHigh
    ));
    regionView = new RegionView(mainUI, new Box2D(
      720, paneDown,
      480, paneHigh
    ));
    mainUI.addChildren(areaView, roomView, regionView);
    
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
  
  
  public SceneView sceneView() {
    return sceneView;
  }
  
  
  
  /**  Various custom popup-presentation methods:
    */
  public void queueMessage(UINode message) {
    messageQueue.add(message);
    world.pauseMonitoring();
  }
  
  
  public void dismissMessage(UINode message) {
    messageQueue.remove(message);
  }
  
  
  public void showClickMenu(ClickMenu menu) {
    this.clickMenu = menu;
    setChild(menu, true);
  }
  
  
  public void hideClickMenu(ClickMenu menu) {
    setChild(menu, false);
    this.clickMenu = null;
  }
  
  
  
  /**  Rendering methods-
    */
  protected void updateAndRender(Surface surface, Graphics2D g) {
    
    final UINode topMessage = messageQueue.first();
    
    if (world.activeScene() != null) {
      sceneView.visible = true ;
      mainUI   .visible = false;
    }
    else {
      sceneView.visible = false;
      mainUI   .visible = true ;
    }
    
    if (topMessage != messageShown) {
      if (topMessage != null) {
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











