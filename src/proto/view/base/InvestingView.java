

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class InvestingView extends UINode {
  
  
  RegionView regionView;
  MapInsetView mapView;
  
  
  
  public InvestingView(UINode parent, Box2D bounds) {
    super(parent, bounds);
    
    int fullWide = (int) bounds.xdim(), fullHigh = (int) bounds.ydim();
    
    mapView = new MapInsetView(this, new Box2D(
      320, 5, fullWide - 640, fullHigh - 10
    ));
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    mapView.resizeToFitAspectRatio();
    
    regionView = new RegionView(this, mapView, new Box2D(
      0, 5, 320, fullHigh - 10
    ));
    
    addChildren(regionView, mapView);
  }
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    return true;
  }
  
  
}














