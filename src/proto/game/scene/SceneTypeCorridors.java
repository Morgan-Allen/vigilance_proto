

package proto.game.scene;
import proto.game.world.*;
import proto.util.*;



public class SceneTypeCorridors extends SceneType {
  
  
  
  public SceneTypeCorridors(
    String name, String ID, Object... args
  ) {
    super(name, ID, args);
  }
  

  public Scene generateScene(World world, int size, boolean forTesting) {
    final Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    applyToScene(scene, 2, 2, N, size - 4);
    return scene;
  }
  
  
  public void applyToScene(
    Scene scene, int offX, int offY, int facing, int limit
  ) {
    final SceneGenCorridors gen = new SceneGenCorridors(scene);
    final Box2D area = new Box2D(offX, offY, limit, limit);
    gen.populateAsRoot(this, area);
    gen.printMarkup();
  }
  
  
  
  
  
  
}



