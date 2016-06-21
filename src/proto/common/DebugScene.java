

package proto.common;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;

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
    world.initDefaultNations();
    world.initDefaultBase();
    
    Event played = Kidnapping.TYPE.createRandomEvent(world);
    final FightLead combat = new FightLead(
      "Test event", "test event info", played, 101, played, null
    );
    
    UrbanScene mission = new UrbanScene(world, 32);
    for (Person p : world.base().roster()) {
      mission.addToTeam(p);
    }
    mission.assignMissionParameters(
      combat, world.districtFor(played.region()), 0.5f, 100, null
    );
    mission.setupScene();
    
    world.enterScene(mission);
    
    return world;
  }
  
  
}










