

package proto.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;



public class DebugPlotAfter extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugPlotAfter(), "saves/debug_plot_after");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MORETTI_FAMILY);
    DebugPlotUtils.assignRandomPlot(crooks, PlotTypes.ALL_TYPES);
    
    GameSettings.noTipoffs = true;
    world.updateWorld(0);
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    if (afterLoad) return false;
    
    GameSettings.noTipoffs = true;
    Plot plot = world.events.nextActivePlot();
    if (plot != null) return DebugPlotUtils.enterPlotDebugLoop(
      world, plot, ! suite, suite, plot.scene(), plot.organiser()
    );
    return false;
  }
}







