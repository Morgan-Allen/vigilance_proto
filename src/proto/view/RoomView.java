

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
    
    Object area = parent.baseView.selectedArea;
    if (! (area instanceof Room)) return;
    Room room = (Room) area;
    
    final int
      vx = (int) viewBounds.xpos(),
      vy = (int) viewBounds.ypos(),
      vw = (int) viewBounds.xdim(),
      vh = (int) viewBounds.ydim()
    ;
    //Image portrait = nation.region.view.portrait;
    
    /*
    //  TODO:  Use a proper interior view instead.
    Image portrait = room.icon();
    int portW = vw - 40, portH = (int) (portW / 2.5f);
    g.drawImage(portrait, vx + 20, vy + 40, portW, portH, null);
    //*/

    g.setColor(Color.WHITE);
    g.drawString(room.name(), vx + 20, vy + 20);
    
    
    //  TODO:  Create a TaskView class for this, and share with RegionView!
    
    int down = vy + 50, across;
    
    for (Task task : room.possibleTasks()) {
      //
      //  Draw the icon and description for this particular lead-
      Image leadImg = task.icon();
      //g.drawImage(leadImg, vx + 20, down, LIS, LIS, null);
      
      g.setColor(Color.WHITE);
      ViewUtils.drawWrappedString(
        task.info(), g, vx + 20 + 60 +5, down, vw - 85, 45
      );
      g.drawString(task.testInfo(), vx + 20 + 60 + 5, down + 45 + 15);
      //
      //  Draw the highlight/selection rectangle, and toggle selection if
      //  clicked-
      final boolean hovered = surface.mouseIn(
        vx + 20 -5, down - 5, vw + 10 - 40, 60 + 10, this
      );
      if (selectedTask == task || hovered) {
        if (task != selectedTask) g.setColor(Color.GRAY);
        g.drawRect(vx + 20 -5, down - 5, vw + 10 - 40, 60 + 10);
      }
      if (surface.mouseClicked(this) && hovered) {
        boolean selected = parent.lastSelected == task;
        selectedTask = selected ? null : task;
        parent.setSelection(selectedTask);
      }
      //
      //  Finally, draw any persons assigned to this lead...
      across = vx + 250;
      for (Person p : task.assigned()) {
        g.drawImage(p.kind().sprite(), across, down + 45, 20, 20, null);
        across += 25;
      }
      //
      //  ...and move down for the next lead.
      down += 60 + 10;
    }
  }
  
}












