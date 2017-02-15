

package proto.view.base;
import proto.common.*;
import proto.game.person.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class AbilityPalette {
  
  
  final int wide, high;
  final Ability grid[][];
  
  
  public AbilityPalette(int wide, int high) {
    this.wide = wide;
    this.high = high;
    this.grid = new Ability[wide][high];
  }
  
  
  public void attachAbility(Ability a, int gridX, int gridY) {
    try { grid[gridX][gridY] = a; }
    catch(ArrayIndexOutOfBoundsException e) {
      I.complain("Wrong palette coords!");
    }
  }
  
  
  Coord gridLocation(Ability a) {
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      if (grid[c.x][c.y] == a) return c;
    }
    return null;
  }
  
  
  void renderTo(
    Surface surface, Graphics2D g, int offX, int offY, UINode parent
  ) {
    int iconSize = 40, padding = 15;
    
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      final Ability a = grid[c.x][c.y];
      if (a == null) continue;
      
      Image icon = a.icon();
      int x = c.x * (iconSize + padding), y = c.y * (iconSize + padding);
      int w = iconSize, h = iconSize;
      x += offX + padding / 2;
      y += offY + padding / 2;
      
      Box2D bound = new Box2D(x, y, w, h);
      
      g.setColor(Color.LIGHT_GRAY);
      x += parent.relBounds.xpos();
      y += parent.relBounds.ypos();
      g.drawRect(x - 2, y - 2, w + 4, h + 5);
      
      /*
      Image icon = a.icon();
      Box2D bound = new Box2D(c.x * iconSize, c.y * iconSize, 0, 0);
      bound.incX(offX);
      bound.incY(offY);
      bound.xdim(iconSize);
      bound.ydim(iconSize);
      
      g.setColor(Color.LIGHT_GRAY);
      g.drawRect(
        (int) parent.relBounds.xpos() + offX + (c.x * iconSize),
        (int) parent.relBounds.ypos() + offY + (c.y * iconSize),
        iconSize, iconSize
      );
      //*/
      
      ImageButton b = new ImageButton(icon, bound, parent) {
        protected void whenClicked() {
          I.say("  Ability picked: "+a);
        }
      };
      b.refers = a;
      b.renderNow(surface, g);
    }
  }
}





