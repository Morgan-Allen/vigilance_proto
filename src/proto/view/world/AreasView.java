

package proto.view.world;
import proto.game.world.*;
import proto.game.person.*;

import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class AreasView extends UINode {
  
  
  final public MapView mapView;
  
  
  
  public AreasView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    mapView = new MapView(this, new Box2D(
      80, 10,
      viewBounds.xdim() - 90, viewBounds.ydim() - 20
    ));
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    mapView.resizeToFitAspectRatio();
    addChildren(mapView);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    final World world = mainView.world();
    final Base  base  = world.base();
    Object selection = mainView.selectedObject();
    
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
    
    return true;
  }
}





