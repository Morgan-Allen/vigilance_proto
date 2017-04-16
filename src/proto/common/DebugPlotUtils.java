

package proto.common;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;



public class DebugPlotUtils {
  
  
  public static void enterPlotDebugLoop(
    World world, Plot testPlot, boolean askToLoop, Element... bestTrail
  ) {
    Base played = world.playerBase();
    Lead bust = null;
    
    //  We update the world to allow the plot to be scheduled and updated...
    world.updateWorld(6);
    
    while (! testPlot.complete()) {
      int  time    = world.timing.totalHours();
      Step doing   = testPlot.currentStep();
      int  tenseDo = doing == null ? Lead.TENSE_NONE : doing.tense();
      
      I.say("\n\n\nUpdating Investigation into "+testPlot);
      I.say("  Current time: "+time);
      I.say("  Ideal trail: "+I.list(bestTrail));
      I.say("\nCurrent step: "+doing);
      I.add(" ["+Lead.TENSE_DESC[Nums.clamp(tenseDo, 3)]+"]");
      
      List <Lead> bestLeads = new List <Lead> () {
        protected float queuePriority(Lead r) {
          return 0 - r.leadRating;
        }
      };
      for (Element suspect : testPlot.allInvolved()) {
        for (Lead lead : played.leads.leadsFor(suspect)) {
          lead.leadRating = 0;
        }
      }
      for (Element suspect : testPlot.allInvolved()) {
        float evidence = played.leads.evidenceAgainst(suspect, testPlot, false);
        if (evidence <= 0) continue;
        
        int       trailIndex = 1 + Visit.indexOf(suspect, bestTrail);
        boolean   shouldBust = suspect == Visit.last(bestTrail);
        Plot.Role role       = testPlot.roleFor(suspect);
        Place     lastKnown  = played.leads.lastKnownLocation(suspect);
        Place     location   = suspect.place();
        
        I.say("\n  "+suspect+" ("+role+")");
        I.say("  Last Seen: "+lastKnown+"  At: "+location);
        I.say("  Should Bust? "+shouldBust);
        I.say("  Evidence Rating: "+evidence+"  Trail ID: "+trailIndex);
        for (Clue clue : played.leads.cluesFor(testPlot, role, true)) {
          I.say("    "+clue.traitDescription()+" ("+clue.leadType()+")");
        }
        
        for (Lead lead : played.leads.leadsFor(suspect)) {
          if (lead.type.medium == Lead.MEDIUM_ASSAULT) {
            if (shouldBust) bust = lead;
            else continue;
          }
          float numDetects = 0, numSteps = testPlot.allSteps().size();
          for (Step step : testPlot.allSteps()) {
            if (lead.canDetect(step, step.tense(), testPlot, time)) {
              if (step.tense() == Lead.TENSE_DURING) numDetects += numSteps;
              else numDetects++;
            }
          }
          if ((numDetects /= numSteps) > 0) {
            float rating =  1 - Nums.min(1, Nums.abs(1 - evidence));
            rating       *= Nums.max(0.5f, trailIndex) * numDetects;
            lead.leadRating = Nums.max(rating, lead.leadRating);
            lead.autoWin    = true;
            bestLeads.include(lead);
          }
        }
      }
      
      bestLeads.queueSort();
      I.say("\n  Best Leads:");
      if (bestLeads.empty()) {
        I.say("    None");
      }
      else for (Lead lead : bestLeads) {
        I.say("    "+lead.activeInfo()+" ("+lead.leadRating+")");
      }
      
      List <Person> agents = new List();
      Visit.appendTo(agents, played.roster());
      
      I.say("\n  Performing assignments:");
      
      if (bust != null) for (Person agent : agents) {
        agent.addAssignment(bust);
        I.say("    "+agent+" -> "+bust.activeInfo());
      }
      else while (! (agents.empty() || bestLeads.empty())) {
        Lead lead = bestLeads.removeFirst();
        Person agent = agents.removeFirst();
        agent.addAssignment(lead);
        I.say("    "+agent+" -> "+lead.activeInfo());
      }
      
      if (askToLoop) {
        while (true) {
          I.say("\n  Proceed? (y/Yes, n/No, s/Save, f/Finish Loop)");
          I.say("    ");
          String response = I.listen().toUpperCase().substring(0, 1);
          if      (response.equals("N")) System.exit(0);
          else if (response.equals("S")) world.performSave();
          if      (response.equals("F")) { askToLoop = false; break; }
          else if (response.equals("Y")) break;
          else continue;
        }
      }
      
      if (world.activeScene() != null) {
        bust = (Lead) world.activeScene().playerTask();
        break;
      }
      else {
        world.updateWorld(12);
      }
    }
    
    if (bust != null) {
      I.say("\n\nINVESTIGATION SUCCEEDED: "+bust);
    }
    else {
      I.say("\n\nINVESTIGATION FAILED: "+testPlot.complete());
    }
  }
}



