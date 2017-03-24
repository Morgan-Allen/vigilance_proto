

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



public class MissionsView extends UINode {
  
  /**  Constants, data fields, construction and save/load methods-
    */
  final static String
    IMG_DIR = "media assets/city map/";
  final static Image
    ALERT_IMAGE   = Kind.loadImage(IMG_DIR+"alert_symbol.png"  ),
    MYSTERY_IMAGE = Kind.loadImage(IMG_DIR+"mystery_symbol.png"),
    FILE_IMAGE    = Kind.loadImage(IMG_DIR+"file_image.png"    );
  
  final static Object
    ALL_CASES  = "all-cases",
    PLOT_CLUES = "plot-clues",
    PERP_LINKS = "perp-links";
  
  MapInsetView mapView;
  StringButton casesButton, backButton;
  
  ScrollArea casesArea;
  MissionsViewCasesView casesView;
  MissionsViewRolesView rolesView;
  MissionsViewCluesView cluesView;
  MissionsViewPerpsView perpsView;
  MissionsViewLinksView linksView;
  UINode focusViews[], activeFocusView;
  
  Object activeFocus = null;
  List <Object> focusStack = new List();
  
  
  
  public MissionsView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int fullWide = (int) viewBounds.xdim(), fullHigh = (int) viewBounds.ydim();
    mapView = new MapInsetView(this, new Box2D(
      320, 5, fullWide - 640, fullHigh - 10
    )) {
      protected void onRegionSelect(Region region) {
        setActiveFocus(region, true);
      }
    };
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    mapView.resizeToFitAspectRatio();
    addChildren(mapView);
    
    casesButton = new StringButton(
      "View Open Cases", new Box2D(0, 10, 200, 25), this
    ) {
      protected void whenClicked() {
        focusStack.clear();
        setActiveFocus(ALL_CASES, false);
      }
    };
    backButton = new StringButton(
      "Back", new Box2D(200, 10, 120, 25), this
    ) {
      protected void whenClicked() {
        focusStack.removeLast();
        Object before = focusStack.last();
        if (before == null) before = ALL_CASES;
        setActiveFocus(before, false);
      }
    };
    addChildren(casesButton, backButton);
    
    Box2D casesBound  = new Box2D(0, 50, 340, fullHigh - 100);
    Box2D scrollBound = new Box2D(0, 0 , 320, fullHigh - 100);
    casesArea = new ScrollArea(this, casesBound);
    addChildren(casesArea);
    UINode scrollKid = new UINode(casesArea, scrollBound) {};
    casesArea.attachScrollPane(scrollKid, (int) scrollBound.ydim());
    
    casesView  = new MissionsViewCasesView(scrollKid, scrollBound);
    rolesView  = new MissionsViewRolesView(scrollKid, scrollBound);
    cluesView  = new MissionsViewCluesView(scrollKid, scrollBound);
    perpsView  = new MissionsViewPerpsView(scrollKid, scrollBound);
    linksView  = new MissionsViewLinksView(scrollKid, scrollBound);
    focusViews = new UINode[] {
      casesView, rolesView, cluesView, perpsView, linksView
    };
    scrollKid.addChildren(focusViews);
    setActiveFocus(ALL_CASES, false);
  }
  
  
  protected void renderAfterKids(Surface surface, Graphics2D g) {
    Person person = mainView.rosterView.selectedPerson();
    if (person == null) return;
    
    g.setColor(Color.WHITE);
    final Assignment task = person.topAssignment();
    String assignDesc = "None";
    if (task != null) {
      assignDesc = task.activeInfo();
    }
    else {
      assignDesc = "At Base";
    }
    
    ViewUtils.drawWrappedString(
      person+" is "+assignDesc, g,
      vx + 330, vy + 10, 320, 45
    );
  }
  
  
  public Object focusOfType(Class type) {
    //
    //  Works backward through the navigation stack to find the last plot
    //  referred to.
    for (ListEntry e = focusStack; (e = e.lastEntry()) != focusStack;) {
      if (type.isAssignableFrom(e.refers.getClass())) {
        return e.refers;
      }
    }
    return null;
  }
  
  
  public void setActiveFocus(Object focus, boolean onStack) {
    if (onStack && focus != this.activeFocus) {
      focusStack.add(focus);
    }
    
    this.activeFocus = focus;
    this.activeFocusView = null;
    backButton.valid = focusStack.last() != ALL_CASES && ! focusStack.empty();
    
    if (focus == ALL_CASES) {
      activeFocusView = casesView;
    }
    else if (focus == PLOT_CLUES) {
      activeFocusView = cluesView;
    }
    else if (focus == PERP_LINKS) {
      activeFocusView = linksView;
    }
    else if (focus instanceof Plot) {
      activeFocusView = rolesView;
    }
    else if (focus instanceof Plot.Role) {
      activeFocusView = perpsView;
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
  
}



