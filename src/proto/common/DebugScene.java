

package proto.common;
import proto.game.world.*;
import proto.util.Rand;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.Crooks;
import proto.content.agents.Heroes;
import proto.content.events.*;
import proto.content.scenes.*;
import java.awt.EventQueue;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DebugScene ex = new DebugScene();
        ex.setVisible(true);
      }
    });
  }
  
  
  DebugScene() {
    super("debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultNations(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    //  Create bosses and create experts.
    //  Have bosses create plans.
    //  Have bosses execute those plans until they get interrupted and/or have
    //  to be revised and/or are abandoned for fear of exposure.
    
    //  Create Events for either the plans in general and/or individuals steps
    //  in those plans, and pop them into the event queue associated with
    //  particular areas on the map.
    
    //  Generate leads either toward or from those events, and associate with
    //  particular regions and particular cases.  Use those to either perform
    //  perform forensic analysis and/or catch the criminals red-handed.
    
    Person boss = new Person(Crooks.MOBSTER, world, "Crime Boss");
    Person victim = Crooks.randomOfKind(Crooks.CIVILIAN, world);
    
    Plan plan = new Plan(boss, world, StepTypes.ALL_TYPES);
    PlanStep kidnapStep = new PlanStep(StepTypes.KIDNAP, plan);
    kidnapStep.setGives(victim);
    
    Event kidnapEvent = kidnapStep.spawnEvent(world);
    final FightLead combat = new FightLead(101, kidnapEvent.place());
    
    final Base base = world.playerBase();
    UrbanScene mission = new UrbanScene(world, 32);
    mission.addToTeam(base.firstOfKind(Heroes.HERO_BATMAN   ));
    mission.addToTeam(base.firstOfKind(Heroes.HERO_NIGHTWING));
    mission.addToTeam(base.firstOfKind(Heroes.HERO_BATGIRL  ));
    
    mission.assignMissionParameters(
      combat, kidnapEvent.place(), 0.5f, 100, null
    );
    mission.setupScene();
    
    world.enterScene(mission);
    
    return world;
  }
}



