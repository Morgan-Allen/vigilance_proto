

package proto.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;
import proto.util.*;


public class DebugPlotSpooked extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugPlotSpooked(), "saves/debug_plot_spooked");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MORETTI_FAMILY);
    DebugPlotUtils.assignRandomPlot(crooks, PlotTypes.ALL_TYPES);
    
    world.updateWorld(0);
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    if (afterLoad) return false;
    
    Plot plot = world.events.nextActivePlot();
    Series <Element> involvedBefore = plot.allInvolved();
    
    I.say("\n\nCrime generated: "+plot);
    Element spooked = (Element) Rand.pickFrom(involvedBefore);
    plot.takeSpooking(100, spooked);
    
    I.say("\nCrime interrupted by spooking: "+plot.complete());
    
    world.updateWorld(LeadType.CLUE_EXPIRATION_TIME + 24);
    boolean expired = ! world.events.past().includes(plot);
    
    I.say("\nPast events are: "+world.events.past());
    I.say("\nCrime has expired: "+expired);
    
    return expired;
  }
  
}












