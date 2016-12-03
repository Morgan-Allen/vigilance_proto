

package proto.common;
import proto.game.world.*;
import proto.util.*;
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
    
    //  TODO:  You still need to generate tipoffs to indicate a plan is in
    //  progress.
    
    //  TODO:  Also, you need to avoid different bosses interfering with
    //  eachother (if reasonably possible.)
    
    /*
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
    //*/
    
    return world;
  }
}




