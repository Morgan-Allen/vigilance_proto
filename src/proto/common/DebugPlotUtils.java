

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
    
    DebugPlotSingleLead forL = new DebugPlotSingleLead();
    forL.world = forL.setupWorld();
    boolean leadOK = forL.runTests(forL.world, false, true);
    
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
    
    DebugPlotFindHQ forHQ = new DebugPlotFindHQ();
    forHQ.world = forHQ.setupWorld();
    boolean foundHQ = forHQ.runTests(forHQ.world, false, true);
    
    I.say("\n\n\nPlot Testing Complete.");
    I.say("  Single lead tests okay:    "+leadOK  );
    I.say("  Plot before okay:          "+beforeOK);
    I.say("  Plot after okay:           "+afterOK );
    I.say("  Plot-effect tests okay:    "+effectOK);
    I.say("  Spooking tests okay:       "+spooksOK);
    I.say("  HQ detection okay:         "+foundHQ );
  }
  
  
  public static Plot assignRandomPlot(Base base, PlotType... types) {
    PlotType type  = (PlotType) Rand.pickFrom(types);
    Plot     crime = type.initPlot(base);
    crime.fillAndExpand();
    base.plots.assignRootPlot(crime, 0);
    return crime;
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
      
      if (plot.expired()) {
        if (report) I.say("Plot is expired: "+plot);
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
        
        if (plot.expired()) break;
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
  
}




