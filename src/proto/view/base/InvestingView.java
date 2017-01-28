

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
    
    Person person = mainView.rosterView.selectedPerson();
    int across = 10, down = 10;
    
    //  TODO:  Turn investments into a kind of Task?
    /*
    TaskCraft current = null;
    for (Assignment a : person.assignments()) if (a instanceof TaskCraft) {
      current = (TaskCraft) a;
    }
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Current Crafting: "+(current == null ? "None" : current.made()), g,
      vx + across, vy + down, 320, 30
    );
    //*/
    
    return true;
  }
  
  
}


