

package proto.common;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.Heroes;
import proto.content.events.*;
import proto.content.scenes.*;
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
    super("debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultNations(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    /*
    Event played = Kidnapping.TYPE.createRandomEvent(world);
    final FightLead combat = new FightLead(
      "Test event", "test event info", played, 101, played, null
    );
    
    final Base base = world.playerBase();
    UrbanScene mission = new UrbanScene(world, 32);
    mission.addToTeam(base.firstOfKind(Heroes.HERO_BATMAN   ));
    mission.addToTeam(base.firstOfKind(Heroes.HERO_NIGHTWING));
    mission.addToTeam(base.firstOfKind(Heroes.HERO_BATGIRL  ));
    
    mission.assignMissionParameters(
      combat, played.place(), 0.5f, 100, null
    );
    mission.setupScene();
    
    world.enterScene(mission);
    //*/
    
    return world;
  }
  
  
}










