

package proto.content.places;
import proto.common.*;
import proto.game.scene.*;
import static proto.game.scene.SceneType.*;
import static proto.game.scene.SceneTypeGrid.*;



public class UrbanScenes {
  

  final static String IMG_DIR = "media assets/scene layout/bar scene/";
  final public static PropType
    KIND_WALL = new PropType(
      "Wall", "prop_wall_urban",
      IMG_DIR+"sprite_wall_thin.png",
      1, 0, Kind.BLOCK_FULL, true
    ),
    KIND_THICK_WALL = new PropType(
      "Thick Wall", "prop_thick_wall_urban",
      IMG_DIR+"sprite_wall.png",
      1, 1, Kind.BLOCK_FULL, true
    ),
    KIND_FLOOR = new PropType(
      "Floor", "prop_floor_urban",
      IMG_DIR+"sprite_floor.png",
      1, 1, Kind.BLOCK_NONE, false
    ),
    KIND_DOOR = new PropType(
      "Door", "prop_door_urban",
      IMG_DIR+"sprite_door_thin.png",
      1, 0, Kind.BLOCK_PARTIAL, true,
      PlacesCommon.ALARMED, 1
    ),
    KIND_WINDOW = new PropType(
      "Window", "prop_window_urban",
      IMG_DIR+"sprite_window_thin.png",
      1, 0, Kind.BLOCK_FULL, false,
      PlacesCommon.ALARMED, 1
    ),
    
    KIND_POOL_TABLE = new PropType(
      "Pool Table", "prop_pool_table_urban",
      IMG_DIR+"sprite_pool_table.png",
      3, 2, Kind.BLOCK_FULL, false
    ),
    KIND_BAR_TABLE = new PropType(
      "Bar Table", "prop_bar_table_urban",
      IMG_DIR+"sprite_bar_table.png",
      2, 1, Kind.BLOCK_FULL, false
    ),
    KIND_BAR_STOOL = new PropType(
      "Bar Stool", "prop_bar_stool_urban",
      IMG_DIR+"sprite_bar_stool.png",
      1, 1, Kind.BLOCK_PARTIAL, false
    ),
    KIND_BAR_STOOLS = new PropType(
      "Bar Stools", "prop_bar_stools_urban",
      IMG_DIR+"sprite_bar_stools.png",
      1, 1, Kind.BLOCK_PARTIAL, false
    ),
    KIND_BAR_TAPS = new PropType(
      "Bar Taps", "prop_bar_taps_urban",
      IMG_DIR+"sprite_bar_taps.png",
      3, 2, Kind.BLOCK_FULL, false
    ),
    KIND_JUKEBOX = new PropType(
      "Jukebox", "prop_jukebox_urban",
      IMG_DIR+"sprite_jukebox.png",
      1, 1, Kind.BLOCK_FULL, true
    ),
    KIND_PINBALL_MACHINE = new PropType(
      "Pinball Machine", "prop_pinball_machine_urban",
      IMG_DIR+"sprite_pinball_machine.png",
      1, 1, Kind.BLOCK_FULL, true
    ),
    BAR_ROOM_PROP_TYPES[] = {
      KIND_POOL_TABLE,
      KIND_BAR_TABLE,
      KIND_BAR_STOOL,
      KIND_BAR_STOOLS,
      KIND_BAR_TAPS,
      KIND_JUKEBOX,
      KIND_PINBALL_MACHINE,
      KIND_THICK_WALL
    }
  ;
  
  final public static SceneTypeFixed ROOM_MAIN_BAR = new SceneTypeFixed(
    "main bar", "type_main_bar_urban",
    KIND_FLOOR, BAR_ROOM_PROP_TYPES,
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 4, 3, 4, 0, 4, 4, 3, 0 },
      { 5, 5, 5, 0, 5, 5, 5, 0 },
      { 5, 5, 5, 0, 5, 5, 5, 0 },
      { 8, 8, 8, 0, 8, 8, 8, 0 },
      { 4, 3, 0, 0, 0, 0, 3, 0 },
      { 2, 2, 0, 2, 2, 0, 2, 2 },
    }
  );
  final public static SceneTypeFixed ROOM_POOL_AREA = new SceneTypeFixed(
    "pool area", "type_pool_area_urban",
    KIND_FLOOR, BAR_ROOM_PROP_TYPES,
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 4, 3, 0 },
      { 0, 1, 1, 1, 0, 2, 2, 0 },
      { 0, 1, 1, 1, 0, 4, 4, 0 },
      { 0, 0, 0, 0, 0, 3, 4, 0 },
      { 0, 0, 0, 0, 0, 2, 2, 0 },
      { 0, 1, 1, 1, 0, 3, 3, 0 },
      { 0, 1, 1, 1, 0, 0, 8, 0 },
      { 0, 0, 0, 0, 0, 0, 7, 0 },
    }
  );
  final public static SceneTypeFixed ROOM_SEATING = new SceneTypeFixed(
    "seating", "type_seating_urban",
    KIND_FLOOR, BAR_ROOM_PROP_TYPES,
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 3, 3 },
      { 3, 4, 0, 0, 0, 0, 2, 2 },
      { 2, 2, 3, 0, 8, 0, 4, 4 },
      { 3, 0, 0, 0, 6, 0, 4, 0 },
      { 4, 3, 0, 0, 0, 0, 2, 2 },
      { 2, 2, 0, 0, 8, 0, 4, 3 },
      { 3, 0, 0, 0, 7, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
    }
  );
  
  
  final public static PropType
    KIND_BATHROOM_FLOOR = new PropType(
      "Bathroom Floor", "prop_bathroom_floor_urban",
      IMG_DIR+"sprite_bathroom_floor.png",
      1, 1, Kind.BLOCK_NONE, false
    ),
    KIND_BATHROOM_STALL = new PropType(
      "Bathroom Stall", "prop_bathroom_stall_urban",
      IMG_DIR+"sprite_bathroom_stall.png",
      2, 1, Kind.BLOCK_FULL, true
    ),
    KIND_BASIN = new PropType(
      "Basin", "prop_basin_urban",
      IMG_DIR+"sprite_basin.png",
      1, 1, Kind.BLOCK_PARTIAL, false
    ),
    BATHROOM_PROP_TYPES[] = {
      KIND_BATHROOM_STALL,
      KIND_BASIN,
      KIND_THICK_WALL
    }
  ;
  
  final public static SceneTypeFixed ROOM_BATHROOM = new SceneTypeFixed(
    "bathroom", "type_bathroom_urban",
    KIND_BATHROOM_FLOOR, BATHROOM_PROP_TYPES,
    8, 8, new byte[][] {
      { 2, 0, 1, 1, 3, 0, 0, 0 },
      { 0, 0, 1, 1, 3, 0, 0, 0 },
      { 3, 0, 1, 1, 3, 0, 0, 0 },
      { 2, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 1, 1, 3, 3, 0, 3 },
      { 3, 0, 1, 1, 3, 2, 0, 2 },
      { 2, 0, 1, 1, 3, 0, 0, 0 },
      { 0, 0, 0, 0, 3, 0, 0, 0 },
    }
  );
  
  final public static SceneType URBAN_SCENE = new SceneTypeGrid(
    "urban scene", "type_urban_scene",
    8, 3,
    KIND_WALL, KIND_DOOR, KIND_WINDOW, KIND_FLOOR,
    numberUnit         (ROOM_MAIN_BAR , WALL_EXTERIOR, 1    ),
    numberOrPercentUnit(ROOM_BATHROOM , WALL_INTERIOR, 20, 1),
    percentUnit        (ROOM_POOL_AREA, WALL_EXTERIOR, 33   ),
    percentUnit        (ROOM_SEATING  , WALL_EXTERIOR, 67   )
  );
  
  final public static SceneType MANSION_SCENE = new SceneTypeCorridors(
    "mansion scene", "type_mansion_scene",
    BORDERS, KIND_WALL  ,
    DOOR   , KIND_DOOR  ,
    WINDOW , KIND_WINDOW,
    FLOORS , KIND_FLOOR 
  );
  
}


