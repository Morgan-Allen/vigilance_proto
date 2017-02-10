

package proto.common;
import proto.game.world.*;
import proto.content.items.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.content.places.UrbanScenes.*;



public class DebugSceneWithLayout extends RunGame {
  
  
  final public static SceneType FIXED_TEST_SCENE = new SceneTypeFixed(
    "fixed test scene", "type_urban_scene_fixed",
    KIND_FLOOR,
    new PropType[] {
      KIND_THICK_WALL,
      KIND_DOOR      ,
      KIND_WINDOW    ,
      KIND_POOL_TABLE,
      KIND_BAR_TABLE ,
      KIND_BAR_STOOL ,
      KIND_JUKEBOX   ,
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
    final Scene mission = new Scene(world, 12);
    mission.setupScene(true);
    sceneType.applyToScene(mission, 2, 2, TileConstants.N, 8, true);
    
    for (int y = mission.size() - 1; y-- > 1;) {
      PropType kind = y == 6 ? KIND_DOOR : KIND_WALL;
      mission.addProp(kind, 4, y, TileConstants.E);
    }
    
    /*
    mission.addProp(KIND_POOL_TABLE, 0, 0, TileConstants.N);
    mission.addProp(KIND_POOL_TABLE, 8, 0, TileConstants.E);
    mission.addProp(KIND_POOL_TABLE, 8, 8, TileConstants.S);
    mission.addProp(KIND_POOL_TABLE, 0, 8, TileConstants.W);
    
    for (int x = 4; x-- > 0;) {
      int dir = TileConstants.T_ADJACENT[x];
      mission.addProp(KIND_THIN_WALL, 1 + (x * 2), 4, dir);
      mission.addProp(KIND_JUKEBOX  , 1 + (x * 2), 3, dir);
    }
    //*/
    Tile.printWallsMask(mission);
    
    GameSettings.debugScene = true;
    GameSettings.viewSceneBlocks = true;
    //GameSettings.debugLineSight = true;
    //GameSettings.pauseScene = true;
    //
    //  Then introduce the agents themselves-
    final Base base = world.playerBase();
    Series <Person> active = base.roster();
    int across = (mission.size() - (active.size())) / 2;
    for (Person p : active) {
      p.gear.equipItem(Gadgets.TEAR_GAS, PersonGear.SLOT_ITEM_1);
      p.addAssignment(mission);
      mission.enterScene(p, 0, across++);
      break;
    }
    
    //
    //  Then enter and return-
    world.enterScene(mission);
    return world;
  }
}










