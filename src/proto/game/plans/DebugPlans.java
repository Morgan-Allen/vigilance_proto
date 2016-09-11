

package proto.game.plans;
import proto.util.*;



public class DebugPlans {
  
  
  public static void main(String args[]) {
    I.say("Now running sketch!\n");
    
    Thing world = new Thing(Thing.TYPE_PLACE , "World"     );
    Thing boss  = new Thing(Thing.TYPE_PERSON, "Crime Boss");
    Batch <Thing> crooks = new Batch();
    
    for (int n = 5; n-- > 0;) {
      Thing civvy = genRandomPerson();
      civvy.setInside(world, true);
      Thing crook = genRandomPerson();
      crook.setOwner(boss);
      crook.setInside(world, true);
      crooks.add(crook);
    }
    I.say(world.longDescription());
    
    I.say("\nBeginning plan-generation...");
    Plan plan = new Plan(boss, world);
    Action firstStep = new Action(ActionTypes.HEIST, plan);
    plan.addGoal(firstStep, 10);
    for (Thing crook : crooks) plan.addObtained(crook);
    
    for (int n = 2; n-- > 0;) plan.advancePlan();
    
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
    
    for (String stat : Thing.ALL_STATS) {
      person.setValue(stat, (int) (Math.random() * 10));
    }
    return person;
  }
  
  
  public static Object pickFrom(Object array[]) {
    return array[(int) (Math.random() * array.length)];
  }
  
}








