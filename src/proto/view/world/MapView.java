

package proto.view.world;
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
  final public static Image
    ALERT_IMAGE   = Kind.loadImage(IMG_DIR+"alert_symbol.png"  ),
    CLUE_IMAGE    = Kind.loadImage(IMG_DIR+"clue_symbol.png"   ),
    MYSTERY_IMAGE = Kind.loadImage(IMG_DIR+"mystery_symbol.png"),
    FILE_IMAGE    = Kind.loadImage(IMG_DIR+"file_image.png"    ),
    TRIAL_IMAGE   = Kind.loadImage(IMG_DIR+"trial_image.png"   ),
    JAILED_IMAGE  = Kind.loadImage(IMG_DIR+"jailed_image.png"  ),
    NOT_BUILT = Kind.loadImage(
      "media assets/tech icons/state_not_built.png"
    ),
    IN_PROGRESS = Kind.loadImage(
      "media assets/tech icons/state_in_progress.png"
    );
  final static Object
    ALL_EVENTS  = "all-cases",
    PLOT_CLUES = "plot-clues";
  
  
  final public MapInsetView mapView;
  final public ScrollArea infoArea;
  /*
  EventsView    eventsView;
  CaseRolesView rolesView ;
  CaseCluesView cluesView ;
  CasePerpsView perpsView ;
  RegionView    regionView;
  
  UINode focusViews[], activeFocusView;
  StringButton backButton, eventsButton;
  
  Object activeFocus = null;
  List <Object> focusStack = new List();
  //*/
  
  
  
  public MapView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int fullWide = (int) viewBounds.xdim(), fullHigh = (int) viewBounds.ydim();
    mapView = new MapInsetView(this, new Box2D(
      10, 10, fullWide - 360, fullHigh - 20
    )) {
      protected void onRegionSelect(Region region) {
        //setActiveFocus(region, true);
      }
    };
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png",
      MainView.MAPS_DIR+"city_districts_outline.png"
    );
    addChildren(mapView);
    
    Box2D casesBound  = new Box2D(fullWide - 340, 35, 340, fullHigh - 35);
    Box2D scrollBound = new Box2D(0             ,  0, 320, fullHigh - 35);
    
    infoArea = new ScrollArea(this, casesBound);
    /*
    infoArea = new ScrollArea(this, casesBound);
    addChildren(infoArea);
    UINode scrollKid = new UINode(infoArea, scrollBound) {};
    infoArea.attachScrollPane(scrollKid, (int) scrollBound.ydim());
    
    backButton = new StringButton(
      "Back", new Box2D(fullWide - 70, 5, 60, 25), this
    ) {
      protected void whenClicked() {
        navigateFocusBack();
      }
    };
    eventsButton = new StringButton(
      "View Events", new Box2D(fullWide - 340, 5, 270, 25), this
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
    //*/
    
    //showEventsFocus();
  }
  
  
  
  
  public void showEventsFocus() {
    //  TODO:  Restore this...
    //setActiveFocus(ALL_EVENTS, true);
  }
  
  
  public void setActiveFocus(Object focus, boolean wipeStack) {
    //  TODO:  Restore this...
  }
  
  
  public Object priorFocus() {
    //  TODO:  Restore this...
    return null;
  }
  
  
  
  /*
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
  //*/
}



