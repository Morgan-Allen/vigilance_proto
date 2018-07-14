

package proto.test;
import proto.game.event.*;
import proto.game.person.*;
import proto.common.DefaultGame;
import proto.common.GameSettings;
import proto.common.RunGame;
import proto.common.Trait;
import proto.content.agents.*;
import proto.content.events.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.base.CasesFX;



public class DebugPlotSingleLead extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugPlotSingleLead(), "saves/debug_plot_before");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world, false);
    
    Base crooks = world.baseFor(Crooks.THE_MORETTI_FAMILY);
    DebugPlotUtils.assignRandomPlot(crooks, PlotTypes.ALL_TYPES);
    
    return world;
  }
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    if (afterLoad) return false;
    
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
        I.say("  "+clue.role()+" "+clue+" ("+clue.leadType()+")");
        
        Clue match = null;
        if (clue.isConfirmation()) {
          match = clue;
        }
        else for (Clue p : possible) {
          if (p.matches(clue)) match = p;
        }
        
        if (match == null) {
          I.say("    Should not be possible!");
        }
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
        DebugPlotUtils.printSuspects(suspects, role);
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
    
    I.say("\nFailed to execute single lead correctly...");
    return false;
  }
  
}








