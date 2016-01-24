

package proto;
import static proto.Common.*;
import static proto.Person.*;
import util.*;



public class UrbanScene extends Scene {
  
  
  final static String IMG_DIR = "media assets/bar scene/";
  final static Kind
    KIND_WALL = Kind.ofProp(
      "Wall", IMG_DIR+"sprite_wall.png",
      1, 1, true, true
    ),
    KIND_FLOOR = Kind.ofProp(
      "Floor", IMG_DIR+"sprite_floor.png",
      1, 1, false, false
    ),
    KIND_DOOR = Kind.ofProp(
      "Door", IMG_DIR+"sprite_door.png",
      1, 1, false, true
    ),
    KIND_WINDOW = Kind.ofProp(
      "Window", IMG_DIR+"sprite_window.png",
      1, 1, true, false
    ),
    KIND_POOL_TABLE = Kind.ofProp(
      "Pool Table", IMG_DIR+"sprite_pool_table.png",
      3, 2, true, false
    );
  
  final static Kind
    KIND_GOON = Kind.ofPerson(
      "Goon", IMG_DIR+"sprite_big_goon.png",
      Kind.TYPE_MOOK,
      HEALTH, 20 ,
      ARMOUR, 0  ,
      MUSCLE, 16 ,
      BRAIN , 6  ,
      SPEED , 10 ,
      SIGHT , 6  ,
      MOVE, 1, STRIKE, 1
    );
  
  
  public UrbanScene(World world, int size) {
    super(world, size);
  }
  
  
  void beginScene() {
    
    int cX = (size / 2) - 10, cY = 5;
    
    for (Coord c : Visit.grid(cX, cY, 20, 20, 1)) {
      addProp(KIND_FLOOR, c.x, c.y);
    }
    for (Coord c : Visit.grid(cX - 1, cY - 1, 22, 22, 1)) {
      if (tileAt(c.x, c.y).prop != null) continue;
      
      if (c.x == cX + 10 || c.y == cY + 10) {
        addProp(KIND_DOOR, c.x, c.y);
      }
      else {
        addProp(KIND_WALL, c.x, c.y);
      }
    }
    addProp(KIND_POOL_TABLE, cX + 10, cY + 10);
    
    for (int n = 10; n-- > 0;) {
      Person p = new Person(KIND_GOON, "Goon");
      int x = cX + 5 + Rand.index(10), y = cY + 5 + Rand.index(10);
      Tile under = tileAt(x, y);
      if (blockedAt(under) || under.standing != null) continue;
      addPerson(p, x, y);
    }
    
    //  TODO:  Add mooks and objectives!  Check for completion!  And add
    //  effects on crime/trust depending on success/failure/collateral!
    
    //  TODO:  Also, some patrol routes for goons would be nice?
    super.beginScene();
  }
  
  
}








