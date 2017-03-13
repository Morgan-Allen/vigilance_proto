

package proto.view.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.view.common.*;
import proto.game.world.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;


//  TODO:  Given the various specialised sub-interfaces, I may not need this
//  any more.


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
    final Person person = mainView.rosterView.selectedPerson();
    final boolean assignOK = person != null && task.allowsAssignment(person);
    
    g.setColor(Color.WHITE);
    if (! assignOK) g.setColor(Color.GRAY);
    int iconSize = vh;
    
    if (showIcon) {
      Image leadImg = task.icon();
      g.drawImage(leadImg, vx + 20, vy, iconSize, iconSize, null);
    }
    else {
      iconSize = 0;
    }
    ViewUtils.drawWrappedString(
      task.choiceInfo(person), g,
      vx + 20 + iconSize + 5, vy, vw - (20 + iconSize + 20), vh - 0
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
    
    if (surface.mouseClicked() && hovered && assignOK) {
      final boolean wasAssigned = person.assignments().includes(task);
      if (wasAssigned) person.removeAssignment(task);
      else             person.addAssignment   (task);
    }
    //
    //  Finally, draw any persons assigned to this task...
    ViewUtils.renderAssigned(
      task.assigned(), vx + vw - 20, vy + vh, surface, g
    );
    return true;
  }
  
}


