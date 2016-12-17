

package proto.common;
import proto.content.agents.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.events.*;
import proto.content.places.*;
import proto.util.*;



public class DebugPlans {
  
  
  public static void main(String args[]) {
    
    Assets.compileAssetList("proto");
    Assets.advanceAssetLoading(-1);
    
    //  TODO:  Also, you need to avoid different bosses interfering with
    //  eachother (if reasonably possible.)
    
    I.say("Now running sketch!\n");
    
    World world = new World();
    Person boss = new Person(Crooks.MOBSTER, world, "Crime Boss");
    Batch <Person> civvies = new Batch();
    Batch <Person> crooks  = new Batch();
    
    for (int n = 5; n-- > 0;) {
      Person civvy = genRandomPerson(Civilians.CIVILIAN, world);
      world.setInside(civvy, true);
      civvies.add(civvy);
      Person crook = genRandomPerson(Crooks.GOON, world);
      world.setInside(crook, true);
      crooks.add(crook);
    }
    
    Place bank = new Place(Facilities.BUSINESS_PARK, 0, world);
    world.setInside(bank, true);
    Person worker = (Person) Rand.pickFrom(civvies);
    Place.setResident(worker, bank, true);
    
    I.say("\nBeginning plan-generation...");
    Plan plan = new Plan(boss, world, StepTypes.ALL_TYPES);
    plan.verbose = true;
    for (Element crook : crooks) plan.addObtained(crook);
    
    PlanStep firstStep = StepTypes.HEIST.asGoal(bank, plan);
    plan.addGoal(firstStep, 10);
    plan.advancePlan(6);
    plan.printFullPlan();
  }
  
  
  final static String FIRST_NAMES[] = {
    "Lenny", "Steve", "Marco", "Tommy", "Joey", "Abed", "Lance", "Zeke"
  };
  final static String LAST_NAMES[] = {
    "Cruz", "Martin", "Conway", "Tomasi", "Braun", "Phelton", "Mara", "Soren"
  };
  
  
  private static Person genRandomPerson(Kind kind, World world) {
    String name = pickFrom(FIRST_NAMES)+" "+pickFrom(LAST_NAMES);
    Person person = new Person(kind, world, name);
    
    for (Trait stat : PersonStats.ALL_STATS) {
      person.stats.setLevel(stat, 5 + (int) (Math.random() * 10), true);
    }
    for (Trait stat : PersonStats.ALL_SKILLS) {
      person.stats.setLevel(stat, (int) (Math.random() * 10), true);
    }
    
    return person;
  }
  
  
  public static Object pickFrom(Object array[]) {
    return array[(int) (Math.random() * array.length)];
  }
  
}








