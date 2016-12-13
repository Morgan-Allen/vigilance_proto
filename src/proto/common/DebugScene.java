

package proto.common;
import proto.game.world.*;
import proto.util.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.places.*;
import java.awt.EventQueue;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DebugScene ex = new DebugScene();
        ex.setVisible(true);
      }
    });
  }
  
  
  DebugScene() {
    super("saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    GameSettings.freeTipoffs = true;
    
    //  TODO:  Also, you need to avoid different bosses interfering with
    //  eachother (if reasonably possible.)
    
    return world;
  }
}








