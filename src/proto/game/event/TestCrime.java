

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;




public class TestCrime {
  
  
  final static Crime.Role
    ROLE_TAILS = new Crime.Role("role_tails", "Tails"),
    ROLE_GRABS = new Crime.Role("role_grabs", "Grabs"),
    ROLE_DRUGS = new Crime.Role("role_drugs", "Drugs");
  
  static CrimeType TYPE_KIDNAP = new CrimeType(
    "Kidnapping", "crime_type_kidnap", null
  ) {
    protected Crime initCrime(Base base) {
      return new Crime(this, base) {};
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
    Crime kidnap = TYPE_KIDNAP.initCrime(crooks);
    
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
    
    kidnap.queueContacts(
      Lead.MEDIUM_MEET, World.HOURS_PER_DAY, Crime.ROLE_ORGANISER,
      ROLE_TAILS, ROLE_DRUGS, ROLE_GRABS
    );
    kidnap.queueContact(
      Lead.MEDIUM_SURVEIL,
      World.HOURS_PER_DAY,
      ROLE_TAILS, Crime.ROLE_TARGET
    );
    kidnap.queueContact(
      Lead.MEDIUM_MEET,
      World.HOURS_PER_DAY,
      ROLE_DRUGS, ROLE_GRABS
    );
    
    
    I.say("\n\nRoles are: ");
    for (Crime.RoleEntry entry : kidnap.entries) {
      I.say("  "+entry);
    }
    
    Lead lead = new Lead(Lead.LEAD_SURVEIL_PERSON, kidnap, crooks.leader());
    I.say("\nFollowing Lead...");
    
    for (Crime.Contact contact : kidnap.contacts) {
      if (! lead.canDetect(contact, Lead.TENSE_ANY, kidnap)) continue;
      
      float chance = 0, result = 0;
      chance = lead.followChance(sleuth);
      result = lead.followAttempt(sleuth, contact, Lead.TENSE_ANY, kidnap);
      
      I.say("  Detected: "+contact);
      I.say("  Chance "+chance+"  Result: "+result);
    }
    
    for (Crime.RoleEntry entry : kidnap.entries) {
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



