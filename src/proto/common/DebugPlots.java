

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
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    //  TODO:  Generate relationships (kin and otherwise) between persons in
    //  nearby venues!
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = CrimeTypes.TYPE_KIDNAP.initPlot(crooks);
    
    //  TODO:  Substitute specific 'initAsGoal' and 'initAsSupplies' methods
    //  instead.  Have separate passes for specifying/choosing targets and
    //  settling on filling needs later.
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap);
    Person organiser = kidnap.organiser();
    
    //  TODO:  You will need to generate tipoffs automatically again...
    //
    Base heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Clue tipoff = new Clue(kidnap, Plot.ROLE_ORGANISER);
    int timeFound = world.timing.totalHours();
    tipoff.confirmMatch(organiser, Lead.LEAD_QUESTION, timeFound);
    heroes.leads.caseFor(organiser).recordClue(tipoff);
    
    
    /*
    I.say("\nFollowing Lead...");
    Lead lead = null;
    world.beginMonitoring();
    crooks.plans.assignRootPlot(kidnap);
    
    lead = new Lead(heroes, Lead.LEAD_SURVEIL_PERSON, kidnap, crooks.leader());
    sleuth.addAssignment(lead);
    
    for (int hours = 8, days = 5, n = days * 24 / hours; n-- > 0;) {
      world.timing.advanceTime(hours);
      world.updateWorld();
      lead.updateAssignment();
    }
    
    for (Plot.RoleEntry entry : kidnap.entries) {
      CaseFile file = heroes.leads.caseFor(entry.element);
      
      Series <Clue> clues = file.clues;
      if (clues.empty()) {
        I.say("\nNo clues obtained on "+entry);
        continue;
      }
      
      I.say("\nObtained clues on "+entry);
      for (Clue clue : clues) {
        I.say("  "+clue);
      }
      
      Series <Element> suspects = file.matchingSuspects(crooks.roster());
      if (suspects.empty()) {
        I.say("  No matching suspects.");
        continue;
      }
      
      I.say("  Possible suspects: ");
      for (Element s : suspects) {
        I.say("    "+s);
      }
    }
    //*/
    
    return world;
  }
  
}



