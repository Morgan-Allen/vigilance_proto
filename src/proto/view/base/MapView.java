
package proto.view.base;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class MapView extends UINode {
  
  
  /**  Constants, data fields, construction and save/load methods-
    */
  final static String
    IMG_DIR = "media assets/city map/";
  final static Image
    ALERT_IMAGE   = Kind.loadImage(IMG_DIR+"alert_symbol.png"  ),
    MYSTERY_IMAGE = Kind.loadImage(IMG_DIR+"mystery_symbol.png"),
    FILE_IMAGE    = Kind.loadImage(IMG_DIR+"file_image.png"    ),
    TRIAL_IMAGE   = Kind.loadImage(IMG_DIR+"trial_image.png"   ),
    JAILED_IMAGE  = Kind.loadImage(IMG_DIR+"jailed_image.png"  );
  final static Object
    ALL_EVENTS  = "all-cases",
    PLOT_CLUES = "plot-clues";
  
  
  MapInsetView mapView;
  ScrollArea infoArea;
  
  EventsView    eventsView;
  CaseRolesView rolesView ;
  CaseCluesView cluesView ;
  CasePerpsView perpsView ;
  RegionView    regionView;

  UINode focusViews[], activeFocusView;
  StringButton backButton, eventsButton;
  
  Object activeFocus = null;
  List <Object> focusStack = new List();
  

  
  public MapView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int fullWide = (int) viewBounds.xdim(), fullHigh = (int) viewBounds.ydim();
    mapView = new MapInsetView(this, new Box2D(
      10, 10, fullWide - 360, fullHigh - 20
    )) {
      protected void onRegionSelect(Region region) {
        setActiveFocus(region, true);
      }
    };
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    addChildren(mapView);
    
    Box2D casesBound  = new Box2D(fullWide - 340, 25, 340, fullHigh - 25);
    Box2D scrollBound = new Box2D(0             , 25, 320, fullHigh - 25);
    infoArea = new ScrollArea(this, casesBound);
    addChildren(infoArea);
    UINode scrollKid = new UINode(infoArea, scrollBound) {};
    infoArea.attachScrollPane(scrollKid, (int) scrollBound.ydim());
    
    backButton = new StringButton(
      "Back", new Box2D(fullWide - 70, 10, 60, 25), this
    ) {
      protected void whenClicked() {
        navigateFocusBack();
      }
    };
    eventsButton = new StringButton(
      "View Events", new Box2D(fullWide - 340, 10, 270, 25), this
    ) {
      protected void whenClicked() {
        showEventsFocus();
      }
    };
    addChildren(backButton, eventsButton);
    
    eventsView = new EventsView   (scrollKid, scrollBound);
    rolesView  = new CaseRolesView(scrollKid, scrollBound);
    cluesView  = new CaseCluesView(scrollKid, scrollBound);
    perpsView  = new CasePerpsView(scrollKid, scrollBound);
    regionView = new RegionView   (scrollKid, scrollBound);
    focusViews = new UINode[] {
      eventsView, rolesView, cluesView, perpsView, regionView
    };
    scrollKid.addChildren(focusViews);
    
    showEventsFocus();
  }
  
  

  public Object priorFocus() {
    return focusStack.atIndex(focusStack.size() - 2);
  }
  
  
  public void setActiveFocus(Object focus, boolean wipeStack) {
    if (wipeStack) {
      focusStack.clear();
      focusStack.add(focus);
    }
    else if (focus != this.activeFocus) {
      focusStack.add(focus);
    }
    
    this.activeFocus     = focus;
    this.activeFocusView = null ;
    backButton  .valid   = focusStack.size() > 1;
    eventsButton.valid   = activeFocus != ALL_EVENTS;
    
    if (focus == ALL_EVENTS) {
      activeFocusView = eventsView;
    }
    else if (focus == PLOT_CLUES) {
      activeFocusView = cluesView;
    }
    else if (focus instanceof Plot) {
      activeFocusView = rolesView;
    }
    else if (focus instanceof Role) {
      activeFocusView = perpsView;
    }
    else if (focus instanceof Region) {
      activeFocusView = regionView;
      mapView.setSelectedRegion((Region) focus);
    }
    else if (focus instanceof Element) {
      activeFocusView = perpsView;
      Element e = (Element) focus;
      mapView.setSelectedRegion(e.region());
    }
    for (UINode node : focusViews) {
      node.visible = node == activeFocusView;
    }
  }
  
  
  public void showEventsFocus() {
    setActiveFocus(ALL_EVENTS, true);
  }
  
  
  public void navigateFocusBack() {
    focusStack.removeLast();
    Object before = focusStack.last();
    if (before == null) before = ALL_EVENTS;
    this.activeFocus = before;
    setActiveFocus(before, false);
  }
}



