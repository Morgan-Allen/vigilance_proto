

package proto.content.scenes;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;
import static proto.util.TileConstants.*;

import proto.content.agents.Crooks;



//  Get furniture from the region.  Get forces from the event.

public class UrbanScene extends Scene {
  
  
  final static String IMG_DIR = "media assets/scene layout/bar scene/";
  final static Kind
    KIND_WALL = Kind.ofProp(
      "Wall", "prop_wall_urban", IMG_DIR+"sprite_wall.png",
      1, 1, true, true
    ),
    KIND_FLOOR = Kind.ofProp(
      "Floor", "prop_floor_urban", IMG_DIR+"sprite_floor.png",
      1, 1, false, false
    ),
    KIND_DOOR = Kind.ofProp(
      "Door", "prop_door_urban", IMG_DIR+"sprite_door.png",
      1, 1, false, true
    ),
    KIND_WINDOW = Kind.ofProp(
      "Window", "prop_window_urban", IMG_DIR+"sprite_window.png",
      1, 1, true, false
    ),
    KIND_POOL_TABLE = Kind.ofProp(
      "Pool Table", "prop_pool_table_urban", IMG_DIR+"sprite_pool_table.png",
      3, 2, true, false
    );
  
  
  
  
  public UrbanScene(World world, int size) {
    super(world, size);
  }
  
  
  public UrbanScene(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public void assignMissionParameters(
    Lead trigger, Place site,
    float dangerLevel, int expireTime, Series<Person> forces
  ) {
    super.assignMissionParameters(
      trigger, site, dangerLevel, expireTime, forces
    );
    if (forces == null) generateForces();
  }


  private void generateForces() {
    final World world = this.world();
    /*
    final Kind BOSSES[] = {
      Villains.KIND_SLADE,
      Villains.KIND_MR_FREEZE
    };
    //*/
    final Kind GOONS[] = {
      Crooks.MOBSTER,
      Crooks.MOBSTER,
      Crooks.MOBSTER
    };
    final float GOON_CHANCES[] = { 3, 2, 1 };
    final float GOON_POWERS [] = { 1, 1, 1 };
    
    float forceLimit = dangerLevel() * 10;
    float bossChance = dangerLevel() / 2;
    float hostages   = 1 + Rand.index(3);
    float forceSum   = 0;
    
    while (hostages-- > 0) {
      Person hostage = new Person(Crooks.CIVILIAN, world);
      addToTeam(hostage);
    }
    while (forceSum < forceLimit) {
      Kind ofGoon = (Kind) Rand.pickFrom(GOONS, GOON_CHANCES);
      Person goon = new Person(ofGoon, world);
      forceSum += GOON_POWERS[Visit.indexOf(ofGoon, GOONS)];
      addToTeam(goon);
    }
    
    I.say("Scene generated, team is: ");
    for (Person p : this.othersTeam()) I.say("  "+p);
  }
  
  
  private Tile findEntryPoint(Person p, int nearX, int nearY) {
    int x = nearX + 5 + Rand.index(10), y = nearY + 5 + Rand.index(10);
    Tile under = tileAt(x, y);
    int dir = T_INDEX[Rand.index(T_INDEX.length)], size = size();
    
    while (under != null) {
      if (x != Nums.clamp(x, size) || y != Nums.clamp(y, size)) break;
      if (! under.blocked()) return under;
      x += T_X[dir];
      y += T_Y[dir];
      under = tileAt(x, y);
    }
    return null;
  }
  
  
  public void setupScene() {
    super.setupScene();
    int cX = (size() / 2) - 10, cY = 5;
    
    for (Coord c : Visit.grid(cX, cY, 20, 20, 1)) {
      addProp(KIND_FLOOR, c.x, c.y);
    }
    for (Coord c : Visit.grid(cX - 1, cY - 1, 22, 22, 1)) {
      if (tileAt(c.x, c.y).prop() != null) continue;
      
      if (c.x == cX + 10 || c.y == cY + 10) {
        addProp(KIND_DOOR, c.x, c.y);
      }
      else {
        addProp(KIND_WALL, c.x, c.y);
      }
    }
    addProp(KIND_POOL_TABLE, cX + 10, cY + 10);
    
    for (Person p : othersTeam()) {
      Tile entry = findEntryPoint(p, cX, cY);
      
      I.say("Finding entry for "+p+", at: "+entry);
      if (entry != null) enterScene(p, entry.x, entry.y);
      else I.say("  COULD NOT FIND ENTRY FOR: "+p);
    }
    
    int across = (size() - (playerTeam().size())) / 2;
    for (Person p : playerTeam()) {
      enterScene(p, across++, 0);
    }
    
    //  TODO:  Some patrol routes for goons would be nice...?
  }
  
  
  public void beginScene() {
    super.beginScene();
  }
  //*/
  
}








