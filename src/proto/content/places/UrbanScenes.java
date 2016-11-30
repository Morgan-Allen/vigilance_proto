

package proto.content.places;
import proto.common.*;
import proto.game.scene.*;
import static proto.game.scene.SceneType.*;



public class UrbanScenes {
  

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
  
  
  final public static SceneType URBAN_SCENE = new SceneType(
    "urban scene", "type_urban_scene",
    BORDERS, KIND_WALL      ,
    DOOR   , KIND_DOOR      ,
    WINDOW , KIND_WINDOW    ,
    FLOORS , KIND_FLOOR     ,
    PROP   , KIND_POOL_TABLE
  );
  
  final public static SceneType MANSION_SCENE = new SceneType(
    "mansion scene", "type_mansion_scene",
    BORDERS, KIND_WALL      ,
    DOOR   , KIND_DOOR      ,
    WINDOW , KIND_WINDOW    ,
    FLOORS , KIND_FLOOR     
  );
  
}






