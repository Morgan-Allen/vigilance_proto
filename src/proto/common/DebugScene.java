

package proto.common;
import proto.game.world.*;
import proto.util.I;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.places.*;

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
    super("saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultNations(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    //  Create bosses and create experts.
    //  Associate bosses and experts with Bases.
    //  Have bosses create plans.
    //  Have bosses execute those plans until they get interrupted and/or have
    //  to be revised and/or are abandoned for fear of exposure.
    
    //  Create Events for steps in those plans, and pop them into the event
    //  queue.  After they transpire, attach clues to persons, places or things
    //  involved (and/or generate tipoffs.)
    
    //  Clues point from one object to another object.
    
    Person boss = new Person(Crooks.MOBSTER, world, "Crime Boss");
    Person perp = Crooks.randomOfKind(Crooks.GOON, world);
    Person victim = Crooks.randomOfKind(Crooks.CIVILIAN, world);
    Region port = world.regionFor(Regions.PORT_ADAMS);
    Place building = port.buildSlot(0);
    
    Place home = new Place(Facilities.COMMUNITY_COLLEGE, 0, world);
    world.setInside(home, true);
    world.regionFor(Regions.BLACKGATE).setAttached(home, true);
    victim.setResidence(home);
    
    Plan plan = new Plan(boss, world, StepTypes.ALL_TYPES);
    PlanStep kidnapStep = new PlanStep(StepTypes.KIDNAP, plan);
    kidnapStep.setGives(victim);
    kidnapStep.setNeed(TypeKidnapping.Needs.VENUE, home);
    kidnapStep.setNeed(TypeKidnapping.Needs.ALARM_CRACKER, perp);
    
    Event kidnapEvent = kidnapStep.spawnEvent(world, 24);
    world.events.scheduleEvent(kidnapEvent);
    
    Clue tipoff = new Clue(ClueTypes.TIPOFF, world, perp);
    building.setAttached(tipoff, true);
    building.setAttached(perp, true);
    world.playerBase().leads.addLead(perp);
    
    //  Okay.  So.  You get a tipoff suggesting that a given perp is involved
    //  in an upcoming crime.  You investigate them, they lead you to the main
    //  crime.  Once you have the main crime, you can Guard the location (or
    //  the victim, or whatev) to prevent the crime taking place.
    
    /*
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
    //*/
    
    return world;
  }
}




