

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
    GameSettings.freeTipoffs     = true ;
    GameSettings.reportWorldInit = false;
    runGame(new DebugSceneEffects(), "saves/debug_scene_effects");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = PlotTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap, 0);
    Element target = kidnap.target();
    Step heist = kidnap.mainHeist();
    
    Base  heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Lead  guard  = heroes.leads.leadFor(target, Lead.LEAD_SURVEIL_PERSON);
    Scene scene  = kidnap.generateScene(heist, target, guard);
    
    world.enterScene(scene);
    scene.onSceneCompletion(Scene.STATE_WON);
    
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    I.say("\n\n\nEvaluating after-effects of combat scene...");
    
    Trial trial = world.events.latestTrial();
    if (trial == null) {
      I.say("  Trial was never scheduled!");
      return false;
    }
    
    while (! trial.complete()) {
      world.updateWorld(48);
    }
    
    I.say("  Trial concluded...");
    for (Person p : trial.accused()) {
      if (! p.isCaptive()) {
        I.say("  "+p+" was released.");
      }
      else {
        int sentence = world.council.sentenceDuration(p);
        I.say("  "+p+" is serving a "+sentence+" day sentence.");
      }
    }
    
    return true;
  }
  
}















