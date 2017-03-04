

package proto.common;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.items.*;
import proto.content.places.*;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    
    //GameSettings.freeTipoffs = true;
    //GameSettings.debugScene  = true;
    runGame(new DebugScene(), "saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultTime   (world);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
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
    plan.addStep(kidnapStep);
    boss.base().plans.assignPlan(plan, 0);
    
    
    //  TODO:  Just enter the scene directly.  Don't make this more complex
    //  than it needs to be.
    /*
    final Base base = world.playerBase();
    final Lead tipoff = new LeadTipoff(base, perp);
    final CaseFile file = base.leads.caseFor(home);
    Event kidnapEvent = kidnapStep.matchedEvent();
    file.recordRole(kidnapEvent, CaseFile.ROLE_SCENE, tipoff);
    
    final Base HQ = world.playerBase();
    Task guarding = file.investigationOptions().first();
    int ID = 0;
    for (Person p : HQ.roster()) {
      ItemType gadget1 = ID % 2 == 0 ? Gadgets.MED_KIT     : Gadgets.BOLAS   ;
      ItemType gadget2 = ID % 2 == 0 ? Gadgets.SONIC_PROBE : Gadgets.TEAR_GAS;
      p.gear.equipItem(gadget1, PersonGear.SLOT_ITEM_1);
      p.gear.equipItem(gadget2, PersonGear.SLOT_ITEM_2);
      p.updateOnBase();
      p.addAssignment(guarding);
      ID++;
    }
    guarding.setCompleted(true);
    //*/
    
    return world;
  }
}







