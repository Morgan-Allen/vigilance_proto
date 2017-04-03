

package proto.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;
import proto.util.*;



public class DebugSceneEffects extends RunGame {
  
  
  public static void main(String args[]) {
    GameSettings.freeTipoffs = true;
    runGame(new DebugSceneEffects(), "saves/debug_scene_effects");
  }
  
  
  protected World setupWorld() {
    
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = CrimeTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap);
    Element target = kidnap.target();
    Step heist = kidnap.stepWithLabel("heist");
    
    Base heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Lead guard = heroes.leads.leadFor(target, Lead.LEAD_SURVEIL_PERSON);
    Scene scene = kidnap.generateScene(heist, target, guard);
    
    world.enterScene(scene);
    scene.onSceneCompletion(Scene.STATE_WON);
    
    Council.Trial trial = world.council.nextTrialFor(kidnap);
    world.council.scheduleTrial(trial, 1, 1);
    
    return world;
  }
  
}




