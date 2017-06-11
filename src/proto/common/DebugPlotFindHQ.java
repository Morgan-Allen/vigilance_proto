

package proto.common;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.util.*;
import proto.content.agents.*;
import proto.content.events.*;



public class DebugPlotFindHQ extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugPlotFindHQ(), "saves/debug_plot_find_hq");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world, false);
    
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    
    Base played = world.playerBase();
    Base crooks = world.baseFor(Crooks.THE_MORETTI_FAMILY);
    int maxPlotsTried = 10, plotsTried = 0;
    
    Place oldHQ = crooks.HQ();
    boolean singledHQ = false, lostClues = false, movedHQ = false;
    Plot instigated = null;
    
    while (true) {
      instigated = DebugPlotUtils.assignRandomPlot(crooks, PlotTypes.ALL_TYPES);
      DebugPlotUtils.enterLeadFollowingLoop(
        world, instigated, false,
        false, 0,
        instigated.hideout(), instigated.scene()
      );
      crooks.plots.assignRootPlot(null, 0);
      
      Series <Element> suspects = played.leads.suspectsFor(
        Plot.ROLE_HQ, instigated
      );
      DebugPlotUtils.printSuspects(suspects, Plot.ROLE_HQ);
      
      Series <Clue> allClues = played.leads.cluesFor(
        null, null, Plot.ROLE_HQ, null, true
      );
      I.say("\nClues assembled on HQ are:");
      for (Clue c : allClues) I.say("  "+c);
      
      if (suspects.size() == 1) {
        I.say("\nSuccessfully identified headquarters!");
        singledHQ = true;
        break;
      }
      if (++plotsTried > maxPlotsTried) {
        I.say("\nToo many attempts!");
        break;
      }
    }
    
    //
    //  After identifying the HQ, conduct a bust and make sure that any earlier
    //  clues on the HQ are disregarded:
    I.say("\nConducting bust to put away leader:");
    
    Lead bust = played.leads.leadFor(oldHQ, LeadType.BUST);
    Scene scene = PlotUtils.generateHideoutScene(
      instigated, oldHQ, bust
    );
    scene.onSceneCompletion(Scene.STATE_WON);
    
    boolean markedBust = false;
    Series <Clue> clues = played.leads.cluesFor(instigated, Plot.ROLE_HQ, true);
    for (Clue c : clues) {
      if (c.match() != oldHQ || ! c.isConfirmation()) continue;
      if (c.leadType() != LeadType.MAJOR_BUST) continue;
      markedBust = true;
      break;
    }
    if (! scene.didEnter().includes(crooks.leader())) {
      I.say("Leader was not present on scene...");
    }
    if (! markedBust) {
      I.say("Did not record bust on HQ...");
    }
    
    world.updateWorld(24);
    world.council.releasePrisoner(crooks.leader());
    world.updateWorld(24);
    
    instigated = DebugPlotUtils.assignRandomPlot(crooks, PlotTypes.ALL_TYPES);
    Series <Element> suspects = played.leads.suspectsFor(
      Plot.ROLE_HQ, instigated
    );
    DebugPlotUtils.printSuspects(suspects, Plot.ROLE_HQ);
    
    if (suspects.size() < world.inside().size()) {
      I.say("Incorrectly retained old clues on HQ!");
    }
    else {
      I.say("Correctly disregarded old clues on HQ.");
      lostClues = true;
    }
    if (crooks.HQ() == oldHQ) {
      I.say("Incorrectly retained old HQ!");
    }
    else {
      I.say("Correctly moved to new HQ.");
      movedHQ = true;
    }
    
    return singledHQ && lostClues && movedHQ;
  }
  
}



