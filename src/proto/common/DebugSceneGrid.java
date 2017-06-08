

package proto.common;
import proto.game.world.*;
import proto.content.items.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.content.agents.*;
import proto.content.places.*;



public class DebugSceneGrid extends RunGame {
  
  
  final static String IMG_DIR = "media assets/testing/test tiles/";
  final public static PropType
    KIND_WALL = new PropType(
      "Wall", "test_prop_wall",
      IMG_DIR+"tile_wall.png",
      Kind.SUBTYPE_WALLING, 1, 0, Kind.BLOCK_FULL, true
    ),
    KIND_FLOOR = new PropType(
      "Floor", "test_prop_floor",
      IMG_DIR+"tile_floor.png",
      Kind.SUBTYPE_WALLING, 1, 1, Kind.BLOCK_NONE, false
    ),
    KIND_DOOR = new PropType(
      "Door", "test_prop_door",
      IMG_DIR+"tile_door.png",
      Kind.SUBTYPE_WALLING, 1, 0, Kind.BLOCK_PARTIAL, true,
      PlacesCommon.ALARMED, 1
    ),
    KIND_WINDOW = new PropType(
      "Window", "test_prop_window",
      IMG_DIR+"tile_window.png",
      Kind.SUBTYPE_WALLING, 1, 0, Kind.BLOCK_FULL, false,
      PlacesCommon.ALARMED, 1
    ),
    KIND_COLUMN = new PropType(
      "Column", "test_prop_column",
      IMG_DIR+"tile_column.png",
      Kind.SUBTYPE_WALLING, 1, 1, Kind.BLOCK_FULL, true
    ),
    KIND_BIG_TABLE = new PropType(
      "Table", "test_prop_big_table",
      IMG_DIR+"tile_big_table.png",
      Kind.SUBTYPE_FURNISH, 3, 2, Kind.BLOCK_FULL, false
    ),
    KIND_SMALL_TABLE = new PropType(
      "Bar Table", "test_prop_small_table",
      IMG_DIR+"tile_small_table.png",
      Kind.SUBTYPE_FURNISH, 2, 1, Kind.BLOCK_FULL, false
    ),
    KIND_CHAIR = new PropType(
      "Bar Stool", "test_prop_chair",
      IMG_DIR+"tile_chair.png",
      Kind.SUBTYPE_FURNISH, 1, 1, Kind.BLOCK_PARTIAL, false
    ),
    KIND_ROOM_OBJECT = new PropType(
      "Jukebox", "test_prop_room_object",
      IMG_DIR+"tile_room_object.png",
      Kind.SUBTYPE_FURNISH, 1, 1, Kind.BLOCK_FULL, true
    );
  
  
  final public static SceneType GRID_TEST_SCENE = new SceneTypeGrid(
    "fixed test scene", "type_urban_scene_fixed",
    KIND_FLOOR,
    new PropType[] {
      KIND_COLUMN     ,
      KIND_DOOR       ,
      KIND_WINDOW     ,
      KIND_BIG_TABLE  ,
      KIND_SMALL_TABLE,
      KIND_CHAIR      ,
      KIND_ROOM_OBJECT,
    },
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 1, 0 },
      { 0, 0, 0, 0, 0, 0, 7, 6 },
      { 4, 4, 4, 0, 0, 0, 0, 0 },
      { 4, 4, 4, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 6, 0 },
      { 1, 0, 6, 6, 0, 5, 5, 0 },
      { 7, 0, 5, 5, 0, 6, 6, 0 },
      { 0, 0, 6, 6, 0, 0, 0, 0 },
    }
  );
  
  
  public static void main(String args[]) {
    GameSettings.debugScene      = true ;
    GameSettings.reportWorldInit = false;
    runGame(new DebugSceneGrid(), "saves/debug_scene_grid");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world, false);
    //
    //  Generate the scene-
    final Scene mission = new Scene(world, 12, 12);
    mission.setupScene(true);
    //*
    SceneType sceneType = GRID_TEST_SCENE;
    Scenery   gen       = sceneType.generateScenery(world, 8, 8, true);
    sceneType.applyScenery(mission, gen, 2, 2, TileConstants.E, true);
    for (int y = mission.high() - 1; y-- > 1;) {
      PropType kind = y == 6 ? KIND_DOOR : KIND_WALL;
      mission.addProp(kind, 4, y, TileConstants.E, world);
    }
    //*/
    /*
    mission.addProp(KIND_POOL_TABLE, 0, 0, TileConstants.N);
    mission.addProp(KIND_POOL_TABLE, 8, 0, TileConstants.E);
    mission.addProp(KIND_POOL_TABLE, 8, 8, TileConstants.S);
    mission.addProp(KIND_POOL_TABLE, 0, 8, TileConstants.W);
    
    for (int x = 4; x-- > 0;) {
      int dir = TileConstants.T_ADJACENT[x];
      mission.addProp(KIND_WALL   , 1 + (x * 2), 4, dir);
      mission.addProp(KIND_JUKEBOX, 1 + (x * 2), 3, dir);
    }
    //*/
    Tile.printWallsMask(mission);
    //
    //  Then introduce the agent/s themselves-
    final Base base = world.playerBase();
    int across = (mission.wide() - 0) / 2;
    Person hero = base.roster().first();
    hero.gear.equipItem(Gadgets.TEAR_GAS, PersonGear.SLOT_ITEM_1);
    hero.gear.equipItem(Gadgets.BOLAS   , PersonGear.SLOT_ITEM_2);
    hero.stats.setLevel(Techniques.STEADY_AIM, 1, true);
    hero.stats.setLevel(Techniques.VIGILANCE , 1, true);
    hero.addAssignment(mission);
    mission.enterScene(hero, 0, across++);
    hero.onTurnStart();
    
    hero.actions.assignAction(Common.GUARD.configAction(
      hero, hero.currentTile(), hero, mission, null, null
    ));
    
    //
    //  And a random goon-
    Person goon = Person.randomOfKind(Crooks.BRUISER, world);
    goon.addAssignment(mission);
    mission.enterScene(goon, mission.wide() - 1, across++);
    goon.onTurnStart();
    goon.actions.assignAction(Common.STRIKE.configAction(
      goon, hero.currentTile(), hero, mission, null, null
    ));
    //
    //  Then enter and return-
    world.enterScene(mission);
    return world;
  }
}






