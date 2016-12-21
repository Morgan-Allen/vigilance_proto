

package proto.common;
import proto.game.world.*;
import proto.game.scene.*;
import proto.content.places.*;
import proto.util.*;

import java.awt.EventQueue;



public class DebugSceneGen {
  
  
  public static void main(String args[]) {
    World world = new World();
    
    Place park = new Place(Facilities.BUSINESS_PARK, 0, world);
    SceneType root = park.kind().sceneType();
    Scene scene = new Scene(park, 32);
    
    SceneGen gen = new SceneGen(scene);
    gen.verbose = true;
    gen.subdivideAreas(root, new Box2D(2, 2, 24, 24));
    
    I.say("FINAL STATE...");
    gen.printMarkup();
  }
}










