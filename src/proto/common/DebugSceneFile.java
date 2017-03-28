
package proto.common;
import proto.game.scene.*;
import proto.game.world.World;



public class DebugSceneFile extends RunGame {
  
  
  final static SceneType FILE_TEST_SCENE = SceneFromXML.fixedSceneFrom(
    "media assets/scene layout/civic scenes/", "test_scene.xml"
  );
  
  
  public static void main(String args[]) {
    GameSettings.debugScene = true;
    GameSettings.pauseScene = true;
    runGame(new DebugSceneFile(), "saves/debug_scene_file");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    
    Scene scene = FILE_TEST_SCENE.generateScene(world);
    world.enterScene(scene);
    scene.view().setZoomPoint(scene.tileAt(2, 2));
    
    return world;
  }

}