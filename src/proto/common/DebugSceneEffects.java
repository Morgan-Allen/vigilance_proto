

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
    I.say("  Current date: "+world.timing.currentTimeString());
    
    Trial trial = world.events.latestTrial();
    Batch <Person> released = new Batch();
    if (trial == null) {
      I.say("  Trial was never scheduled!");
      return false;
    }
    
    Base base = trial.plot().base();
    Person leader = base.leader();
    int maxSentence = 0, maxTrialTime = world.timing.totalHours();
    maxTrialTime += Council.TRIAL_DELAY_MAX * World.HOURS_PER_DAY;
    trial.addToAccused(leader);
    
    while (! trial.complete()) {
      world.updateWorld(48);
      
      if (world.timing.totalHours() > maxTrialTime) {
        I.say("  Trial did not conclude in allowed time!");
        return false;
      }
    }
    
    I.say("  Trial concluded...");
    for (Person p : trial.accused()) {
      if (! p.isCaptive()) {
        released.add(p);
        I.say("    "+p+" was released.");
      }
      else {
        int sentence = world.council.sentenceDuration(p);
        maxSentence = Nums.max(maxSentence, sentence);
        I.say("    "+p+" is serving a "+sentence+" day sentence.");
      }
    }
    
    if (leader != null) {
      world.council.applySentence(leader, 2);
      int sentence = world.council.sentenceDuration(leader);
      maxSentence = Nums.max(maxSentence, sentence);
      I.say("    "+leader+" is serving a "+sentence+" day sentence.");
    }
    
    int maxCaptiveTime = world.timing.totalHours();
    maxCaptiveTime += maxSentence * World.HOURS_PER_DAY;
    maxCaptiveTime += World.HOURS_PER_DAY * World.DAYS_PER_WEEK;
    
    I.say("  Awaiting release of all prisoners...");
    while (released.size() < trial.accused().size()) {
      for (Person p : trial.accused()) {
        if (released.includes(p)) {
          continue;
        }
        if (! p.isCaptive()) {
          released.add(p);
          I.say("    "+p+" was released on "+world.timing.currentTimeString());
        }
      }
      
      boolean leaderFree = released.includes(leader);
      for (Event e : world.events.coming()) if (e.isPlot()) {
        Plot plot = (Plot) e;
        if (leaderFree || base != plot.base()) continue;
        
        I.say("New plot was generated while mastermind was still captive!");
        return false;
      }
      
      world.updateWorld(48);
      
      if (world.timing.totalHours() > maxCaptiveTime) {
        I.say("  Prisoners were not released in allowed time!");
        return false;
      }
    }
    
    I.say("  Scene effects all correct!");
    return true;
  }
  
}


