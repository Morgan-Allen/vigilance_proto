

package proto.common;
import proto.game.world.*;
import proto.content.events.PlotTypes;
import proto.game.event.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.base.*;



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
    
    DebugPlotSpooked forS = new DebugPlotSpooked();
    forS.world = forS.setupWorld();
    boolean spooksOK = forS.runTests(forS.world, false, true);
    
    I.say("\n\n\nPlot Testing Complete.");
    I.say("  Single lead tests okay:    "+leadOK  );
    I.say("  Investigation before okay: "+bOK     );
    I.say("  Investigation after okay:  "+aOK     );
    I.say("  Plot-effect tests okay:    "+effectOK);
    I.say("  Spooking tests okay:       "+spooksOK);
  }
  
  
  public static Plot assignRandomPlot(Base base, PlotType... types) {
    PlotType type  = (PlotType) Rand.pickFrom(types);
    Plot     crime = type.initPlot(base);
    crime.fillAndExpand();
    base.plots.assignRootPlot(crime, 0);
    return crime;
  }
  
  
  public static boolean runSingleLeadTests(World world) {
    
    Base played = world.playerBase();
    GameSettings.noTipoffs = true;
    world.updateWorld(0);
    played.leads.extractNewClues();
    
    Plot     plot  = world.events.nextActivePlot();
    int      time  = world.timing.totalHours();
    Step     step  = plot.currentStep();
    Place    place = plot.goes(step);
    CaseFile file  = played.leads.caseFor(plot);
    Role     roleP = plot.roleFor(place);
    Trait    trait = (Trait) Rand.pickFrom(place.traits());
    
    boolean recordCorrect = true;
    Clue toPlace = Clue.locationClue(plot, roleP, step, place.region(), 0);
    Clue further = Clue.locationClue(plot, roleP, step, place.region(), 1);
    file.recordClue(toPlace, LeadType.REPORT, time, place);
    file.recordClue(further, LeadType.REPORT, time, place);
    
    I.say("\nGenerated location clues...");
    I.say("  "+CasesFX.longDescription(toPlace, played));
    I.say("  "+CasesFX.longDescription(further, played));
    if (! file.clues().includes(toPlace)) {
      I.say("  Clue was not recorded.");
      recordCorrect = false;
    }
    if (file.clues().includes(further)) {
      I.say("  Redundant clue was stored.");
      recordCorrect = false;
    }
    file.wipeRecord();
    
    Clue confirms = Clue.confirmSuspect(plot, roleP, step, place);
    Clue forTrait = Clue.traitClue     (plot, roleP, step, trait);
    file.recordClue(confirms, LeadType.REPORT, time, place);
    file.recordClue(forTrait, LeadType.REPORT, time, place);

    I.say("\nGenerated trait clue after confirmation...");
    I.say("  "+CasesFX.longDescription(confirms, played));
    I.say("  "+CasesFX.longDescription(forTrait, played));
    if (! file.clues().includes(confirms)) {
      I.say("  Clue was not recorded.");
      recordCorrect = false;
    }
    if (file.clues().includes(forTrait)) {
      I.say("  Redundant clue was stored.");
      recordCorrect = false;
    }
    
    file.wipeRecord();
    played.leads.extractNewClues();
    if (! recordCorrect) return false;
    I.say("  Clues were recorded correctly.");
    I.say("  Current step is: "+step);
    
    Pick <Lead> toFollow = new Pick();
    for (Lead lead : played.leads.leadsFor(place)) {
      if (lead.type.medium == LeadType.MEDIUM_ASSAULT){
        continue;
      }
      if (lead.type.canDetect(place, step, plot, place)) {
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
    for (Element e : plot.involved(step)) {
      Visit.appendTo(possible, step.possibleClues(
        plot, e, e, step, played, LeadType.TIPOFF
      ));
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
      world.updateWorld(6);
      
      Series <Clue> extracted = played.leads.extractNewClues();
      if (extracted.empty()) continue;
      
      Batch <Role> roles = new Batch();
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
      
      for (Role role : roles) {
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
      int  tenseDo = doing == null ? LeadType.TENSE_NONE : plot.tense(doing);
      
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
        I.add(" ["+LeadType.TENSE_DESC[Nums.clamp(tenseDo, 3)]+"]");
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
        
        int     trailIndex = 1 + Visit.indexOf(suspect, bestTrail);
        boolean shouldBust = suspect == Visit.last(bestTrail);
        Role    role       = plot.roleFor(suspect);
        Place   lastKnown  = played.leads.lastKnownLocation(suspect);
        Place   location   = suspect.place();
        
        if (verbose) {
          I.say("\n  "+suspect+" ("+role+")");
          I.say("  Last Seen: "+lastKnown+"  At: "+location);
          I.say("  Should Bust? "+shouldBust);
          I.say("  Evidence Rating: "+evidence+"  Trail ID: "+trailIndex);
          for (Clue clue : played.leads.cluesFor(plot, role, true)) {
            I.say("    "+clue+" ("+clue.leadType()+")");
          }
        }
        
        for (Lead lead : played.leads.leadsFor(suspect)) {
          if (lead.type.medium == LeadType.MEDIUM_ASSAULT) {
            if (shouldBust) { bustLead = lead; lead.noScene = false; }
            else continue;
          }
          float numDetects = 0, numSteps = plot.allSteps().size();
          for (Step step : plot.allSteps()) {
            int tense = plot.tense(step);
            if (lead.type.canDetect(suspect, step, plot, suspect)) {
              if (tense == LeadType.TENSE_PRESENT) numDetects += numSteps;
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
          I.say("\n  Proceed? ");
          I.add("(y/Yes, n/No, s/Save, f/Finish Loop, r/Reevaluate, p/Play)");
          I.say("    ");
          String response = I.listen().toUpperCase().substring(0, 1);
          if      (response.equals("N")) System.exit(0);
          else if (response.equals("S")) world.performSave();
          if      (response.equals("F")) { askToLoop = false; break; }
          else if (response.equals("R")) continue loop;
          else if (response.equals("P")) return false;
          else if (response.equals("Y")) break;
        }
      }
      
      world.updateWorld(6);
      
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
    Plot plot = world.events.nextActivePlot();
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






