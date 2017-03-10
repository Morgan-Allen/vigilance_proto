

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;




public class TestCrime {
  
  
  final static Crime.Role
    ROLE_TAILS = new Crime.Role("role_tails"),
    ROLE_GRABS = new Crime.Role("role_grabs"),
    ROLE_DRUGS = new Crime.Role("role_drugs");
  
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
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Place hideout = crooks.place();
    Crime kidnap = TYPE_KIDNAP.initCrime(crooks);
    
    Pick <Person> pick = new Pick();
    for (Region r : world.regions()) for (Place b : r.buildSlots()) {
      if (b != null) for (Person p : b.residents()) {
        //  TODO:  Use closeness to another, wealthy person here...
        pick.compare(p, Rand.num());
      }
    }
    
    Person target = pick.result();
    Series <Person> goons = kidnap.goonsOnRoster();
    
    kidnap.assignTarget(target, target.resides());
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
    
    I.say("\n\nRoles are: "+kidnap.entries);
    Lead lead = new Lead(Lead.SURVEIL_PERSON, kidnap, crooks.leader());
    
    for (Crime.Contact contact : kidnap.contacts) {
      if (! lead.canDetect(contact, Lead.TENSE_ANY, kidnap)) continue;
      
      Series <Clue> possible = lead.possibleClues(
        contact, Lead.TENSE_ANY, kidnap
      );
      if (possible.empty()) continue;
      
      I.say("\nAssessing possible clues from contact.\n"+contact);
      for (Clue clue : possible) {
        I.say("  "+clue);
      }
    }
  }
  
  
}






