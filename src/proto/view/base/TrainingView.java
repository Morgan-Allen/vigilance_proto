

package proto.view.base;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.view.common.*;
import static proto.game.person.PersonStats.*;
import proto.util.*;

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
    
    renderTraining (surface, g, person);
    renderStats    (surface, g, person);
    renderEquipment(surface, g, person);
    
    return true;
  }
  
  
  private void renderTraining(
    Surface surface, Graphics2D g, final Person person
  ) {
    int down = 10, across = 10;
    //
    //  Describe whatever the agent is currently training in-
    final Base played = mainView.world().playerBase();
    Series <Ability> trainPath = new Batch();
    Ability hovered = null;
    TaskTrain current = null;
    for (Assignment a : person.assignments()) if (a instanceof TaskTrain) {
      current = (TaskTrain) a;
      hovered = current.trainingNow();
      trainPath = TaskTrain.trainingPath(current.trainingGoal(), person);
    }
    g.setColor(Color.WHITE);
    
    String trainDesc = "None";
    if (current != null) {
      trainDesc = ""+current.trainingNow();
      if (current.trainingGoal() != current.trainingNow()) {
        trainDesc += " (Goal: "+current.trainingGoal()+")";
      }
    }
    
    ViewUtils.drawWrappedString(
      "Currently training: "+trainDesc, g,
      vx + across, vy + down, 320, 30
    );
    
    AbilityPalette palette = person.kind().abilityPalette();
    int iconSize = 50, padding = 15;
    down += 30; across = 90;
    //
    //  Draw lines to indicate dependency between abilities-
    for (Coord c : Visit.grid(0, 0, palette.wide, palette.high, 1)) {
      final Ability a = palette.grid[c.x][c.y];
      if (a == null) continue;
      
      g.setColor(Color.LIGHT_GRAY);
      for (Trait t : a.roots()) if (t instanceof Ability) {
        Coord at = palette.gridLocation((Ability) t);
        if (at == null) continue;
        
        int lineX = vx + across, lineY = vy + down;
        int xa = (int) ((c .x + 0.5f) * (iconSize + padding));
        int ya = (int) ((c .y + 0.5f) * (iconSize + padding));
        int xb = (int) ((at.x + 0.5f) * (iconSize + padding));
        int yb = (int) ((at.y + 0.5f) * (iconSize + padding));
        g.drawLine(lineX + xa, lineY + ya, lineX + xb, lineY + yb);
      }
    }
    //
    //  And buttons for the abilities themselves-
    for (Coord c : Visit.grid(0, 0, palette.wide, palette.high, 1)) {
      final Ability a = palette.grid[c.x][c.y];
      if (a == null) continue;
      
      Image icon = a.icon();
      int x = c.x * (iconSize + padding), y = c.y * (iconSize + padding);
      int w = iconSize, h = iconSize;
      x += across + (padding / 2);
      y += down   + (padding / 2);
      Box2D bound = new Box2D(x, y, w, h);
      
      boolean canLearn = a.canLearn(person);
      
      Color status = null;
      if (a == trainPath.first()) {
        status = Color.BLUE;
      }
      else if (a == trainPath.last()) {
        status = Color.RED;
      }
      else if (trainPath.includes(a)) {
        status = Color.MAGENTA;
      }
      if (status != null) {
        g.setColor(status);
        g.fillRect(vx + x - 2, vy + y - 2, w + 4, h + 5);
      }
      
      ImageButton b = new ImageButton(icon, bound, this) {
        protected void whenClicked() {
          selectTraining(a, person, played);
        }
      };
      b.refers = a;
      b.renderNow(surface, g);
      
      if (! canLearn) {
        g.setColor(new Color(1, 1, 1, 0.5f));
        g.fillRect(vx + x - 2, vy + y - 2, w + 4, h + 5);
      }
    }
    //
    //  And provide a description for the current ability-
    Object focus = surface.lastFocus();
    if (focus instanceof Ability) hovered = (Ability) focus;
    
    if (hovered != null) {
      down += 20 + (palette.high * (iconSize + padding));
      across = 10;
      
      float xp = person.stats.xpLevelFor(hovered);
      int trainTime = TaskTrain.trainingTime(person, hovered);
      trainTime /= World.HOURS_PER_DAY;
      int costAP = hovered.minCostAP();
      int level = person.stats.levelFor(hovered);
      
      String desc = hovered.name;
      desc += " ("+hovered.levelDesc(level + 1)+")";
      desc += "\n"+hovered.description;
      
      Series <Ability> path = TaskTrain.trainingPath(hovered, person);
      Ability nextNeed = path.atIndex(path.size() - 2);
      if (nextNeed != null) desc += "\n  Requires: "+nextNeed.name;
      
      if (trainTime <= 0) desc += "\n  Cannot train when badly wounded.";
      else                desc += "\n  Training time: "+trainTime+" days";
      desc += " ("+(int) (xp * 100)+"% complete).";
      if (costAP > 0) desc += "\n  Base AP cost: "+costAP;
      
      g.setColor(Color.LIGHT_GRAY);
      ViewUtils.drawWrappedString(desc, g, vx + across, vy + down, 320, 200);
    }
  }
  
  
  void selectTraining(Ability trained, Person person, Base base) {
    TaskTrain trainTask = base.training.trainingFor(person);
    trainTask.assignTraining(trained);
    person.addAssignment(trainTask);
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
      
      if (surface.tryHover(vx + x + across, vy + y + down, 150, 20, t, this)) {
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
  
  
  void renderEquipment(Surface surface, Graphics2D g, Person person) {
    int down = 1, across = (vw - 320) / 2;
    
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Equipment", g, vx + across, vy + down, 320, 30
    );
    down += 30;
    
    int maxSlots = person.gear.maxSlots();
    for (int slotID : PersonGear.SLOT_IDS) {
      if (slotID >= maxSlots) break;
      
      Item inSlot = person.gear.itemInSlot(slotID);
      Image  icon = inSlot == null ? null   : inSlot.icon();
      String desc = inSlot == null ? "None" : inSlot.name();
      String slotName = PersonGear.SLOT_NAMES[slotID];
      
      final boolean hovered = surface.tryHover(
        vx + 5 + across, vy + down, 320, 40, "Slot_"+slotID, this
      );
      if (hovered) g.setColor(Color.YELLOW);
      else         g.setColor(Color.WHITE );
      
      g.drawImage(icon, vx + across + 5, vy + down, 40, 40, null);
      g.drawString(slotName+": "+desc, vx + across + 50, vy + down + 15);
      
      if (hovered && surface.mouseClicked()) {
        createItemMenu(person, slotID, vx + across + 5, vy + down + 20);
      }
      
      down += 40 + 10;
    }
  }
  
  
  void createItemMenu(final Person person, final int slotID, int x, int y) {
    final Base base = mainView.world().playerBase();
    final Batch <ItemType> types = new Batch();
    final BaseStocks stocks = mainView.world().playerBase().stocks;
    final int slotType = PersonGear.SLOT_TYPES[slotID];
    
    for (ItemType type : stocks.availableItemTypes(person, slotType)) {
      types.add(type);
    }
    if (types.empty()) return;
    
    mainView.showClickMenu(new ClickMenu <ItemType> (
      types, x, y, mainView
    ) {
      protected Image imageFor(ItemType option) {
        return option.icon();
      }
      
      protected String labelFor(ItemType option) {
        return option.name()+" ("+option.describeStats(person)+")";
      }
      
      protected void whenPicked(ItemType option, int optionID) {
        Item item = base.stocks.nextOfType(option);
        person.gear.equipItem(item, slotID, base);
      }
    });
  }
  
}



