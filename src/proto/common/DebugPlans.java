

package proto.common;
import proto.content.agents.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.content.events.*;
import proto.content.places.*;
import proto.util.*;



public class DebugPlans extends RunGame {
  
  
  public static void main(String args[]) {
    GameSettings.freeTipoffs = true;
    runGame(new DebugPlans(), "saves/debug_plans");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultTime   (world);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    final Place bank = world.regionFor(Regions.SECTOR02).setupFacility(
      Facilities.BUSINESS_PARK, 1, world.playerBase(), true
    );
    
    I.say("\nBeginning plan-generation...");
    final Base crookBase = world.bases().atIndex(1);
    final Person boss = crookBase.leader();
    
    /*
    final Plan plan = new Plan(boss, world, StepTypes.ALL_TYPES);
    plan.verbose = true;
    for (Element crook : crookBase.roster()) plan.addObtained(crook);
    
    //  TODO:  You need to avoid plans that are overly baroque or involve
    //  seemingly contradictory goals (such as both bribing and kidnapping the
    //  same expert.)
    
    //  TODO:  Also, you need to avoid different bosses interfering with
    //  eachother if reasonably possible?
    
    final PlanStep firstStep = StepTypes.HEIST.asGoal(bank, plan);
    plan.addGoal(firstStep, 10);
    plan.advancePlan(6);
    crookBase.plans.assignPlan(plan, 16);
    //*/
    
    return world;
  }
}





