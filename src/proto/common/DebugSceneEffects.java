

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
    DefaultGame.initDefaultWorld(world, false);
    
    Base crooks = world.baseFor(Crooks.THE_MORETTI_FAMILY);
    Plot kidnap = PlotTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap, 0);
    Element target = kidnap.target();
    
    Base  heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Lead  guard  = heroes.leads.leadFor(target, LeadType.SURVEIL);
    Scene scene  = kidnap.generateScene(target, guard);
    
    world.enterScene(scene);
    scene.onSceneCompletion(Scene.STATE_WON);
    
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    I.say("\n\n\nEvaluating after-effects of combat scene...");
    I.say("  Current date: "+world.timing.currentTimeString());
    
    Trial trial = world.events.nextTrial();
    boolean jailLeader = true;
    
    if (trial == null) {
      I.say("  Trial was never scheduled!");
      return false;
    }
    
    List <Person> released = new List();
    Base base = trial.plot().base();
    Person leader = base.leader();
    
    if (leader == null) {
      I.say("  Plot has no mastermind/base-leader!");
      return false;
    }
    
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
    I.say("  Defends:    "+trial.defends   ());
    I.say("  Prosecutes: "+trial.prosecutes());
    
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
    
    if (jailLeader) {
      world.council.applySentence(leader, 2);
      int sentence = world.council.sentenceDuration(leader);
      maxSentence = Nums.max(maxSentence, sentence);
      released.remove(leader);
      I.say("  Leader "+leader+" is serving a "+sentence+" day sentence.");
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
      Plot coming = comingPlot(base);
      if (jailLeader && coming != null && ! leaderFree) {
        I.say("New plot was generated while mastermind was captive: "+coming);
        return false;
      }
      if (coming != null && ! jailLeader) {
        I.say("New plot was generated: "+coming);
        break;
      }
      
      world.updateWorld(48);
      
      if (world.timing.totalHours() > maxCaptiveTime) {
        I.say("  Prisoners were not released in allowed time!");
        return false;
      }
    }
    
    boolean newPlot = false;
    if (jailLeader) while (! newPlot) {
      Plot coming = comingPlot(base);
      if (coming != null) {
        I.say("  New plot was generated after mastermind's release: "+coming);
        newPlot = true;
        break;
      }
      world.updateWorld(48);
    }
    
    I.say("  Scene effects all correct!");
    return true;
  }
  
  
  private static Plot comingPlot(Base base) {
    for (Event e : base.world().events.coming()) if (e.isPlot()) {
      Plot plot = (Plot) e;
      if (plot.base() == base) return plot;
    }
    return null;
  }
  
}




