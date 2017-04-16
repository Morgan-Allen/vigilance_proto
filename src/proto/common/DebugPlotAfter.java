

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
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = CrimeTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap);
    Step heist = kidnap.stepWithLabel("grab target");
    kidnap.advanceToStep(heist);

    GameSettings.noTipoffs = true;
    world.updateWorld(24);
    world.updateWorld(0);
    return world;
  }
  
  
  protected void runWorldTests(World world, boolean afterLoad) {
    GameSettings.noTipoffs = true;
    Plot plot = world.events.latestPlot();
    if (plot != null) DebugPlotUtils.enterPlotDebugLoop(
      world, plot, true, true, plot.scene(), plot.organiser()
    );
  }
}







