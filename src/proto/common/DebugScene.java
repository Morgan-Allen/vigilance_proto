

package proto.common;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.items.*;
import proto.content.places.*;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    //GameSettings.freeTipoffs = true;
    GameSettings.debugScene = true;
    runGame(new DebugScene(), "saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = CrimeTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap);
    
    Base heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Lead guarding = heroes.leads.leadFor(
      kidnap.target(), Lead.LEAD_SURVEIL_PERSON
    );
    for (Person p : heroes.roster()) {
      p.addAssignment(guarding);
    }
    Step heist = kidnap.stepWithLabel("heist");
    kidnap.advanceToStep(heist);
    
    Scene scene = kidnap.generateScene(heist, kidnap.target(), guarding);
    world.enterScene(scene);
    
    return world;
  }
}







