

package proto.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;



public class DebugPlots extends RunGame {
  
  
  public static void main(String args[]) {
    GameSettings.freeTipoffs = true;
    runGame(new DebugPlots(), "saves/debug_plots");
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
    
    Base heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Clue tipoff = new Clue(kidnap, Plot.ROLE_ORGANISER);
    int timeFound = world.timing.totalHours();
    Place placeFound = kidnap.hideout();
    tipoff.confirmTipoff(organiser, Lead.LEAD_TIPOFF, timeFound, placeFound);
    heroes.leads.caseFor(organiser).recordClue(tipoff);
    
    world.view().missionView.setActiveFocus(kidnap, true);
    return world;
  }
  
}




