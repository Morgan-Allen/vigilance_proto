

package proto.common;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;



public class DebugCampaign extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugCampaign(), "saves/debug_campaign");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world, false);
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    
    I.say("\n\n\nBegan testing plot generation...");
    Stack <Plot> currentPlots = new Stack();
    int minTrust = 0, minDeter = 0, maxYears = 2 + world.timing.timeYears();
    
    while (true) {
      int time = world.timing.totalHours(), years = world.timing.timeYears();
      
      for (Event e : world.events.active()) if (e.isPlot()) {
        Plot p = (Plot) e;
        if (currentPlots.includes(p)) continue;
        currentPlots.add(p);
        I.say("  Plot began: "+p   );
        I.say("  Time:       "+time);
      }
      
      for (Plot p : currentPlots) if (! world.events.active().includes(p)) {
        currentPlots.remove(p);
        I.say("  Plot ended: "+p   );
        I.say("  Time:       "+time);
        
        //  TODO:  Report on the effects of the plot!
      }
      
      if (currentPlots.empty()) {
        world.updateWorld(48);
      }
      else {
        world.updateWorld(6);
      }
      
      if (years > maxYears) {
        I.say("\nHuh.  The system works.");
        return false;
      }
      
      if (world.timing.monthIsUp()) {
        boolean allCrap = true;
        
        I.say("\nMonth complete: "+world.timing.currentTimeString());
        for (Region r : world.regions()) {
          float trust = r.currentValue(Region.TRUST     );
          float deter = r.currentValue(Region.DETERRENCE);
          I.say("  Stats for "+r);
          I.say("    Trust: "+trust);
          I.say("    Deter: "+deter);
          if (trust > minTrust || deter > minDeter) allCrap = false;
        }
        
        if (allCrap) {
          I.say("\nYou maniacs!  You blew it up!");
          return true;
        }
      }
    }
  }
  
}

