

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
  
  final static String
    IMG_DIR = "media assets/city map/";
  final static Image
    ALERT_IMAGE   = Kind.loadImage(IMG_DIR+"alert_symbol.png"  ),
    MYSTERY_IMAGE = Kind.loadImage(IMG_DIR+"mystery_symbol.png");
  
  
  MapInsetView mapView;
  StringButton casesButton, backButton;

  CasesView   casesView  ;
  RolesView   rolesView  ;
  SuspectView suspectView;
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
        setActiveFocus(null, false);
      }
    };
    backButton = new StringButton(
      "Back", new Box2D(200, 10, 120, 25), this
    ) {
      protected void whenClicked() {
        focusStack.removeLast();
        setActiveFocus(focusStack.last(), false);
      }
    };
    addChildren(casesButton, backButton);
    
    Box2D focusViewBound = new Box2D(0, 50, 320, fullHigh - 55);
    casesView   = new CasesView  (this, mapView, focusViewBound);
    rolesView   = new RolesView  (this, mapView, focusViewBound);
    suspectView = new SuspectView(this, mapView, focusViewBound);
    focusViews  = new UINode[] { casesView, rolesView, suspectView };
    addChildren(focusViews);
    setActiveFocus(null, false);
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
  
  
  public void setActiveFocus(Object focus, boolean onStack) {
    if (onStack && focus != this.activeFocus) focusStack.add(focus);
    
    this.activeFocus = focus;
    this.activeFocusView = null;
    backButton.valid = focusStack.size() > 0;
    
    if (focus == null) {
      activeFocusView = casesView;
    }
    if (focus instanceof Plot) {
      activeFocusView = rolesView;
    }
    else if (focus instanceof Element) {
      Element e = (Element) focus;
      activeFocusView = suspectView;
      mapView.setSelectedRegion(e.region());
    }
    
    for (UINode node : focusViews) {
      node.visible = node == activeFocusView;
    }
  }
  
}

