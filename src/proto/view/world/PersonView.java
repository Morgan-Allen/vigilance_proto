

package proto.view.world;
import proto.game.world.*;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;

import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class PersonView extends UINode {
  
  final static Object[] STAT_DISPLAY_COORDS = {
    INTELLECT, 0, 0,
    REFLEX   , 0, 1,
    SOCIAL   , 0, 2,
    STRENGTH , 0, 3,
    
    ENGINEERING  , 0, 5 ,
    INFORMATICS  , 0, 6 ,
    PHARMACY     , 0, 7 ,
    ANATOMY      , 0, 8 ,
    LAW_N_FINANCE, 0, 9 ,
    THE_OCCULT   , 0, 10,
    
    LANGUAGES    , 1, 0 ,
    QUESTION     , 1, 1 ,
    DISGUISE     , 1, 2 ,
    SUASION      , 1, 3 ,
    
    STEALTH      , 1, 5 ,
    SURVEILLANCE , 1, 6 ,
    VEHICLES     , 1, 7 ,
    MARKSMAN     , 1, 8 ,
    
    INTIMIDATE   , 1, 10,
    GYMNASTICS   , 1, 11,
    CLOSE_COMBAT , 1, 12,
    STAMINA      , 1, 13,
  };
  final static int
    TAB_SKILLS = 1,
    TAB_GEAR   = 2,
    TAB_BONDS  = 3,
    
    ACTION_BACK = 0,
    ACTION_LAST = 1,
    ACTION_NEXT = 2
  ;
  
  
  int tabMode = TAB_SKILLS;
  StringButton tabButtons[];
  
  
  public PersonView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    tabButtons = new StringButton[3];
    final String tabNames[] = { "Skills", "Gear", "Bonds" };
    for (int i = 0; i < 3; i++) {
      final int modeID = i + 1;
      tabButtons[i] = new StringButton(
        tabNames[i], vx + 0 + 5 + (60 * i), vy + 160 + 40, 60, 20, this
      ) {
        protected void whenClicked() {
          setTab(modeID, this);
        }
      };
    }
    addChildren(tabButtons);
  }
  
  
  void setTab(int modeID, StringButton clicked) {
    tabMode = modeID;
    for (StringButton b : tabButtons) b.toggled = b == clicked;
  }
  
  
  protected void updateAndRender(Surface surface, Graphics2D g) {
    super.updateAndRender(surface, g);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Person person = mainView.selectedPerson();
    if (person == null) return false;
    
    g.setColor(Color.WHITE);
    g.drawImage(person.kind().sprite(), vx, vy, 120, 120, null);
    
    g.drawString("Codename: "+person.name(), vx + 125, vy + 20);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      person.history.summary(), g, vx + 125, vy + 20, vw - (120 + 10), 150
    );
    
    g.setColor(Color.WHITE);
    final Assignment task = person.assignment();
    String assignDesc = "None";
    if (task != null) assignDesc = task.activeInfo();
    ViewUtils.drawWrappedString(
      "Assignment: "+assignDesc, g,
      vx + 5, vy + 160, vw - 10, 40
    );

    g.setColor(Color.LIGHT_GRAY);
    if (tabMode == TAB_SKILLS) {
      renderSkills(surface, g, person);
    }
    if (tabMode == TAB_GEAR) {
      renderGear(surface, g, person);
    }
    if (tabMode == TAB_BONDS) {
      renderBonds(surface, g, person);
    }
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
    
    return true;
  }
  
  
  void performNavigation(int actionID, Person person) {
    if (actionID == ACTION_BACK) {
      mainView.setSelection(null);
      return;
    }
    
    final Base base = mainView.world().playerBase();
    final Series <Person> roster = base.roster();
    int personID = base.rosterIndex(person);
    
    if (actionID == ACTION_NEXT) {
      personID = (personID + 1) % roster.size();
      mainView.setSelection(base.atRosterIndex(personID));
    }
    if (actionID == ACTION_LAST) {
      personID = (personID + roster.size() - 1) % roster.size();
      mainView.setSelection(base.atRosterIndex(personID));
    }
  }
  
  
  void renderSkills(Surface surface, Graphics2D g, Person person) {
    
    Skill hovered = null;
    
    g.setColor(Color.WHITE);
    int down = 160 + 60 + 10;
    
    for (Skill t : ALL_STATS) {
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
      else if (hovered.roots.length == 0) {
        desc += "\n  Adds 1/3 value to derived skills";
        desc += ", XP: "+((int) (XP * 100))+"%";
      }
      else {
        desc += "\n  +"+bonus+" bonus from: ";
        for (Skill r : hovered.roots) {
          desc += r;
          if (r != Visit.last(hovered.roots)) desc += " plus ";
        }
        desc += ", XP: "+((int) (XP * 100))+"%";
      }
      desc = hovered.description + desc;
      
      ViewUtils.drawWrappedString(
        desc, g, vx + 20, vy + down + (15 * 20), 300, 100
      );
    }
  }
  

  void renderGear(Surface surface, Graphics2D g, Person person) {
    int down = 160 + 60 + 10;
    
    for (int slotID : PersonGear.ALL_SLOTS) {
      Equipped inSlot = person.gear.equippedInSlot(slotID);
      Image  icon = inSlot == null ? null   : inSlot.icon();
      String desc = inSlot == null ? "None" : inSlot.name;
      String slotName = PersonGear.SLOT_NAMES[slotID];
      
      final boolean hovered = surface.tryHover(
        vx + 5, vy + down, vw - 10, 40, "Slot_"+slotID
      );
      if (hovered) g.setColor(Color.YELLOW);
      else g.setColor(Color.WHITE);
      
      g.drawImage(icon, vx + 5, vy + down, 40, 40, null);
      g.drawString(slotName+": "+desc, vx + 5 + 40 + 5, vy + down + 15);
      
      if (hovered && surface.mouseClicked()) {
        createItemMenu(person, slotID, vx + 5 + 40, vy + down + 20);
      }
      
      down += 40 + 10;
    }
  }
  
  
  void createItemMenu(final Person person, final int slotID, int x, int y) {
    final Batch <Equipped> types = new Batch();
    final BaseStocks stocks = mainView.world().playerBase().stocks;
    
    for (Equipped type : stocks.availableItems(person, slotID)) {
      types.add(type);
    }
    if (types.empty()) return;
    
    mainView.showClickMenu(new ClickMenu <Equipped> (
      types, x, y, mainView
    ) {
      protected Image imageFor(Equipped option) {
        return option.icon();
      }
      
      protected String labelFor(Equipped option) {
        return option.name+" ("+option.describeStats(person)+")";
      }
      
      protected void whenPicked(Equipped option, int optionID) {
        person.gear.equipItem(option, mainView.world().playerBase());
      }
    });
  }
  

  void renderBonds(Surface surface, Graphics2D g, Person person) {
    
    g.setColor(Color.WHITE);
    int down = vy + 160 + 60 + 20;
    
    final String friendDescs[] = {
      "Civil", "Friendly", "Close", "Soulmate"
    };
    final String enemyDescs[] = {
      "Tense", "Unfriendly", "Hostile", "Nemesis"
    };
    
    for (Person other : person.history.sortedBonds()) {
      float value = person.history.valueFor(other);
      
      g.drawImage(other.kind().sprite(), vx + 5, down + 5, 40, 40, null);
      boolean hoverP = surface.tryHover(vx + 5, down + 5, 40, 40, other);
      
      if (hoverP) {
        g.drawImage(mainView.selectCircle, vx + 5, down + 5, 40, 40, null);
        if (surface.mouseClicked()) {
          mainView.setSelection(other);
        }
      }
      
      g.setColor(Color.WHITE);
      String desc = other.name();
      desc += " "+((int) (value * 100))+"% (";
      if (value > 0) desc += friendDescs[(int) (value *  4)];
      else           desc += enemyDescs [(int) (value * -4)];
      desc += ")";
      g.drawString(desc, vx + 5 + 40 + 4, down + 5 + 15);
      
      Color tint = Color.BLUE, back = Color.DARK_GRAY;
      if (value < 0) { value *= -1; tint = Color.RED; }
      
      ViewUtils.renderStatBar(
        vx + 40 + 5 + 5, down + 5 + 20, vw - 60, 20,
        tint, back, value, false, g
      );
      
      down += 40 + 5;
    }
  }
  
}













