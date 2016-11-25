

package proto.game.plans;
import proto.util.*;



public class DebugPlans {
  
  
  public static void main(String args[]) {
    I.say("Now running sketch!\n");
    
    Thing world = new Thing(Thing.TYPE_PLACE , "World"     );
    Thing boss  = new Thing(Thing.TYPE_PERSON, "Crime Boss");
    Batch <Thing> civvies = new Batch();
    Batch <Thing> crooks  = new Batch();
    
    for (int n = 5; n-- > 0;) {
      Thing civvy = genRandomPerson();
      civvy.beInside(world, true);
      civvies.add(civvy);
      Thing crook = genRandomPerson();
      crook.setOwner(boss);
      crook.beInside(world, true);
      crooks.add(crook);
    }
    
    Thing bank = new Thing(Thing.TYPE_PLACE, "Bank");
    bank.setValue(Thing.PROP_SAFE, true);
    bank.beInside(world, true);
    Thing worker = (Thing) Rand.pickFrom(civvies);
    worker.setOwner(bank);

    I.say(world.longDescription());
    
    I.say("\nBeginning plan-generation...");
    Plan plan = new Plan(boss, world);
    PlanStep firstStep = new PlanStep(StepTypes.HEIST, plan);
    plan.addGoal(firstStep, 10);
    for (Thing crook : crooks) plan.addObtained(crook);
    
    for (int n = 6; n-- > 0;) plan.advancePlan();
    
    I.say("\n\nFinal plan: ");
    for (PlanStep step : plan.steps) {
      I.say("  "+step.langDescription());
    }
  }
  
  
  final static String FIRST_NAMES[] = {
    "Lenny", "Steve", "Marco", "Tommy", "Joey", "Abed", "Lance", "Zeke"
  };
  final static String LAST_NAMES[] = {
    "Cruz", "Martin", "Conway", "Tomasi", "Braun", "Phelton", "Mara", "Soren"
  };
  
  
  private static Thing genRandomPerson() {
    String name = pickFrom(FIRST_NAMES)+" "+pickFrom(LAST_NAMES);
    Thing person = new Thing(Thing.TYPE_PERSON, name);
    
    for (String stat : Thing.PERSON_STATS) {
      person.setValue(stat, (int) (Math.random() * 10));
    }
    return person;
  }
  
  
  public static Object pickFrom(Object array[]) {
    return array[(int) (Math.random() * array.length)];
  }
  
}








