

package proto.view;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class RoomView extends UINode {
  
  
  Task selectedTask = null;
  
  
  RoomView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  
  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    //  TODO:  This should properly be independent of either RoomView or
    //  RegionView.  Either merge the two or move this warning outside!
    AreasView BV = mainView.areaView;
    if (BV.selectedRoom() == null && BV.selectedNation() == null) {
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(
        "Select an agent from your roster to view their gear, skills and "+
        "relationships.\n\nSelect a room or location from the map to assign "+
        "activities.",
        g, vx + 25, vy + 0, vw - 30, 150
      );
    }
    
    Room room = mainView.areaView.selectedRoom();
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
    
    int down = vy + 50;
    
    //  TODO:  Should be using attachment/detachment here?
    for (Task task : room.possibleTasks()) {
      TaskView view = task.createView(parent);
      view.relBounds.set(vx, 0 + down, vw, 50);
      view.updateAndRender(surface, g);
      down += view.relBounds.ydim() + 10;
    }
    
    Assignment selected = mainView.areaView.selectedTask();
    String help =
      "Select a task, then click in the bottom-left of roster portraits to "+
      "assign agents to the task."
    ;
    if (Visit.arrayIncludes(room.possibleTasks(), selected)) {
      help = ((Task) selected).helpInfo();
    }
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(help, g, vx + 25, down + 20, vw - 30, 150);
  }
  
}













