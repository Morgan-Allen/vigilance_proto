

package proto.view.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



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
    //*
    g.setColor(Color.WHITE);
    int iconSize = vh;
    
    if (showIcon) {
      Image leadImg = task.icon();
      g.drawImage(leadImg, vx + 20, vy, iconSize, iconSize, null);
    }
    else {
      iconSize = 0;
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
    
    Color        boxColor = Color.DARK_GRAY;
    if (hovered) boxColor = Color.YELLOW;
    
    g.setColor(boxColor);
    g.drawRect(vx + 20 -5, vy - 5, vw + 10 - 41, vh + 9);
    
    final Person person = mainView.rosterView.selectedPerson();
    if (surface.mouseClicked() && hovered && person != null) {
      person.setAssignment(task);
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





