

package proto.view.base;
import proto.game.world.*;
import proto.game.person.*;

import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class ActivityView extends UINode {
  
  
  MapInsetView mapView;
  RegionView regionView;
  LeadsListView leadsView;
  
  
  
  
  public ActivityView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int fullWide = (int) viewBounds.xdim(), fullHigh = (int) viewBounds.ydim();
    
    leadsView = new LeadsListView(this, new Box2D(
      0, 5, 320, fullHigh - 10
    ));
    
    mapView = new MapInsetView(this, new Box2D(
      320, 5, fullWide - 640, fullHigh - 10
    ));
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    mapView.resizeToFitAspectRatio();
    
    regionView = new RegionView(this, new Box2D(
      fullWide - 320, 5, 320, fullHigh - 10
    ));
    
    addChildren(leadsView, mapView, regionView);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    final World world = mainView.world();
    final Base  base  = world.playerBase();
    
    /*
    //Object selection = mainView.selectedObject();
    int down = 10;
    for (Place room : base.rooms()) if (room != null) {
      g.drawImage(room.icon(), vx + 10, vy + down, 60, 60, null);
      boolean hover = surface.tryHover(vx + 10, vy + down, 60, 60, room);
      
      if (hover || selection == room) {
        g.drawImage(mainView.selectSquare, vx + 10, vy + down, 60, 60, null);
      }
      
      if (hover && surface.mouseClicked()) {
        mainView.setSelection(room);
      }
      
      ViewUtils.renderAssigned(
        room.visitors(), vx + 60 + 10, vy + down + 60, surface, g
      );
      down += 60 + 10;
    }
    //*/
    
    return true;
  }
}





