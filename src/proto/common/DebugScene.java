

package proto.common;
import proto.game.world.*;
import proto.view.scene.SceneView;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.items.*;
import proto.content.places.*;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    runGame(new DebugScene(), "saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    GameSettings.freeTipoffs = true;
    
    Base   crooks   = world.bases().atIndex(1);
    Person boss     = new Person(Crooks.GANGSTER, world, "Crime Boss");
    Person perp     = Person.randomOfKind(Crooks.BRUISER, world);
    Person victim   = Person.randomOfKind(Civilians.CIVILIAN, world);
    Region port     = world.regionFor(Regions.SECTOR01);
    Place  building = port.buildSlot(0);
    
    Place home = new Place(Facilities.COMMUNITY_COLLEGE, 0, world);
    world.setInside(home, true);
    world.regionFor(Regions.SECTOR02).setAttached(home, true);
    victim.setResidence(home);
    building.setAttached(perp, true);
    boss.setBase(crooks);
    perp.setBase(crooks);
    
    Plan plan = new Plan(boss, world, StepTypes.ALL_TYPES);
    PlanStep kidnapStep = StepTypes.KIDNAP.asGoal(victim, plan);
    kidnapStep.setNeed(TypeKidnap.Needs.VENUE        , home, null);
    kidnapStep.setNeed(TypeKidnap.Needs.ALARM_CRACKER, perp, null);
    
    Event kidnapEvent = kidnapStep.matchedEvent();
    world.events.scheduleEvent(kidnapEvent, 8);
    
    final Base base = world.playerBase();
    final Lead tipoff = new LeadTipoff(base, perp);
    final CaseFile file = base.leads.caseFor(home);
    file.recordRole(kidnapEvent, CaseFile.ROLE_SCENE, tipoff);
    
    /*
    final Base HQ = world.playerBase();
    Task guarding = file.investigationOptions().first();
    for (Person p : HQ.roster()) {
      p.gear.equipItem(Gadgets.WING_BLADES, PersonGear.SLOT_WEAPON);
      p.gear.equipItem(Gadgets.KEVLAR_VEST, PersonGear.SLOT_ARMOUR);
      p.updateOnBase(0);
      guarding.setAssigned(p, true);
    }
    guarding.setCompleted(true);
    GameSettings.debugScene = true;
    //*/
    
    return world;
  }
}



