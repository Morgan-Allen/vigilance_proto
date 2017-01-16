

package proto.content.places;
import proto.common.*;
import proto.game.scene.*;
import static proto.game.scene.SceneType.*;
import static proto.game.scene.SceneTypeGrid.*;



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
    BAR_ROOM_PROP_TYPES[] = {
      KIND_FLOOR,
      KIND_WALL,
      KIND_DOOR,
      KIND_WINDOW,
      KIND_POOL_TABLE,
      KIND_BAR_TABLE,
      KIND_BAR_STOOL,
      KIND_BAR_STOOLS,
      KIND_BAR_TAPS,
      KIND_JUKEBOX,
      KIND_PINBALL_MACHINE,
    }
  ;
  
  final public static SceneTypeFixed ROOM_MAIN_BAR = new SceneTypeFixed(
    "main bar", "type_main_bar_urban",
    BAR_ROOM_PROP_TYPES,
    new byte[][] {
      { 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0 },
      { 0, 7, 6, 7, 7, 0 },
      { 0, 8, 8, 8, 1, 0 },
      { 0, 8, 8, 8, 1, 0 },
      { 1, 1, 1, 1, 1, 1 },
    }
  );
  final public static SceneTypeFixed ROOM_POOL_AREA = new SceneTypeFixed(
    "pool area", "type_pool_area_urban",
    BAR_ROOM_PROP_TYPES,
    new byte[][] {
      { 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0 },
      { 0, 4, 4, 4, 0, 0 },
      { 0, 4, 4, 4, 0, 0 },
      { 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0 },
    }
  );
  final public static SceneTypeFixed ROOM_SEATING = new SceneTypeFixed(
    "seating", "type_seating_urban",
    BAR_ROOM_PROP_TYPES,
    new byte[][] {
      { 0, 0, 0, 0, 0, 0 },
      { 0, 6, 6, 0, 9, 0 },
      { 0, 5, 5, 0, 0, 0 },
      { 0, 7, 6, 0, 1, 0 },
      { 0, 0, 0, 0, 10, 0 },
      { 0, 0, 0, 0, 0, 0 },
    }
  );
  
  
  final public static Kind
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
    ),
    BATHROOM_PROP_TYPES[] = {
      KIND_BATHROOM_FLOOR,
      KIND_WALL,
      KIND_DOOR,
      KIND_WINDOW,
      KIND_BATHROOM_STALL,
      KIND_BASIN
    }
  ;
  
  final public static SceneTypeFixed ROOM_BATHROOM = new SceneTypeFixed(
    "bathroom", "type_bathroom_urban",
    BATHROOM_PROP_TYPES,
    new byte[][] {
      { 1, 1, 1, 1, 1, 1 },
      { 1, 5, 0, 0, 0, 1 },
      { 1, 0, 0, 4, 4, 1 },
      { 1, 1, 0, 4, 4, 1 },
      { 1, 5, 0, 4, 4, 1 },
      { 1, 1, 1, 1, 1, 1 },
    }
  );
  
  final public static SceneType URBAN_SCENE = new SceneTypeGrid(
    "urban scene", "type_urban_scene",
    4, 1,
    numberUnit (ROOM_BATHROOM , 1 ),
    numberUnit (ROOM_MAIN_BAR , 1 ),
    percentUnit(ROOM_POOL_AREA, 33),
    percentUnit(ROOM_SEATING  , 67)
  );
  
  final public static SceneType MANSION_SCENE = new SceneTypeCorridors(
    "mansion scene", "type_mansion_scene",
    BORDERS, KIND_WALL      ,
    DOOR   , KIND_DOOR      ,
    WINDOW , KIND_WINDOW    ,
    FLOORS , KIND_FLOOR     
  );
  
}








