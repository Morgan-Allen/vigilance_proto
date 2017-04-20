

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
    Place park = new Place(Facilities.BUSINESS_PARK, 0, world);
    SceneType root = park.kind().sceneType();
    root.generateScene(world, 32, 32, true);
  }
}
