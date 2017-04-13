

package proto.common;
import proto.game.world.*;
import proto.content.items.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.content.agents.*;
import static proto.content.places.UrbanScenes.*;



public class DebugSceneGrid extends RunGame {
  
  
  final public static SceneType GRID_TEST_SCENE = new SceneTypeGrid(
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
    GameSettings.debugScene      = true ;
    GameSettings.reportWorldInit = false;
    runGame(new DebugSceneGrid(), "saves/debug_scene_grid");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
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






