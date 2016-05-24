

package proto.view;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class PersonView {
  
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
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  int tabMode = TAB_SKILLS;
  
  
  PersonView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent;
    this.viewBounds = viewBounds;
  }
  
  

  void renderTo(Surface surface, Graphics2D g) {
    
    final Person person = parent.rosterView.selected();
    
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    
    g.setColor(Color.WHITE);
    g.drawImage(person.kind().sprite(), vx, vy, 120, 120, null);
    
    g.drawString("Codename: "+person.name(), vx + 125, vy + 40);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      person.history.summary(), g, vx + 125, vy + 40, vw - (120 + 10), 80
    );
    
    final String navNames[] = { "Back", "Last", "Next" };
    for (int i = 0; i < 3; i++) {
      final int actionID = i;
      StringButton navButton = new StringButton(
        navNames[i], vx + 120 + 5 + (60 * i), vy + 0, 60, 20, this
      ) {
        void whenClicked() {
          performNavigation(actionID, person);
        }
      };
      navButton.renderTo(surface, g);
    }
    
    final String tabNames[] = { "Skills", "Gear", "Bonds" };
    for (int i = 0; i < 3; i++) {
      final int modeID = i + 1;
      StringButton tabButton = new StringButton(
        tabNames[i], vx + 120 + 5 + (60 * i), vy + 120, 60, 20, this
      ) {
        void whenClicked() {
          tabMode = modeID;
        }
      };
      tabButton.toggled = tabMode == modeID;
      tabButton.renderTo(surface, g);
    }
    
    g.setColor(Color.WHITE);
    final Assignment task = person.assignment();
    String assignDesc = "None";
    if (task != null) assignDesc = task.description();
    //g.drawString("Assigned to: "+assignDesc, vx + 5, vy + 120 + 35);
    ViewUtils.drawWrappedString(
      "Assigned to: "+assignDesc, g,
      vx + 5, vy + 120 + 20, vw - 10, 40
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
  }
  
  
  void performNavigation(int actionID, Person person) {
    if (actionID == ACTION_BACK) {
      parent.rosterView.setSelection(null);
      return;
    }
    
    final Base base = parent.world.base();
    final Series <Person> roster = base.roster();
    int personID = base.rosterIndex(person);
    
    if (actionID == ACTION_NEXT) {
      personID = (personID + 1) % roster.size();
      parent.rosterView.setSelection(base.atRosterIndex(personID));
    }
    if (actionID == ACTION_LAST) {
      personID = (personID + roster.size() - 1) % roster.size();
      parent.rosterView.setSelection(base.atRosterIndex(personID));
    }
  }
  
  
  void renderSkills(Surface surface, Graphics2D g, Person person) {
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    Stat hovered = null;
    
    g.setColor(Color.WHITE);
    int down = 120 + 60 + 10;
    
    for (Stat t : ALL_STATS) {
      int index = Visit.indexOf(t, STAT_DISPLAY_COORDS);
      if (index == -1) continue;
      
      int level = person.stats.levelFor(t);
      int x = (Integer) STAT_DISPLAY_COORDS[index + 1];
      int y = (Integer) STAT_DISPLAY_COORDS[index + 2];
      x *= 150;
      y *=  20;
      Color forT = Color.LIGHT_GRAY;
      
      if (surface.mouseIn(vx + x + 20, vy + y + down, 150, 20, this)) {
        hovered = t;
        forT = Color.YELLOW;
      }
      
      g.setColor(forT);
      g.drawString(t.name  , vx + x + 20      , vy + y + down + 15);
      g.drawString(""+level, vx + x + 20 + 100, vy + y + down + 15);
    }
    
    if (hovered != null) {
      g.setColor(Color.LIGHT_GRAY);
      
      String desc = "";
      if (hovered.roots.length == 0) {
        
      }
      else {
        desc += "\n  Bonus from: ";
        for (Stat r : hovered.roots) {
          desc += r;
          if (r != Visit.last(hovered.roots)) desc += " plus ";
        }
      }
      desc = hovered.description + desc;
      
      ViewUtils.drawWrappedString(
        desc, g, vx + 20, vy + down + (15 * 20), 300, 100
      );
    }
  }
  

  void renderGear(Surface surface, Graphics2D g, Person person) {
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    
    g.setColor(Color.WHITE);
    int down = 120 + 60 + 10;
    
    for (int slotID : Person.ALL_SLOTS) {
      Equipped inSlot = person.equippedInSlot(slotID);
      String desc = inSlot == null ? "None" : inSlot.name;
      String slotName = Person.SLOT_NAMES[slotID];
      g.drawString(slotName+": "+desc, vx + 5, vy + down + 15);
      down += 40 + 10;
    }
    
  }
  

  void renderBonds(Surface surface, Graphics2D g, Person person) {
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    
    g.setColor(Color.WHITE);
    int down = 120 + 60 + 10;
    
    for (Person other : person.bonds.sortedBonds()) {
      g.drawImage(other.kind().sprite(), vx + 5, down, 40, 40, null);
      
      float value = person.bonds.valueFor(other);
      Color tint = Color.BLUE, back = Color.GRAY;
      if (value < 0) { value *= -1; tint = Color.RED; }
      
      ViewUtils.renderStatBar(
        vx + 40 + 5 + 5, down + 5, vw - 60, 40, tint, back, value, false, g
      );
      down += 40 + 5;
    }
  }
  
}













