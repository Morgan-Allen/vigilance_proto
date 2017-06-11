

package proto.common;
import proto.game.world.*;
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
    boolean beforeOK = dpb.runTests(dpb.world, false, true);
    
    DebugPlotAfter dpa = new DebugPlotAfter();
    dpa.world = dpa.setupWorld();
    boolean afterOK = dpa.runTests(dpa.world, false, true);
    
    DebugPlotAfter forE = new DebugPlotAfter();
    forE.world = forE.setupWorld();
    boolean effectOK = runPlotToCompletion(forE.world());
    
    DebugPlotSpooked forS = new DebugPlotSpooked();
    forS.world = forS.setupWorld();
    boolean spooksOK = forS.runTests(forS.world, false, true);
    
    I.say("\n\n\nPlot Testing Complete.");
    I.say("  Single lead tests okay:    "+leadOK  );
    I.say("  Plot before okay:          "+beforeOK);
    I.say("  Plot after okay:           "+afterOK );
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
    
    Plot     plot  = world.events.nextPlot();
    int      time  = world.timing.totalHours();
    Place    place = plot.location(Plot.ROLE_TARGET);
    CaseFile file  = played.leads.caseFor(plot);
    Role     roleP = plot.roleFor(place);
    Trait    trait = (Trait) Rand.pickFrom(place.traits());
    
    //
    //  First, we check to ensure that basic clue-recording is functional:
    boolean recordCorrect = true;
    
    Clue toPlace = Clue.locationClue(plot, roleP, place.region(), 0);
    Clue further = Clue.locationClue(plot, roleP, place.region(), 1);
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
    
    Clue confirms = Clue.confirmSuspect(plot, roleP, place);
    Clue forTrait = Clue.traitClue     (plot, roleP, trait);
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
    
    //
    //  Secondly, we ensure that suitable leads are available:
    Pick <Lead> toFollow = new Pick();
    for (Lead lead : played.leads.leadsFor(place)) {
      if (lead.type.medium == LeadType.MEDIUM_ASSAULT){
        continue;
      }
      if (lead.type.canDetect(place, plot, place)) {
        toFollow.compare(lead, Rand.num() + 0.5f);
      }
    }
    Lead followed = toFollow.result();
    if (followed == null) {
      I.say("\nCould not find viable lead!");
      return false;
    }
    followed.setResult = 0.5f;
    
    for (Person agent : played.roster()) {
      agent.addAssignment(followed);
    }
    I.say("\nWill follow lead: "+followed);
    I.say("Possible clues:");
    
    Batch <Clue> possible = new Batch();
    for (Element e : plot.allInvolved()) {
      for (Clue clue : ClueUtils.possibleClues(
        plot, e, e, played, followed.type
      )) {
        possible.add(clue);
      }
    }
    for (Clue clue : possible) {
      I.say("  "+clue.role()+" "+clue);
    }
    I.say("Agents assigned: "+followed.assigned());
    
    //
    //  And finally, we check to ensure that any clue extracted will usefully
    //  narrow the field of suspects and was within the range of possible
    //  clues for that lead-type.
    while (! plot.complete()) {
      //
      //  We update the world, then extract any clues derived from the followed
      //  leads.
      world.updateWorld(6);
      I.say("\nUpdating world, time: "+world.timing.totalHours());
      I.say("  Plot complete? "+plot.complete()+", tense: "+plot.tense());
      
      Series <Clue> newClues = played.leads.extractNewClues();
      Batch <Clue> fromFollow = new Batch();
      for (Clue c : newClues) if (c.source() == followed) fromFollow.add(c);
      if (fromFollow.empty()) continue;
      
      //
      //  Then we check to ensure that any clues extracted match one of those
      //  originally considered possible:
      I.say("\nClues extracted: ");
      Batch <Role> roles = new Batch();
      boolean narrowed = true, allPossible = true;
      
      for (Clue clue : fromFollow) {
        I.say("  "+clue.role()+": "+clue+" ("+clue.leadType()+")");
        
        Clue match = null;
        if (clue.isConfirmation()) {
          match = clue;
        }
        else for (Clue p : possible) {
          if (p.makesRedundant(clue) && clue.makesRedundant(p)) match = p;
        }
        
        if (match == null) I.say("    Should not be possible!");
        allPossible &= match != null;
        roles.include(clue.role());
      }
      if (! allPossible) {
        I.say("  Not all Clues were originally possible!");
      }
      
      //
      //  Then ensure that the range of suspects has narrowed for at least one
      //  of the roles associated with the plot.
      for (Role role : roles) {
        Series <Element> suspects = played.leads.suspectsFor(role, plot);
        printSuspects(suspects, role);
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
  
  
  public static boolean enterLeadFollowingLoop(
    World world, Plot plot, boolean autoWin,
    boolean askToLoop, int verbosity,
    Element... bestTrail
  ) {
    boolean report = verbosity > 0;
    if (report) I.say("\n\nENTERING DEBUG LOOP FOR "+plot);
    //
    //  Your task is to pursue leads on a particular known or suspected target
    //  until you narrow down their identity to a single suspect.  (Ideally,
    //  you want to narrow down the headquarters as well)- but that might not
    //  be possible until right at the end.
    GameSettings.noTipoffs = true;
    
    Base     played    = world.playerBase();
    Element  first     = bestTrail[0];
    Role     firstRole = plot.roleFor(first);
    Role     lastRole  = plot.roleFor((Element) Visit.last(bestTrail));
    Clue     tip       = Clue.confirmSuspect(plot, firstRole, first);
    CaseFile file      = played.leads.caseFor(plot);
    boolean  foundLast = false;
    file.recordClue(tip, LeadType.TIPOFF, 0, first.region());
    
    while (true) {
      int time = world.timing.totalHours();
      
      if (report) {
        I.say("\n\nTime is now: "+time);
        I.say("Will assess potential leads.");
      }
      
      Pick <Lead> pick = new Pick();
      
      for (Role r : Plot.CORE_ROLES) {
        Series <Element> suspects = played.leads.suspectsFor(r, plot);
        if (report) printSuspects(suspects, r);
        
        if (suspects.size() > 1) {
          if (report) I.say("  No definite suspect for "+r);
          continue;
        }
        Element place = plot.location(r);
        
        for (Lead l : played.leads.leadsFor(place)) {
          if (l.type.medium == LeadType.MEDIUM_ASSAULT) {
            continue;
          }
          if (plot.outcome(l) != LeadType.RESULT_NONE) {
            continue;
          }
          if (report) I.say("  Assessing lead: "+l);
          pick.compare(l, Rand.num());
        }
        
        if (r == lastRole) foundLast = true;
      }
      
      if (plot.complete()) {
        if (report) I.say("Plot is complete: "+plot);
        break;
      }
      if (foundLast) {
        if (report) I.say("\nSuccessfully found intended target!");
        break;
      }
      
      Lead followed = pick.result();
      if (followed == null) {
        if (report) I.say("\nNo suitable lead found.");
        break;
      }
      
      if (report) I.say("\nPicked lead: "+followed);
      for (Person p : played.roster()) {
        p.addAssignment(followed);
      }
      
      if (autoWin) {
        followed.setResult = 0.5f;
      }
      else {
        followed.setResult = -1;
      }
      
      while (plot.outcome(followed) == LeadType.RESULT_NONE) {
        world.updateWorld(6);
        time = world.timing.totalHours();
        
        Series <Clue> clues = played.leads.extractNewClues();
        if (report && ! clues.empty()) {
          I.say("\nClues extracted:");
          for (Clue c : clues) I.say("  "+c.role()+" "+c);
        }
        
        if (plot.complete()) break;
      }
      
      if (report) {
        I.say("\nOutcome for lead: "+plot.outcome(followed)+", time: "+time);
      }
    }
    
    return foundLast;
  }
  
  
  public static void printSuspects(Series <Element> suspects, Role role) {
    I.say("\n"+suspects.size()+" Suspects for role: "+role);
    int sID = 0;
    if (suspects.empty()) {
      I.say("  <None>");
    }
    else for (Element e : suspects) {
      if (++sID > 3) { I.say("  ...etc."); break; }
      I.say("  "+e);
    }
  }
  
  
  /*
  I.say("\nGetting possible clues for HQ:");
  for (Lead l : played.leads.leadsFor(HQ)) {
    I.say("  "+l);
    for (Clue c : ClueUtils.possibleClues(
      instigated, HQ, hideout, played, l.type
    )) {
      I.say("    "+c);
    }
  }
  //*/
  
}




