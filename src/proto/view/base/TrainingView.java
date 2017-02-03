

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
    BRAINS     , 0, 0 , 10,
    REFLEXES   , 0, 1 , 10,
    WILL       , 0, 2 , 10,
    MUSCLE     , 0, 3 , 10,
    
    ENGINEERING, 0, 5 , 20,
    MEDICINE   , 0, 6 , 20,
    QUESTION   , 0, 7 , 20,
    PERSUADE   , 0, 8 , 20,
    
    ARMOUR     , 1, 0 , 20,
    HEALTH     , 1, 1 , 20,
    MIN_DAMAGE , 1, 2 , 20,
    RNG_DAMAGE , 1, 3 , 20,
    
    ACCURACY   , 1, 5 , 100,
    DEFENCE    , 1, 6 , 100,
    SIGHT_RANGE, 1, 7 , 10,
    HIDE_RANGE , 1, 8 , 10,
    MOVE_SPEED , 1, 9 , 40,
    ACT_POINTS , 1, 10, 10,
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
    int down = 10, across = 10;
    
    TaskTrain current = null;
    for (Assignment a : person.assignments()) if (a instanceof TaskTrain) {
      current = (TaskTrain) a;
    }
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Training: "+(current == null ? "None" : current.trained()), g,
      vx + across, vy + down, 320, 30
    );
    
    down += 30;
    TaskTrain hovered = current;
    
    for (TaskTrain option : base.training.trainingTasksFor(person)) {
      TaskView view = option.createView(mainView);
      view.showIcon = false;
      view.relBounds.set(vx + across, vy + down, 320, 20);
      view.renderNow(surface, g);
      down += view.relBounds.ydim() + 10;
      if (surface.wasHovered(option)) hovered = option;
    }
    
    if (hovered != null) {
      down += 10;
      
      Ability trained = hovered.trained();
      float xp = person.stats.xpLevelFor(trained);
      int trainTime = hovered.trainingTime(person) / World.HOURS_PER_DAY;
      int costAP = trained.minCostAP();
      String desc = hovered.trained().description;
      
      if (trainTime <= 0) desc += "\n  Cannot train when badly wounded.";
      else desc += "\n  Training time: "+trainTime+" days";
      desc += " ("+(int) (xp * 100)+"% complete).";
      if (costAP > 0) desc += "\n  Base AP cost: "+costAP;
      
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
    int down = 10, across = vw - 360;
    for (Trait t : ALL_STATS) {
      int index = Visit.indexOf(t, STAT_DISPLAY_COORDS);
      if (index == -1) continue;
      
      int   level = person.stats.levelFor  (t);
      float XP    = person.stats.xpLevelFor(t);
      int x   = (Integer) STAT_DISPLAY_COORDS[index + 1];
      int y   = (Integer) STAT_DISPLAY_COORDS[index + 2];
      int max = (Integer) STAT_DISPLAY_COORDS[index + 3];
      x = (x * 150) + 10;
      y *= 20;
      Color forT = Color.LIGHT_GRAY;
      
      if (surface.tryHover(vx + x + across, vy + y + down, 150, 20, t)) {
        hovered = t;
        forT = Color.YELLOW;
      }
      
      ViewUtils.renderStatBar(
        vx + x + 100 + across, vy + y + down, 50, 15,
        Color.DARK_GRAY, null, level * 1f / max, false, g
      );
      ViewUtils.renderStatBar(
        vx + x + 100 + across, vy + y + down + 15, 50, 5,
        Color.GRAY, null, XP, false, g
      );
      
      g.setColor(forT);
      g.drawString(t.name  , vx + x + across, vy + y + down + 15);
      g.setColor(Color.WHITE);
      g.drawString(""+level, vx + x + 100 + across, vy + y + down + 15);
    }
    
    if (hovered != null) {
      //int bonus = person.stats.bonusFor(hovered);
      //float XP = person.stats.xpLevelFor(hovered);
      
      g.setColor(Color.LIGHT_GRAY);
      String desc = "";
      /*
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
      //*/
      desc = hovered.description + desc;
      
      ViewUtils.drawWrappedString(
        desc, g, vx + 20 + across, vy + down + (12 * 20), 300, 100
      );
    }
  }
  
  
  
}














