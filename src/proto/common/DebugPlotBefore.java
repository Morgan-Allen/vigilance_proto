

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
    
    Base crooks = world.baseFor(Crooks.THE_MORETTI_FAMILY);
    Plot plot = DebugPlotUtils.assignRandomPlot(crooks, PlotTypes.ALL_TYPES);
    plot.fillAndExpand();
    crooks.plots.assignRootPlot(plot, 0);
    Person organiser = plot.organiser();
    Place  hideout   = plot.hideout  ();
    
    Base played = world.playerBase();
    Clue tipoff = Clue.confirmSuspect(
      plot, Plot.ROLE_ORGANISER, plot.allSteps().first(), organiser, hideout
    );
    int timeFound = world.timing.totalHours();
    Place placeFound = plot.hideout();
    CaseFile file = played.leads.caseFor(plot);
    file.recordClue(tipoff, LeadType.TIPOFF, timeFound, placeFound);
    
    GameSettings.noTipoffs = true;
    world.updateWorld(0);
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    if (afterLoad) return false;
    
    GameSettings.noTipoffs = true;
    Plot plot = world.events.nextActivePlot();
    if (plot != null) return DebugPlotUtils.enterPlotDebugLoop(
      world, plot, ! suite, suite, plot.organiser(), plot.target()
    );
    return false;
  }
  
}








