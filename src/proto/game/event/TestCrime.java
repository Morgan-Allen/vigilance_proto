

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.content.agents.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;




public class TestCrime {
  
  
  final static Crime.RoleType
    ROLE_TAILS = new Crime.RoleType("role_tails"),
    ROLE_GRABS = new Crime.RoleType("role_grabs"),
    ROLE_DRUGS = new Crime.RoleType("role_drugs");
  
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
    Crime kidnap = TYPE_KIDNAP.initCrime(crooks);
    
    Pick <Person> pick = new Pick();
    for (Region r : world.regions()) for (Place b : r.buildSlots()) {
      if (b != null) for (Person p : b.residents()) {
        //  TODO:  Use closeness to another, wealthy person here...
        pick.compare(p, Rand.num());
      }
    }
    kidnap.assignTarget(pick.result());
    Series <Person> goons = kidnap.goonsOnRoster();
    
    kidnap.fillExpertRole(SIGHT_RANGE, goons, ROLE_TAILS);
    kidnap.fillExpertRole(MEDICINE   , goons, ROLE_DRUGS);
    kidnap.fillExpertRole(MUSCLE     , goons, ROLE_GRABS);
    
    I.say("Roles are: "+kidnap.roles);
    Lead lead = new Lead(kidnap, Lead.TYPE_CANVAS, 0.5f);
    I.say("Assessing possible clues for: "+kidnap);
    for (Clue clue : lead.possibleClues(kidnap, null, null)) {
      I.say("  "+clue);
    }
  }
  
  
}










