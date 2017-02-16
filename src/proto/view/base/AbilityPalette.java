

package proto.view.base;
import proto.common.*;
import proto.game.world.*;
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
}








