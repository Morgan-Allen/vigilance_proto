

package proto.common;
import proto.content.agents.*;
import proto.content.items.*;
import proto.content.places.*;
import proto.content.events.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.EventQueue;



public class DefaultGame extends RunGame {
  
  
  final public static String
    DEFAULT_SAVE_PATH = "saves/main_save.vgl";
  
  
  public static void main(String args[]) {
    runGame(new DefaultGame(), DEFAULT_SAVE_PATH);
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    initDefaultTime   (world);
    initDefaultRegions(world);
    initDefaultBase   (world);
    initDefaultCrime  (world);
    return world;
  }
  
  
  public static void initDefaultTime(World world) {
    world.timing.setStartDate(9, 7, 1984);
  }
  
  
  public static void initDefaultRegions(World world) {
    int numN = Regions.ALL_REGIONS.length;
    
    Region[] regions = new Region[numN];
    for (int n = 0; n < numN; n++) {
      regions[n] = new Region(Regions.ALL_REGIONS[n], world);
      regions[n].initialiseRegion();
    }
    world.attachRegions(regions);
  }
  
  
  public static void initDefaultBase(World world) {
    final Base base = new Base(Facilities.MANOR, world, false);
    
    Person leader = new Person(Heroes.HERO_PHOBOS, world);
    base.setLeader(leader);
    base.addToRoster(leader);
    base.addToRoster(new Person(Heroes.HERO_NIGHT_SWIFT, world));
    base.addToRoster(new Person(Heroes.HERO_DEIMOS     , world));
    base.addToRoster(new Person(Heroes.HERO_DR_YANG    , world));
    
    for (Person p : base.roster()) for (Person o : base.roster()) {
      if (p != o) p.history.incBond(o, 0.2f);
    }
    world.regionFor(Regions.SECTOR03).setAttached(base, true);
    
    for (Person p : base.roster()) {
      world.setInside(p, true);
      base.setAttached(p, true);
    }
    
    final PlaceType buildTechs[] = {
      Facilities.BUSINESS_PARK,
      Facilities.CHEMICAL_PLANT,
      Facilities.STEEL_MILL,
      Facilities.UNION_OFFICE,
      Facilities.TECH_STARTUP,
      Facilities.CITY_PARK,
      Facilities.COMMUNITY_COLLEGE,
      Facilities.SOUP_KITCHEN
    };
    for (Object t : buildTechs) base.addTech(t);
    
    for (Region r : world.regions()) {
      for (Place built : r.buildSlots()) if (built != null) {
        built.setOwner(base);
      }
    }
    
    final Ability learnTechs[] = Techniques.PHYS_TECHNIQUES;
    for (Object t : learnTechs) base.addTech(t);
    
    final ItemType craftTechs[] = {
      Gadgets.WING_BLADES,
      Gadgets.REVOLVER,
      Gadgets.BODY_ARMOUR,
      Gadgets.KEVLAR_VEST,
      Gadgets.MED_KIT,
      Gadgets.SONIC_PROBE
    };
    for (ItemType t : craftTechs) {
      base.addTech(t);
      base.stocks.incStock(t, 2);
    }
    
    base.finance.setSecretPercent(5);
    base.finance.incPublicFunds(2000);
    base.finance.incSecretFunds(200);
    base.finance.updateFinance();
    world.addBase(base, true);
  }
  
  
  public static void initDefaultCrime(World world) {
    
    final Kind goonTypes[] = {
      Crooks.BRUISER, Crooks.BRUISER, Crooks.GANGSTER
    };
    final Kind seniorTypes[] = {
      Crooks.GANGSTER, Civilians.DOCTOR, Civilians.INVENTOR, Civilians.BROKER
    };
    
    final Batch <Base> hideouts = new Batch();
    final Person boss1 = new Person(Villains.MORETTI, world);
    final Base base1 = new Base(Facilities.HIDEOUT, world, true);
    world.regionFor(Regions.SECTOR04).setAttached(base1, true);
    base1.setLeader(boss1);
    base1.finance.setSecretPercent(0);
    base1.finance.incPublicFunds(100);
    hideouts.add(base1);
    
    /*
    final Person boss2 = new Person(Villains.SNAKE_EYES, world);
    final Base base2 = new Base(Facilities.HIDEOUT, world, true);
    world.regionFor(Regions.SECTOR07).setAttached(base2, true);
    base2.setLeader(boss2);
    hideouts.add(base2);
    //*/
    
    for (Base base : hideouts) {
      world.addBase(base, false);
      base.addToRoster(base.leader());
      base.setGoonTypes(goonTypes);
      base.plans.assignStepTypes(StepTypes.ALL_TYPES);
      
      for (Kind type : seniorTypes) {
        Person senior = Person.randomOfKind(type, world);
        base.addToRoster(senior);
      }
      
      for (Person p : base.roster()) {
        world.setInside(p, true);
        base.setAttached(p, true);
        I.say("Location of "+p+" is "+p.place());
      }
    }
  }
}




