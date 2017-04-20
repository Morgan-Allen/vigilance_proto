


package proto.game.scene;
import proto.common.*;
import proto.util.*;
import static proto.game.scene.Scenery.*;




public class SceneGenUtils implements TileConstants {
  
  
  final static int
    VISIT_ALL        = 0,
    VISIT_NO_CORNERS = 1,
    VISIT_MIDDLE     = 2,
    VISIT_RANDOM     = 3,
    VISIT_FLOORS     = 4,
    VISIT_CEILING    = 5
  ;
  
  
  static class AreaCheck {
    SceneType room;
    boolean valid;
  }
  
  static abstract class Tiling {
    abstract void tile(Wall wall, Coord at, int dir);
  }
  

  void visitWalls(Room room, int visitMode, Tiling visit) {
    if (visitMode == VISIT_FLOORS || visitMode == VISIT_CEILING) {
      for (Coord c : room.floor.pieces) {
        visit.tile(room.floor, c, CENTRE);
      }
    }
    else if (visitMode == VISIT_CEILING) {
      for (Coord c : room.ceiling.pieces) {
        visit.tile(room.ceiling, c, CENTRE);
      }
    }
    else for (Wall wall : room.walls) {
      Series <WallPiece> points = wall.pieces;
      final int dir = T_ADJACENT[(room.walls.indexOf(wall) + 3) % 4];
      
      if (visitMode == VISIT_ALL) {
        for (Coord at : points) visit.tile(wall, at, dir);
      }
      if (visitMode == VISIT_NO_CORNERS) {
        for (Coord at : points) {
          if (at == points.first() || at == points.last()) continue;
          visit.tile(wall, at, dir);
        }
      }
      if (visitMode == VISIT_MIDDLE) {
        Coord at = points.atIndex(points.size() / 2);
        visit.tile(wall, at, dir);
      }
      if (visitMode == VISIT_RANDOM) {
        Coord at = points.atIndex(1 + Rand.index(points.size() - 2));
        visit.tile(wall, at, dir);
      }
    }
  }
  
  
  /*
  boolean couldBlockPathing(Kind propType, int atX, int atY) {
    int w = propType.wide(), h = propType.high();
    int isBlocked = -1, firstBlocked = -1, numBlockages = 0;
    boolean blocksPath = false;
    
    for (Coord c : Visit.grid(atX, atY, w, h, 1)) {
      final byte mark = sampleFacing(c.x, c.y, CENTRE);
      if (mark != MARK_FLOOR && mark != MARK_CORRIDOR) return true;
    }
    
    for (Coord c : Visit.perimeter(atX, atY, w, h)) {
      final byte mark = sampleFacing(c.x, c.y, CENTRE);
      final boolean blocked = mark != MARK_FLOOR && mark != MARK_CORRIDOR;
      
      if (mark == MARK_DOORS) blocksPath = true;
      
      if (firstBlocked == -1) {
        firstBlocked = blocked ? 1 : 0;
      }
      if (blocked) {
        if (isBlocked != 1) {
          isBlocked = 1;
          numBlockages++;
        }
      }
      else {
        if (isBlocked != 0) {
          isBlocked = 0;
        }
      }
    }
    if (isBlocked != firstBlocked) numBlockages++;
    
    if (numBlockages >= 2 || blocksPath) {
      return true;
    }
    else {
      return false;
    }
  }
  //*/
  
  
  /*
  public void printMarkup() {
    I.say("\nPrinting markup for scene: "+scene);
    
    for (int y = scene.high; y-- > 0;) {
      I.say("  ");
      for (int x = 0; x < scene.wide; x++) {
        byte b = markup[x][y];
        I.add(b+" ");
      }
    }
    I.say("\n");
  }
  
  
  public void printMarkupVisually() {
    
    int colorVals[][] = new int[scene.wide][scene.high];
    final int colorKeys[] = new int[MARKUP_TYPES];
    
    for (int i = MARKUP_TYPES; i-- > 0;) {
      float hue = i * 1f / MARKUP_TYPES;
      colorKeys[i] = java.awt.Color.HSBtoRGB(hue, 1, 0.5f);
    }
    
    for (int y = scene.high; y-- > 0;) {
      for (int x = 0; x < scene.wide; x++) {
        byte b = markup[x][y];
        colorVals[x][y] = colorKeys[Nums.clamp(b, MARKUP_TYPES)];
      }
    }
    
    int winW = scene.wide * 10, winH = scene.high * 10;
    I.present(colorVals, "Generated Scene", winW, winH);
  }
  //*/
}
