

package proto.view;
import proto.game.scene.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RoomView {
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  Task selectedTask = null;
  
  
  RoomView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
  }
  
  
  
  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    
    final int
      vx = (int) viewBounds.xpos(),
      vy = (int) viewBounds.ypos(),
      vw = (int) viewBounds.xdim(),
      vh = (int) viewBounds.ydim()
    ;
    
    //  TODO:  This should properly be independent of either RoomView or
    //  RegionView.  Either merge the two or move this warning outside!
    BaseView BV = parent.baseView;
    if (BV.selectedRoom() == null && BV.selectedNation() == null) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select an agent from your roster to view their gear, skills and "+
        "relationships.\n\nSelect a room or location from the map to assign "+
        "activities.",
        g, vx + 25, vy + 0, vw - 30, 150
      );
    }
    
    Room room = parent.baseView.selectedRoom();
    if (room == null) return;
    
    //Image portrait = nation.region.view.portrait;
    /*
    //  TODO:  Use a proper interior view instead.
    Image portrait = room.icon();
    int portW = vw - 40, portH = (int) (portW / 2.5f);
    g.drawImage(portrait, vx + 20, vy + 40, portW, portH, null);
    //*/

    g.setColor(Color.WHITE);
    g.drawString(room.name(), vx + 20, vy + 20);
    //
    //  TODO:  Render information on the room type!
    
    String info = room.blueprint.description;
    ViewUtils.drawWrappedString(info,
      g, vx + 25, vy + 20, vw - 30, 80
    );
    
    int down = vy + 100;
    
    for (Task task : room.possibleTasks()) {
      TaskView view = task.createView(parent);
      view.viewBounds.set(vx, vy + down, vw, 60);
      view.renderTo(surface, g);
      down += 60 + 10;
    }
    
    if (parent.rosterView.selected() == null) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select an agent from your roster to perform assignments.",
        g, vx + 25, down + 20, vw - 30, 150
      );
    }
  }
  
}











