

package proto.common;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;
import proto.util.*;



public class DebugPlotBefore extends RunGame {
  
  
  public static void main(String args[]) {
    GameSettings.freeTipoffs = true;
    runGame(new DebugPlotBefore(), "saves/debug_plot_before");
  }
  
  
  
  Plot testPlot;
  Element bestTrail[];
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot kidnap = CrimeTypes.TYPE_KIDNAP.initPlot(crooks);
    kidnap.fillAndExpand();
    kidnap.printRoles();
    crooks.plots.assignRootPlot(kidnap);
    Person organiser = kidnap.organiser();
    
    Base played = world.playerBase();
    Clue tipoff = Clue.confirmSuspect(kidnap, Plot.ROLE_ORGANISER, organiser);
    int timeFound = world.timing.totalHours();
    Place placeFound = kidnap.hideout();
    CaseFile file = played.leads.caseFor(kidnap);
    file.recordClue(tipoff, Lead.LEAD_TIPOFF, timeFound, placeFound);
    
    this.testPlot = kidnap;
    this.bestTrail = new Element[] {
      organiser,
      kidnap.target()
    };
    
    world.view().missionView.setActiveFocus(kidnap, true);
    return world;
  }
  
  
  protected void runWorldTests(World world) {
    Base played = world.playerBase();
    boolean askToLoop = true;
    
    while (true && ! testPlot.complete()) {
      int  time  = world.timing.totalHours();
      Step step  = testPlot.currentStep();
      int  tense = step == null ? Lead.TENSE_NONE : step.tense();
      Lead bust  = null;
      
      I.say("\nUpdating Investigation into "+testPlot);
      
      List <Lead> bestLeads = new List <Lead> () {
        protected float queuePriority(Lead r) {
          return r.leadRating;
        }
      };
      for (Element suspect : testPlot.allInvolved()) {
        float     evidence   = played.leads.evidenceAgainst(suspect, testPlot);
        int       trailIndex = Visit.indexOf(suspect, bestTrail);
        boolean   canBust    = suspect == Visit.last(bestTrail);
        Plot.Role role       = testPlot.roleFor(suspect);
        Place     lastKnown  = played.leads.lastKnownLocation(suspect);
        Place     location   = suspect.place();
        if (evidence <= 0) continue;
        
        I.say("\n  Have clues on "+suspect+" ("+role+")");
        I.say("  Known Location: "+lastKnown+"  Actual: "+location);
        I.say("  Can Bust? "+canBust);
        I.say("  Evidence rating: "+evidence+"  Trail ID: "+trailIndex);
        for (Clue clue : played.leads.cluesFor(suspect, true)) {
          I.say("    "+clue.traitDescription()+" ("+clue.leadType()+")");
        }
        I.say("  Leads:");
        
        Series <Lead> leads = played.leads.leadsFor(suspect);
        for (Lead lead : leads) {
          if (canBust && lead.type == Lead.LEAD_BUST) {
            bust = lead;
          }
          if (lead.canDetect(step, tense, testPlot, time)) {
            lead.leadRating = evidence * Nums.max(0.5f, trailIndex + 1);
            lead.autoWin    = true;
            bestLeads.queueAdd(lead);
          }
        }
        
        if (leads.empty()) {
          I.say("    None.");
        }
        else for (Lead lead : leads) {
          I.say("    "+lead.activeInfo());
        }
      }
      
      List <Person> agents = new List();
      Visit.appendTo(agents, played.roster());
      
      I.say("\nPerforming assignments:");
      
      if (bust != null) for (Person agent : agents) {
        agent.addAssignment(bust);
        
        I.say("  "+agent+" -> "+bust.activeInfo());
      }
      else while (! (agents.empty() || bestLeads.empty())) {
        Lead lead = bestLeads.removeFirst();
        Person agent = agents.removeFirst();
        agent.addAssignment(lead);
        
        I.say("  "+agent+" -> "+lead.activeInfo());
      }
      
      if (askToLoop) {
        I.say("\n  Proceed? (y/Yes, n/No, f/Finish Loop)\n    ");
        String response = I.listen().toUpperCase().substring(0, 1);
        if (response.equals("N")) System.exit(0);
        if (response.equals("F")) askToLoop = false;
        else if (! response.equals("Y")) { I.say("?"); break; }
      }
      
      world.updateWorld(12);
    }
  }
  
}


