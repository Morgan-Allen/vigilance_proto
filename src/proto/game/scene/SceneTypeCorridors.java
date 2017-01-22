

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
    
    final Box2D area = new Box2D(2, 2, size - 4, size - 4);
    final SceneGenCorridors gen = new SceneGenCorridors(scene);
    gen.populateAsRoot(this, area);
    gen.printMarkup();
    return scene;
  }
  
  
}
