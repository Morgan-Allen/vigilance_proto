

package proto.view;
import proto.util.*;
import proto.game.world.*;
import proto.common.Kind;
import proto.game.person.*;

import java.awt.*;
import java.awt.image.*;



public class RosterView {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  final static Image
    NOT_ASSIGNED  = Kind.loadImage(
      "media assets/action view/assignment_blank.png"
    ),
    ASSIGN_OKAY   = Kind.loadImage(
      "media assets/action view/select_okay.png"
    ),
    ASSIGN_FORBID = Kind.loadImage(
      "media assets/action view/select_illegal.png"
    );
  
  
  final WorldView parent;
  final Box2D viewBounds;
  
  final Color statColors[] = new Color[4];
  final Image statIcons [] = new Image[4];
  
  private Person selectedPerson;
  
  
  RosterView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
    
    statColors[0] = new Color(0.0f, 0.0f, 1.0f);
    statColors[1] = new Color(0.0f, 1.0f, 0.0f);
    statColors[2] = new Color(1.0f, 1.0f, 0.5f);
    statColors[3] = new Color(1.0f, 0.5f, 1.0f);
    
    final String imgPath = "media assets/stat icons/";
    statIcons[0] = Kind.loadImage(imgPath+"icon_intellect.png");
    statIcons[1] = Kind.loadImage(imgPath+"icon_evasion.png"  );
    statIcons[2] = Kind.loadImage(imgPath+"icon_social.png"   );
    statIcons[3] = Kind.loadImage(imgPath+"icon_combat.png"   );
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    Base base = parent.world.base();
    Person personHovered = null;
    Assignment assignTo = parent.baseView.selectedTask();
    
    Image selectCircle = parent.selectCircle;
    final Box2D b = viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    int across = 0, down = 15, size = 75, sizeA = 25, pad = 25, x, y;
    
    for (Person p : base.roster()) {
      
      int nextAcross = across + size + pad;
      if (nextAcross >= vw) {
        across = 0;
        down += size + pad + 50;
      }
      
      x = vx + across;
      y = vy + down;
      across += size + pad;
      
      g.drawImage(p.kind().sprite(), x, y, size, size, null);
      
      g.setColor(Color.LIGHT_GRAY);
      g.drawString(p.name()+" ("+(base.rosterIndex(p) + 1)+")", x, y - 5);
      
      //g.setColor(Color.YELLOW);
      if (surface.mouseIn(x, y, size, size, this)) {
        personHovered = p;
        //g.drawOval(x, y, size, size);
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      if (selectedPerson == p) {
        //g.drawOval(x, y, size, size);
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      
      final Assignment a = p.assignment();
      Image forA = a == null ? null : a.icon();
      if (assignTo != null) {
        if (a == assignTo) forA = ASSIGN_OKAY;
        else forA = ASSIGN_FORBID;
      }
      g.drawImage(forA, x, y + size - sizeA, sizeA, sizeA, null);
      
      /*
      int MH = p.maxHealth(), MS = p.maxStress();
      int inj = (int) p.injury(), fat = (int) p.stun();
      int str = (int) p.stress();
      renderStatBar(
        x + size + 05, y, 5, size + 35,
        Color.RED, Color.BLACK, (MH - inj) * 1f / MH, g
      );
      renderStatBar(
        x + size + 10, y, 5, size + 35,
        Color.LIGHT_GRAY, Color.GRAY, str * 1f / MS, g
      );
      //*/
      
      int index = 0;
      for (Trait t : PersonStats.BASE_STATS) {
        float fill = p.stats.levelFor(t) / 10f;
        ViewUtils.renderStatBar(
          x + (index * 19), y + size + 5, 16, 20,
          statColors[index], null, fill, true, g
        );
        g.drawImage(
          statIcons[index], x + (index * 19), y + size + 17, 16, 16, null
        );
        index++;
      }
    }
    
    if (personHovered != null && surface.mouseClicked(this)) {
      /*
      final Person p = personHovered;
      final Assignment o = p.assignment();
      if (assignTo != null) {
        if (o != null    ) o       .setAssigned(p, false);
        if (o != assignTo) assignTo.setAssigned(p, true );
      }
      else {
        setSelection(personHovered);
      }
      //*/
      setSelection(personHovered);
    }
  }
  
  
  void setSelection(Person selected) {
    selectedPerson = selected;
  }
  
  
  Person selected() {
    return selectedPerson;
  }
}


/*
  void describePersonOutfit(Person p, StringBuffer s) {
    final Surface input = world.game().surface();
    
    s.append("\nCodename: "+p.name());
    s.append("\nItems equipped:");
    for (Equipped item : p.equipment()) {
      s.append("\n  "+item);
    }
    
    s.append("\nItems available:");
    int index = 0;
    for (Equipped item : world.base().itemsAvailableFor(p)) {
      boolean equipped = p.hasEquipped(item);
      boolean canEquip = p.canEquip   (item);
      s.append("\n  "+item.name+" ("+index+")");
      if      (equipped  ) s.append(" (Equipped)");
      else if (! canEquip) s.append(" (No Slot)" );
      
      if (input.isPressed((char) ('0' + index))) {
        if      (equipped) p.removeItem(item);
        else if (canEquip) p.equipItem (item);
      }
    }
  }

//*/






