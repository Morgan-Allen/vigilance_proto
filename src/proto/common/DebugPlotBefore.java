

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
    Plot kidnap = CrimeTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap);
    Person organiser = kidnap.organiser();
    Place  hideout   = kidnap.hideout();
    
    Base played = world.playerBase();
    Clue tipoff = Clue.confirmSuspect(
      kidnap, Plot.ROLE_ORGANISER, organiser, hideout
    );
    int timeFound = world.timing.totalHours();
    Place placeFound = kidnap.hideout();
    CaseFile file = played.leads.caseFor(kidnap);
    file.recordClue(tipoff, Lead.LEAD_TIPOFF, timeFound, placeFound);

    GameSettings.noTipoffs = true;
    DebugPlotUtils.enterPlotDebugLoop(
      world, kidnap, true,
      organiser, kidnap.target()
    );
    
    return world;
  }
  
}






