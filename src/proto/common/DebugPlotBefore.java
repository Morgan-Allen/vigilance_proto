

package proto.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;
import proto.util.*;



public class DebugPlotBefore extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugPlotBefore(), "saves/debug_plot_before");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = PlotTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    crooks.plots.assignRootPlot(kidnap, 0);
    Person organiser = kidnap.organiser();
    Place  hideout   = kidnap.hideout  ();
    
    Base played = world.playerBase();
    Clue tipoff = Clue.confirmSuspect(
      kidnap, Plot.ROLE_ORGANISER, organiser, hideout
    );
    int timeFound = world.timing.totalHours();
    Place placeFound = kidnap.hideout();
    CaseFile file = played.leads.caseFor(kidnap);
    file.recordClue(tipoff, Lead.LEAD_TIPOFF, timeFound, placeFound);

    GameSettings.noTipoffs = true;
    world.updateWorld(0);
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    if (afterLoad) return false;
    
    GameSettings.noTipoffs = true;
    Plot plot = world.events.latestPlot();
    if (plot != null) return DebugPlotUtils.enterPlotDebugLoop(
      world, plot, ! suite, suite, plot.organiser(), plot.target()
    );
    return false;
  }
  
}








