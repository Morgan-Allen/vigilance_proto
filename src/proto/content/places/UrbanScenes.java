

package proto.content.places;
import proto.common.*;
import proto.game.scene.*;
import static proto.game.scene.SceneType.*;



public class UrbanScenes {
  

  final static String IMG_DIR = "media assets/scene layout/bar scene/";
  final public static Kind
    KIND_WALL = Kind.ofProp(
      "Wall", "prop_wall_urban",
      IMG_DIR+"sprite_wall.png",
      1, 1, true, true
    ),
    KIND_FLOOR = Kind.ofProp(
      "Floor", "prop_floor_urban",
      IMG_DIR+"sprite_floor.png",
      1, 1, false, false
    ),
    KIND_DOOR = Kind.ofProp(
      "Door", "prop_door_urban",
      IMG_DIR+"sprite_door.png",
      1, 1, false, true,
      Facilities.ALARMED, 1
    ),
    KIND_WINDOW = Kind.ofProp(
      "Window", "prop_window_urban",
      IMG_DIR+"sprite_window.png",
      1, 1, true, false,
      Facilities.ALARMED, 1
    ),
    
    KIND_POOL_TABLE = Kind.ofProp(
      "Pool Table", "prop_pool_table_urban",
      IMG_DIR+"sprite_pool_table.png",
      3, 2, true, false
    ),
    KIND_BAR_TABLE = Kind.ofProp(
      "Bar Table", "prop_bar_table_urban",
      IMG_DIR+"sprite_bar_table.png",
      2, 1, true, false
    ),
    KIND_BAR_STOOL = Kind.ofProp(
      "Bar Stool", "prop_bar_stool_urban",
      IMG_DIR+"sprite_bar_stool.png",
      1, 1, false, false
    ),
    KIND_BAR_STOOLS = Kind.ofProp(
      "Bar Stools", "prop_bar_stools_urban",
      IMG_DIR+"sprite_bar_stools.png",
      1, 1, false, false
    ),
    KIND_BAR_TAPS = Kind.ofProp(
      "Bar Taps", "prop_bar_taps_urban",
      IMG_DIR+"sprite_bar_taps.png",
      3, 2, true, false
    ),
    KIND_JUKEBOX = Kind.ofProp(
      "Jukebox", "prop_jukebox_urban",
      IMG_DIR+"sprite_jukebox.png",
      1, 1, true, true
    ),
    KIND_PINBALL_MACHINE = Kind.ofProp(
      "Pinball Machine", "prop_pinball_machine_urban",
      IMG_DIR+"sprite_pinball_machine.png",
      1, 1, true, true
    ),
    
    KIND_BATHROOM_FLOOR = Kind.ofProp(
      "Bathroom Floor", "prop_bathroom_floor_urban",
      IMG_DIR+"sprite_bathroom_floor.png",
      1, 1, false, false
    ),
    KIND_BATHROOM_STALL = Kind.ofProp(
      "Bathroom Stall", "prop_bathroom_stall_urban",
      IMG_DIR+"sprite_bathroom_stall.png",
      2, 1, true, true
    ),
    KIND_BASIN = Kind.ofProp(
      "Basin", "prop_basin_urban",
      IMG_DIR+"sprite_basin.png",
      1, 1, false, false
    )
    ;
  
  
  final public static SceneType URBAN_SCENE = new SceneTypeCorridors(
    "urban scene", "type_urban_scene",
    BORDERS, KIND_WALL      ,
    DOOR   , KIND_DOOR      ,
    WINDOW , KIND_WINDOW    ,
    FLOORS , KIND_FLOOR     ,
    PROP   , KIND_POOL_TABLE
  );
  
  final public static SceneType MANSION_SCENE = new SceneTypeCorridors(
    "mansion scene", "type_mansion_scene",
    BORDERS, KIND_WALL      ,
    DOOR   , KIND_DOOR      ,
    WINDOW , KIND_WINDOW    ,
    FLOORS , KIND_FLOOR     
  );
  
}








