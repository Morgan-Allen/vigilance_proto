

package proto.common;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;




/*
 TODO:  Test to see if plots can be completed without guaranteed wins?
//*/


public class DebugPlotUtils {
  
  
  public static void main(String args[]) {
    runPlotsDebugSuite();
  }
  
  
  public static void runPlotsDebugSuite() {
    
    DebugPlotBefore forL = new DebugPlotBefore();
    forL.world = forL.setupWorld();
    boolean leadOK = runSingleLeadTests(forL.world());
    
    DebugPlotBefore dpb = new DebugPlotBefore();
    dpb.world = dpb.setupWorld();
    boolean bOK = dpb.runTests(dpb.world, false, true);
    
    DebugPlotAfter dpa = new DebugPlotAfter();
    dpa.world = dpa.setupWorld();
    boolean aOK = dpa.runTests(dpa.world, false, true);
    
    DebugPlotAfter forE = new DebugPlotAfter();
    forE.world = forE.setupWorld();
    boolean effectOK = runPlotToCompletion(forE.world());
    
    I.say("\n\n\nPlot Testing Complete.");
    I.say("  Single lead tests okay:    "+leadOK  );
    I.say("  Investigation before okay: "+bOK     );
    I.say("  Investigation after okay:  "+aOK     );
    I.say("  Plot-effect tests okay:    "+effectOK);
  }
  
  
  public static boolean runSingleLeadTests(World world) {
    
    Base played = world.playerBase();
    GameSettings.noTipoffs = true;
    world.updateWorld(0);
    played.leads.extractNewClues();
    
    Plot     plot    = world.events.latestPlot();
    int      time    = world.timing.totalHours();
    Step     current = plot.currentStep();
    Place    place   = current.goes();
    CaseFile file    = played.leads.caseFor(plot);
    
    boolean recordCorrect = true;
    Clue toPlace = Clue.locationClue(
      plot, plot.roleFor(place), place.region(), 0
    );
    Clue further = Clue.locationClue(
      plot, plot.roleFor(place), place.region(), 1
    );
    file.recordClue(toPlace, Lead.LEAD_REPORT, time, place);
    file.recordClue(further, Lead.LEAD_REPORT, time, place);
    
    I.say("\nGenerated location clue: "+toPlace.longDescription(played));
    if (! file.clues().includes(toPlace)) {
      I.say("  Clue was not recorded.");
      recordCorrect = false;
    }
    if (file.clues().includes(further)) {
      I.say("  Redundant clue was stored.");
      recordCorrect = false;
    }
    file.wipeRecord();
    if (! recordCorrect) return false;
    I.say("  Clue was recorded correctly.");
    I.say("  Current step is: "+current);
    
    Pick <Lead> toFollow = new Pick();
    for (Lead lead : played.leads.leadsFor(place)) {
      if (lead.type.medium == Lead.MEDIUM_ASSAULT){
        continue;
      }
      if (lead.canDetect(current, current.tense(), plot, time)) {
        toFollow.compare(lead, Rand.num() + 0.5f);
      }
    }
    Lead followed = toFollow.result();
    if (followed == null) {
      I.say("\nCould not find viable lead!");
      return false;
    }
    followed.setResult = 0.5f;
    
    Batch <Clue> possible = new Batch();
    for (Element e : current.involved()) {
      Visit.appendTo(possible, current.possibleClues(e, followed));
    }
    for (Person agent : played.roster()) {
      agent.addAssignment(followed);
    }
    I.say("\nWill follow lead: "+followed);
    I.say("Possible clues:");
    for (Clue clue : possible) {
      I.say("  "+clue.role()+" "+clue);
    }
    I.say("Agents assigned: "+followed.assigned());
    
    while (! plot.complete()) {
      world.updateWorld(12);
      
      Series <Clue> extracted = played.leads.extractNewClues();
      if (extracted.empty()) continue;
      
      Batch <Plot.Role> roles = new Batch();
      boolean narrowed = true, allPossible = true;
      for (Clue clue : extracted) {
        Clue match = null;
        for (Clue p : possible) {
          if (p.makesRedundant(clue) && clue.makesRedundant(p)) match = p;
        }
        allPossible &= match != null;
        roles.include(clue.role());
      }
      
      I.say("\nClues extracted: ");
      for (Clue clue : extracted) {
        I.say("  "+clue.role()+" "+clue+" ("+clue.leadType()+")");
      }
      
      if (! allPossible) {
        I.say("  Not all Clues were originally possible!");
      }
      
      for (Plot.Role role : roles) {
        Series <Element> suspects = played.leads.suspectsFor(role, plot);
        I.say("\n"+suspects.size()+" Suspects for role: "+role);
        int sID = 0;
        if (suspects.empty()) {
          I.say("  <None>");
        }
        else for (Element e : suspects) {
          if (++sID > 3) { I.say("  ...etc."); break; }
          I.say("  "+e);
        }
        if (suspects.size() < world.inside().size()) {
          I.say("  Suspects were narrowed!");
        }
        else {
          I.say("  Does not narrow suspects!");
          narrowed = false;
        }
      }
      
      return narrowed && allPossible;
    }
    return false;
  }
  
  
  public static boolean enterPlotDebugLoop(
    World world, Plot plot,
    boolean askToLoop, boolean concise,
    Element... bestTrail
  ) {
    Base played = world.playerBase();
    Scene bust = null;
    boolean verbose = ! concise;
    
    loop: while (! plot.complete()) {
      int  time    = world.timing.totalHours();
      Step doing   = plot.currentStep();
      int  tenseDo = doing == null ? Lead.TENSE_NONE : doing.tense();
      
      if (concise || verbose) {
        I.say("\n\n\nClues acquired:");
        for (Clue clue : played.leads.extractNewClues()) {
          I.say("  "+clue.role()+" "+clue+" ("+clue.leadType()+")");
        }
      }
      
      if (concise) {
        I.say("\n\n\nUpdating Investigation into "+plot);
        I.say("  Current time: "+time+", step: "+doing.label());
      }
      if (verbose) {
        I.say("\n\n\nUpdating Investigation into "+plot);
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
      
      for (Element suspect : plot.allInvolved()) {
        for (Lead lead : played.leads.leadsFor(suspect)) {
          lead.leadRating = 0;
          lead.noScene = true;
        }
      }
      for (Element suspect : plot.allInvolved()) {
        float evidence = played.leads.evidenceAgainst(suspect, plot, false);
        if (evidence <= 0) continue;
        
        int       trailIndex = 1 + Visit.indexOf(suspect, bestTrail);
        boolean   shouldBust = suspect == Visit.last(bestTrail);
        Plot.Role role       = plot.roleFor(suspect);
        Place     lastKnown  = played.leads.lastKnownLocation(suspect);
        Place     location   = suspect.place();
        
        if (verbose) {
          I.say("\n  "+suspect+" ("+role+")");
          I.say("  Last Seen: "+lastKnown+"  At: "+location);
          I.say("  Should Bust? "+shouldBust);
          I.say("  Evidence Rating: "+evidence+"  Trail ID: "+trailIndex);
          for (Clue clue : played.leads.cluesFor(plot, role, true)) {
            I.say("    "+clue.traitDescription()+" ("+clue.leadType()+")");
          }
        }
        
        for (Lead lead : played.leads.leadsFor(suspect)) {
          if (lead.type.medium == Lead.MEDIUM_ASSAULT) {
            if (shouldBust) { bustLead = lead; lead.noScene = false; }
            else continue;
          }
          float numDetects = 0, numSteps = plot.allSteps().size();
          for (Step step : plot.allSteps()) {
            if (lead.canDetect(step, step.tense(), plot, time)) {
              if (step.tense() == Lead.TENSE_DURING) numDetects += numSteps;
              else numDetects++;
            }
          }
          if ((numDetects /= numSteps) > 0) {
            float rating =  1 - Nums.min(1, Nums.abs(1 - evidence));
            rating       *= Nums.max(0.5f, trailIndex) * numDetects;
            lead.leadRating = Nums.max(rating, lead.leadRating);
            lead.setResult  = 100f;
            bestLeads.include(lead);
          }
        }
      }
      bestLeads.queueSort();
      
      if (verbose) {
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
      return true;
    }
    else {
      I.say("\n\nINVESTIGATION FAILED: "+plot.complete());
      return false;
    }
  }
  
  
  
  public static boolean runPlotToCompletion(World world) {
    Plot plot = world.events.latestPlot();
    Region affects = plot.target().region();
    
    I.say("\n\n\nRunning plot to completion: "+plot);
    I.say("  Region affected: "+affects);
    
    float trustB = affects.currentValue(Region.TRUST     );
    float deterB = affects.currentValue(Region.DETERRENCE);
    GameSettings.noDeterDecay = true;
    GameSettings.noTrustDecay = true;
    
    while (! plot.complete()) {
      world.updateWorld(12);
    }
    
    float trustA = affects.currentValue(Region.TRUST     );
    float deterA = affects.currentValue(Region.DETERRENCE);
    
    if (trustB == trustA && deterA == deterB) {
      I.say("  No change in regional trust or deterrence!");
      return false;
    }
    
    for (EventEffects effects : plot.allEffects()) {
      I.say("  Effects were:");
      I.say("    Collateral: "+effects.collateralRating);
      I.say("    Getaways:   "+effects.getawaysRating  );
      I.say("    Trust:      "+effects.trustEffect     );
      I.say("    Deterrence: "+effects.deterEffect     );
    }
    
    I.say("  Trust:      "+trustB+" -> "+trustA);
    I.say("  Deterrence: "+deterB+" -> "+deterA);
    return true;
  }
}





