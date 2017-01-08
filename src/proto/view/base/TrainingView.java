

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.common.*;
import static proto.game.person.PersonStats.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class TrainingView extends UINode {

  final static Object[] STAT_DISPLAY_COORDS = {
    BRAINS     , 0, 0 ,
    REFLEXES   , 0, 1 ,
    WILL       , 0, 2 ,
    MUSCLE     , 0, 3 ,
    
    ENGINEERING, 1, 0 ,
    MEDICINE   , 1, 1 ,
    QUESTION   , 1, 2 ,
    PERSUADE   , 1, 3 ,
    
    ARMOUR     , 1, 5 ,
    HEALTH     , 1, 6 ,
    MIN_DAMAGE , 1, 7 ,
    RNG_DAMAGE , 1, 8 ,
    ACCURACY   , 1, 10,
    DEFENCE    , 1, 11,
    SIGHT_RANGE, 1, 12,
    HIDE_RANGE , 1, 13,
    MOVE_SPEED , 1, 14,
    ACT_POINTS , 1, 15
  };
  
  
  public TrainingView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    if (! super.renderTo(surface, g)) return false;
    
    Person person = this.mainView.rosterView.selectedPerson();
    if (person == null) return false;
    
    renderTraining(surface, g, person);
    renderStats   (surface, g, person);
    
    return true;
  }
  
  
  private void renderTraining(
    Surface surface, Graphics2D g, final Person person
  ) {
    final Base base = mainView.world().playerBase();
    int down = 10, across = vw - 320;
    
    TaskTrain hovered = null;
    
    for (TaskTrain option : base.training.trainingTasksFor(person)) {
      TaskView view = option.createView(mainView);
      view.showIcon = false;
      view.relBounds.set(vx + across, vy + down, 320, 45);
      view.renderNow(surface, g);
      down += view.relBounds.ydim() + 10;
      if (surface.wasHovered(option)) hovered = option;
    }
    
    if (hovered != null) {
      down += 10;
      String desc = hovered.trained().description;
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(desc, g, vx + across, vy + down, 320, 200);
    }
    
    //  TODO:  You'll want a different presentation-method for this, given
    //  time- show the actual structure of the skill-tree so you can prioritise
    //  accordingly.
  }
  
  
  private void renderStats(Surface surface, Graphics2D g, Person person) {
    Trait hovered = null;
    
    g.setColor(Color.WHITE);
    int down = 10;
    
    for (Trait t : ALL_STATS) {
      int index = Visit.indexOf(t, STAT_DISPLAY_COORDS);
      if (index == -1) continue;
      
      int   level = person.stats.levelFor  (t);
      float XP    = person.stats.xpLevelFor(t);
      int x = (Integer) STAT_DISPLAY_COORDS[index + 1];
      int y = (Integer) STAT_DISPLAY_COORDS[index + 2];
      x = (x * 150) + 10;
      y *= 20;
      Color forT = Color.LIGHT_GRAY;
      
      if (surface.tryHover(vx + x, vy + y + down, 150, 20, t)) {
        hovered = t;
        forT = Color.YELLOW;
      }
      
      ViewUtils.renderStatBar(
        vx + x + 100, vy + y + down, 50, 15,
        Color.DARK_GRAY, null, level / 10f, false, g
      );
      ViewUtils.renderStatBar(
        vx + x + 100, vy + y + down + 15, 50, 5,
        Color.GRAY, null, XP, false, g
      );
      
      g.setColor(forT);
      g.drawString(t.name  , vx + x      , vy + y + down + 15);
      g.setColor(Color.WHITE);
      g.drawString(""+level, vx + x + 100, vy + y + down + 15);
    }
    
    if (hovered != null) {
      int bonus = person.stats.bonusFor(hovered);
      float XP = person.stats.xpLevelFor(hovered);
      
      g.setColor(Color.LIGHT_GRAY);
      String desc = "";
      if (person.stats.levelFor(hovered) == 0) {
        desc += "\n  This skill is untrained.";
      }
      else if (hovered.roots().length == 0) {
        desc += "\n  Adds 1/3 value to derived skills";
        desc += ", XP: "+((int) (XP * 100))+"%";
      }
      else {
        desc += "\n  +"+bonus+" bonus from: ";
        for (Trait r : hovered.roots()) {
          desc += r;
          if (r != Visit.last(hovered.roots())) desc += " plus ";
        }
        desc += ", XP: "+((int) (XP * 100))+"%";
      }
      desc = hovered.description + desc;
      
      ViewUtils.drawWrappedString(
        desc, g, vx + 20, vy + down + (18 * 20), 300, 100
      );
    }
  }
  
  
  
}














