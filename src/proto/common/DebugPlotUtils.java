

package proto.common;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;



public class DebugPlotUtils {
  
  
  public static void enterPlotDebugLoop(
    World world, Plot testPlot,
    boolean askToLoop, boolean concise,
    Element... bestTrail
  ) {
    Base played = world.playerBase();
    Scene bust = null;
    boolean verbose = ! concise;
    
    loop: while (! testPlot.complete()) {
      int  time    = world.timing.totalHours();
      Step doing   = testPlot.currentStep();
      int  tenseDo = doing == null ? Lead.TENSE_NONE : doing.tense();
      
      if (concise || verbose) {
        I.say("\n\n\nClues acquired:");
        for (Clue clue : played.leads.extractNewClues()) {
          I.say("  "+clue.role()+" "+clue+" ("+clue.leadType()+")");
        }
      }
      
      if (concise) {
        I.say("\n\n\nUpdating Investigation into "+testPlot);
        I.say("  Current time: "+time);
      }
      if (verbose) {
        I.say("\n\n\nUpdating Investigation into "+testPlot);
        I.say("  Current time: "+time);
        I.say("  Ideal trail: "+I.list(bestTrail));
        I.say("\nCurrent step: "+doing);
        I.add(" ["+Lead.TENSE_DESC[Nums.clamp(tenseDo, 3)]+"]");
      }
      
      List <Lead> bestLeads = new List <Lead> () {
        protected float queuePriority(Lead r) {
          return 0 - r.leadRating;
        }
      };
      Lead bustLead = null;
      
      for (Element suspect : testPlot.allInvolved()) {
        for (Lead lead : played.leads.leadsFor(suspect)) {
          lead.leadRating = 0;
          lead.noScene = true;
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
        
        if (verbose) {
          I.say("\n  "+suspect+" ("+role+")");
          I.say("  Last Seen: "+lastKnown+"  At: "+location);
          I.say("  Should Bust? "+shouldBust);
          I.say("  Evidence Rating: "+evidence+"  Trail ID: "+trailIndex);
          for (Clue clue : played.leads.cluesFor(testPlot, role, true)) {
            I.say("    "+clue.traitDescription()+" ("+clue.leadType()+")");
          }
        }
        
        for (Lead lead : played.leads.leadsFor(suspect)) {
          if (lead.type.medium == Lead.MEDIUM_ASSAULT) {
            if (shouldBust) { bustLead = lead; lead.noScene = false; }
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
      
      if (verbose) {
        bestLeads.queueSort();
        I.say("\n  Best Leads:");
        if (bestLeads.empty()) {
          I.say("    None");
        }
        else for (Lead lead : bestLeads) {
          I.say("    "+lead.activeInfo()+" ("+lead.leadRating+")");
        }
      }
      
      List <Person> agents = new List();
      Visit.appendTo(agents, played.roster());
      
      for (Person agent : agents) {
        agent.clearAssignments();
      }
      if (bustLead != null) for (Person agent : agents) {
        agent.addAssignment(bustLead);
      }
      else while (! (agents.empty() || bestLeads.empty())) {
        Lead lead = bestLeads.removeFirst();
        Person agent = agents.removeFirst();
        agent.addAssignment(lead);
      }
      
      if (verbose || concise) {
        I.say("\n  Assignments:");
        for (Person agent : played.roster()) {
          Assignment a = agent.topAssignment();
          if (a == null) continue;
          I.say("    "+agent+" -> "+a.activeInfo());
        }
      }
      
      if (askToLoop) {
        while (true) {
          I.say("\n  Proceed? (y/Yes, n/No, s/Save, f/Finish Loop, r/Reprint)");
          I.say("    ");
          String response = I.listen().toUpperCase().substring(0, 1);
          if      (response.equals("N")) System.exit(0);
          else if (response.equals("S")) world.performSave();
          if      (response.equals("F")) { askToLoop = false; break; }
          else if (response.equals("R")) continue loop;
          else if (response.equals("Y")) break;
          else continue;
        }
      }
      
      world.updateWorld(12);
      
      if (world.activeScene() != null) {
        bust = world.activeScene();
        break;
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



