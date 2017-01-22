

package proto.common;
import proto.game.world.*;
import proto.game.scene.*;
import proto.content.places.*;
import proto.util.*;
import proto.view.*;
import proto.view.scene.*;

import java.awt.EventQueue;



public class DebugSceneGen {
  
  
  public static void main(String args[]) {
    World world = new World();
    
    final int size = 32;
    
    Place park = new Place(Facilities.BUSINESS_PARK, 0, world);
    SceneType root = park.kind().sceneType();
    Scene scene = new Scene(world, size);
    scene.setupScene(true);
    
    SceneGenCorridors gen = new SceneGenCorridors(scene);
    gen.verbose = true;
    gen.populateAsRoot(root, new Box2D(2, 2, size - 4, size - 4));
    gen.printMarkupVisually();
  }
}










