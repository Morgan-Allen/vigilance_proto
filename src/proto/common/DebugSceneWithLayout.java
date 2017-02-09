

package proto.common;
import proto.game.world.*;
import proto.content.items.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.content.places.UrbanScenes.*;



public class DebugSceneWithLayout extends RunGame {
  
  
  final public static PropType
    KIND_THIN_WALL = new PropType(
      "Wall", "prop_wall_thin",
      "media assets/scene layout/common/sprite_wall_thin.png",
      1, 0, Kind.BLOCK_FULL, true
    );
  
  
  final public static SceneType FIXED_TEST_SCENE = new SceneTypeFixed(
    "fixed test scene", "type_urban_scene_fixed",
    KIND_FLOOR,
    new PropType[] {
      KIND_WALL      ,
      KIND_DOOR      ,
      KIND_WINDOW    ,
      KIND_POOL_TABLE,
      KIND_BAR_TABLE ,
      KIND_BAR_STOOL ,
      KIND_JUKEBOX   ,
    },
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
    }
    
    /*
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 7, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 6 },
      { 4, 4, 4, 0, 0, 0, 5, 5 },
      { 4, 4, 4, 0, 0, 0, 6, 0 },
      { 0, 0, 0, 0, 0, 0, 6, 0 },
      { 7, 0, 6, 6, 0, 5, 5, 0 },
      { 0, 0, 5, 5, 0, 6, 6, 0 },
      { 0, 0, 6, 6, 0, 0, 0, 0 },
    }
    //*/
  );
  
  
  
  public static void main(String args[]) {
    runGame(new DebugSceneWithLayout(), "saves/debug_fixed_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    //
    //  Generate the scene-
    SceneType sceneType = FIXED_TEST_SCENE;
    Scene mission = sceneType.generateScene(world, 12, true);
    
    Tile.stopCheck = true;
    
    /*
    mission.addProp(KIND_POOL_TABLE, 0, 0, TileConstants.N);
    mission.addProp(KIND_POOL_TABLE, 8, 0, TileConstants.W);
    mission.addProp(KIND_POOL_TABLE, 8, 8, TileConstants.S);
    mission.addProp(KIND_POOL_TABLE, 0, 8, TileConstants.E);
    
    for (int x = 4; x-- > 0;) {
      int dir = TileConstants.T_ADJACENT[x];
      mission.addProp(KIND_THIN_WALL, 1 + (x * 2), 4, dir);
      mission.addProp(KIND_JUKEBOX  , 1 + (x * 2), 3, dir);
    }
    //*/
    
    //*
    for (int x = mission.size(); x-- > 0;) {
      mission.addProp(KIND_THIN_WALL, x, 3, TileConstants.N);
    }
    //*/
    
    Tile.printWallsMask(mission);
    
    GameSettings.debugScene = true;
    //GameSettings.pauseScene = true;
    //
    //  Then introduce the agents themselves-
    final Base base = world.playerBase();
    Series <Person> active = base.roster();
    int across = (mission.size() - (active.size())) / 2;
    for (Person p : active) {
      p.gear.equipItem(Gadgets.TEAR_GAS, PersonGear.SLOT_ITEM_1);
      p.addAssignment(mission);
      mission.enterScene(p, across++, 0);
    }
    
    //
    //  TODO:  Then introduce some random goons?
    
    //
    //  Then enter and return-
    world.enterScene(mission);
    return world;
  }
}










