

package proto.view.world;
import proto.game.event.*;

import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;


//  TODO:  Crafting items is going to require a listing for total number of
//  items present and the option to equip/dequip.

public class TaskView extends UINode {
  
  
  final Task task;
  public boolean showIcon = true;
  
  
  public TaskView(Task task, UINode parent) {
    super(parent);
    this.task = task;
  }
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Draw the icon and description for this particular task-
    g.setColor(Color.WHITE);
    final int iconSize = vh;
    if (showIcon) {
      Image leadImg = task.icon();
      g.drawImage(leadImg, vx + 20, vy, iconSize, iconSize, null);
    }
    ViewUtils.drawWrappedString(
      task.choiceInfo(), g,
      vx + 20 + iconSize + 5, vy, vw - (20 + iconSize + 20), vh - 15
    );
    ViewUtils.drawWrappedString(
      task.testInfo(), g,
      vx + 20 + iconSize + 5, vy + vh - 20, vw - (20 + iconSize + 20), 20
    );
    //
    //  Draw the highlight/selection rectangle, and toggle selection if
    //  clicked-
    final boolean hovered = surface.tryHover(
      vx + 20 - 5, vy + 0, vw + 10 - 40, vh - 0, task
    );
    final boolean selected = mainView.selectedTask() == task;
    
    Color              boxColor = Color.DARK_GRAY;
    if      (selected) boxColor = Color.YELLOW;
    else if (hovered ) boxColor = Color.YELLOW;
    
    g.setColor(boxColor);
    g.drawRect(vx + 20 -5, vy - 5, vw + 10 - 41, vh + 9);
    
    if (surface.mouseClicked() && hovered) {
      mainView.setSelectedTask(selected ? null : task);
    }
    //
    //  Finally, draw any persons assigned to this task...
    int across = vx + vw;
    ViewUtils.renderAssigned(
      task.assigned(), across - 20, vy + vh, surface, g
    );
    
    return true;
  }
  
}













