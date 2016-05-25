

package proto.view;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;


//  TODO:  Crafting items is going to require a listing for total number of
//  items present and the option to equip/dequip.

public class TaskView {
  
  
  final Task task;
  final WorldView parent;
  final Box2D viewBounds;
  
  public boolean showIcon = true;
  
  
  public TaskView(Task task, WorldView parent) {
    this(task, parent, new Box2D());
  }
  
  
  TaskView(Task task, WorldView parent, Box2D viewBounds) {
    this.task       = task      ;
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
  }
  
  
  
  void renderTo(Surface surface, Graphics2D g) {
    //
    //  
    final int
      vx = (int) viewBounds.xpos(),
      vy = (int) viewBounds.ypos(),
      vw = (int) viewBounds.xdim(),
      vh = (int) viewBounds.ydim()
    ;
    final Person activeP = parent.rosterView.selected();
    //
    //  Draw the icon and description for this particular lead-
    g.setColor(Color.WHITE);
    if (showIcon) {
      Image leadImg = task.icon();
      g.drawImage(leadImg, vx + 20, vy, 60, 60, null);
    }
    ViewUtils.drawWrappedString(
      task.longInfo(), g, vx + 20 + 60 + 5, vy, vw - 95, vh - 15
    );
    g.drawString(task.testInfo(), vx + 20 + 60 +5, vy + vh);
    //
    //  Draw the highlight/selection rectangle, and toggle selection if
    //  clicked-
    final boolean hovered = activeP != null && surface.mouseIn(
      vx + 20 -5, vy - 5, vw + 10 - 40, 60 + 10, this
    );
    final boolean selected = parent.baseView.selectedTask() == task;
    
    Color boxColor = Color.GRAY;
    if (selected) boxColor = Color.GREEN;
    else if (hovered) boxColor = Color.YELLOW;
    
    g.setColor(boxColor);
    g.drawRect(vx + 20 -5, vy - 5, vw + 10 - 41, vh + 9);
    
    if (surface.mouseClicked(this) && hovered) {
      parent.baseView.setSelectedTask(selected ? null : task);
    }
    //
    //  Finally, draw any persons assigned to this task...
    int across = vx + vw;
    ViewUtils.renderAssigned(
      task.assigned(), across - 20, vy + vh, surface, g
    );
  }
  
}














