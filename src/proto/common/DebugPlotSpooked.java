

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
    
    Plot crime = world.events.nextActivePlot();
    Series <Element> involvedBefore = crime.allInvolved();
    boolean allSame = true;
    
    I.say("\n\nCrime generated: "+crime);
    I.say("\nPerps involved at first:");
    for (Element e : involvedBefore) {
      I.say("  "+e+" ("+crime.roleFor(e)+")");
    }
    
    crime.takeSpooking(100);
    world.updateWorld(24);
    
    Series <Element> involvedAfter = crime.allInvolved();
    I.say("\nPerps involved after spooking:");
    
    int index = 0;
    for (Element e : involvedAfter) {
      I.say("  "+e+" ("+crime.roleFor(e)+")");
      Element before = involvedBefore.atIndex(index);
      
      if (e != before) {
        allSame = false;
        I.add(" (=/= "+before+")");
      }
      else {
        I.add(" (same)");
      }
      
      if (++index >= involvedBefore.size()) break;
    }
    
    if (allSame) {
      I.say("\nFailed to update plot correctly!");
    }
    
    return ! allSame;
  }
  
}



