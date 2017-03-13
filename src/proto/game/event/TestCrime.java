

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;




public class TestCrime {
  
  
  final static Plot.Role
    ROLE_TAILS = new Plot.Role("role_tails", "Tails"),
    ROLE_GRABS = new Plot.Role("role_grabs", "Grabs"),
    ROLE_DRUGS = new Plot.Role("role_drugs", "Drugs");
  
  static PlotType TYPE_KIDNAP = new PlotType(
    "Kidnapping", "crime_type_kidnap", null
  ) {
    protected Plot initPlot(Base base) {
      return new Plot(this, base) {};
    }
  };
  
  
  public static void main(String args[]) {
    
    World world = new World();
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    Base heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Person sleuth = heroes.roster().first();
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Place hideout = crooks.place();
    Plot kidnap = TYPE_KIDNAP.initPlot(crooks);
    
    //  TODO:  Generate relationships (kin and otherwise) between persons in
    //  nearby venues!  
    
    Pick <Person> pick = new Pick();
    for (Region r : world.regions()) for (Place b : r.buildSlots()) {
      if (b != null) for (Person p : b.residents()) {
        //  TODO:  Use closeness to another, wealthy person here...
        pick.compare(p, Rand.num());
      }
    }
    
    Person target = pick.result();
    Series <Person> goons = kidnap.goonsOnRoster();
    
    kidnap.assignTarget   (target, target.resides());
    kidnap.assignOrganiser(crooks.leader(), hideout);
    kidnap.fillExpertRole(SIGHT_RANGE, goons, ROLE_TAILS);
    kidnap.fillExpertRole(MEDICINE   , goons, ROLE_DRUGS);
    kidnap.fillExpertRole(MUSCLE     , goons, ROLE_GRABS);
    
    kidnap.queueSteps(
      Lead.MEDIUM_MEET, World.HOURS_PER_DAY, Plot.ROLE_ORGANISER,
      ROLE_TAILS, ROLE_DRUGS, ROLE_GRABS
    );
    kidnap.queueStep(
      Lead.MEDIUM_SURVEIL,
      World.HOURS_PER_DAY,
      ROLE_TAILS, Plot.ROLE_TARGET
    );
    kidnap.queueStep(
      Lead.MEDIUM_MEET,
      World.HOURS_PER_DAY,
      ROLE_DRUGS, ROLE_GRABS
    );
    
    I.say("\n\nRoles are: ");
    for (Plot.RoleEntry entry : kidnap.entries) {
      I.say("  "+entry);
    }
    
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
    
  }
  
}



