
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
    ALL_CASES  = "all-cases",
    PLOT_CLUES = "plot-clues";
  
  
  MapInsetView mapView;
  ScrollArea casesArea;
  
  EventsView    eventsView;
  CaseRolesView rolesView ;
  CaseCluesView cluesView ;
  CasePerpsView perpsView ;
  RegionView    regionView;

  UINode focusViews[], activeFocusView;
  StringButton backButton, closeButton;
  
  Object activeFocus = null;
  List <Object> focusStack = new List();
  

  
  public MapView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);

    int fullWide = (int) viewBounds.xdim(), fullHigh = (int) viewBounds.ydim();
    mapView = new MapInsetView(this, new Box2D(
      5, 5, fullWide - 340, fullHigh - 10
    )) {
      protected void onRegionSelect(Region region) {
        setActiveFocus(region, true);
      }
    };
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    //mapView.resizeToFitAspectRatio();
    addChildren(mapView);
    
    Box2D casesBound  = new Box2D(fullWide - 340, 25, 340, fullHigh - 25);
    Box2D scrollBound = new Box2D(0             , 25, 320, fullHigh - 25);
    casesArea = new ScrollArea(this, casesBound);
    addChildren(casesArea);
    UINode scrollKid = new UINode(casesArea, scrollBound) {};
    casesArea.attachScrollPane(scrollKid, (int) scrollBound.ydim());
    
    backButton = new StringButton(
      "Back", new Box2D(fullWide - 70, 10, 60, 25), this
    ) {
      protected void whenClicked() {
        navigateFocusBack();
      }
    };
    closeButton = new StringButton(
      "Close", new Box2D(fullWide - 340, 10, 60, 25), this
    ) {
      protected void whenClicked() {
        wipeFocusStack();
      }
    };
    addChildren(backButton, closeButton);
    
    eventsView = new EventsView   (scrollKid, scrollBound);
    rolesView  = new CaseRolesView(scrollKid, scrollBound);
    cluesView  = new CaseCluesView(scrollKid, scrollBound);
    perpsView  = new CasePerpsView(scrollKid, scrollBound);
    regionView = new RegionView   (scrollKid, scrollBound);
    focusViews = new UINode[] {
      eventsView, rolesView, cluesView, perpsView, regionView
    };
    scrollKid.addChildren(focusViews);
    
    wipeFocusStack();
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
    backButton .visible = focusStack.size() > 1;
    closeButton.visible = activeFocus != ALL_CASES;
    
    if (focus == ALL_CASES) {
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
  
  
  public void wipeFocusStack() {
    setActiveFocus(ALL_CASES, true);
  }
  
  
  public void navigateFocusBack() {
    focusStack.removeLast();
    Object before = focusStack.last();
    if (before == null) before = ALL_CASES;
    this.activeFocus = before;
    setActiveFocus(before, false);
  }
}



