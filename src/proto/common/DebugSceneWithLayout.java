

package proto.common;
import proto.game.world.*;
import proto.content.items.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.content.places.UrbanScenes.*;

import java.awt.EventQueue;



public class DebugSceneWithLayout extends RunGame {
  
  
  public static void main(String args[]) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DebugSceneWithLayout ex = new DebugSceneWithLayout();
        ex.setVisible(true);
      }
    });
  }
  
  final public static SceneType FIXED_TEST_SCENE = new SceneTypeFixed(
    "fixed test scene", "type_urban_scene_fixed",
    new Kind[] {
      KIND_FLOOR     ,
      KIND_WALL      ,
      KIND_DOOR      ,
      KIND_WINDOW    ,
      KIND_POOL_TABLE,
      KIND_BAR_TABLE ,
      KIND_BAR_STOOL ,
      KIND_JUKEBOX   ,
    },
    new byte[][] {
      { 0, 0, 0, 0, 0, 0, 7, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 6 },
      { 0, 0, 4, 4, 4, 0, 5, 5 },
      { 0, 0, 4, 4, 4, 0, 6, 0 },
      { 0, 0, 0, 0, 0, 0, 6, 0 },
      { 7, 0, 6, 6, 0, 5, 5, 0 },
      { 0, 0, 5, 5, 0, 6, 6, 0 },
      { 0, 0, 6, 6, 0, 0, 0, 0 },
    }
  );
  
  
  
  DebugSceneWithLayout() {
    super("saves/debug_fixed_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    //
    //  Generate the scene-
    SceneType sceneType = FIXED_TEST_SCENE;
    Scene mission = sceneType.generateScene(world, 32, true);
    //
    //  Then introduce the agents themselves-
    final Base base = world.playerBase();
    Series <Person> active = base.roster();
    int across = (mission.size() - (active.size())) / 2;
    for (Person p : active) {
      Item tearGas = new Item(Gadgets.TEAR_GAS, world);
      p.gear.equipItem(tearGas, PersonGear.SLOT_ITEM_1);
      p.addAssignment(mission);
      mission.enterScene(p, across++, 0);
    }
    //
    //  Then enter and return-
    world.enterScene(mission);
    return world;
  }
}




