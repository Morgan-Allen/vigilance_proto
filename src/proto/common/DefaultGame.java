

package proto.common;
import proto.content.agents.*;
import proto.content.items.*;
import proto.content.places.*;
import proto.content.rooms.*;
import proto.content.events.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.EventQueue;



public class DefaultGame extends RunGame {
  
  
  final public static String
    DEFAULT_SAVE_PATH = "saves/main_save.vgl";
  
  
  DefaultGame() {
    super(DEFAULT_SAVE_PATH);
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    initDefaultRegions(world);
    initDefaultBase(world);
    initDefaultCrime(world);
    return world;
  }
  
  
  public static void initDefaultRegions(World world) {
    int numN = Regions.ALL_REGIONS.length;
    
    Region[] regions = new Region[numN];
    for (int n = 0; n < numN; n++) {
      regions[n] = new Region(Regions.ALL_REGIONS[n], world);
    }
    for (Region region : regions) {
      region.initialiseRegion(null);
      region.nudgeCurrentStat(Region.TRUST     , 25);
      region.nudgeCurrentStat(Region.DETERRENCE, 25);
    }
    world.attachDistricts(regions);
  }
  
  
  public static void initDefaultBase(World world) {
    final Base base = new Base(Facilities.WAYNE_MANOR, world);
    
    Person leader = new Person(Heroes.HERO_BATMAN, world);
    base.setLeader(leader);
    base.addToRoster(leader);
    base.addToRoster(new Person(Heroes.HERO_BATGIRL  , world));
    base.addToRoster(new Person(Heroes.HERO_NIGHTWING, world));
    base.addToRoster(new Person(Heroes.HERO_QUESTION , world));
    
    for (Person p : base.roster()) for (Person o : base.roster()) {
      if (p != o) p.history.incBond(o, 0.2f);
    }
    world.regionFor(Regions.OLD_GOTHAM).setAttached(base, true);
    
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
      Facilities.ROBINS_CAMP,
      Facilities.SOUP_KITCHEN
    };
    for (PlaceType t : buildTechs) base.addTech(t.tech);
    
    base.addFacility(Gymnasium .BLUEPRINT, 0, 1f);
    base.addFacility(Library   .BLUEPRINT, 1, 1f);
    base.addFacility(Workshop  .BLUEPRINT, 2, 1f);
    base.addFacility(Laboratory.BLUEPRINT, 3, 1f);
    
    base.stocks.incStock(Gadgets.BATARANGS  , 4);
    base.stocks.incStock(Gadgets.BODY_ARMOUR, 2);
    base.stocks.incStock(Gadgets.MED_KIT    , 2);
    
    base.setIncomeFloor(20);
    base.incFunding(500);
    world.addBase(base, true);
  }
  
  
  public static void initDefaultCrime(World world) {
    
    final Kind goonTypes[] = {
      Crooks.GOON, Crooks.GOON, Crooks.MOBSTER
    };
    final Kind seniorTypes[] = {
      Crooks.MOBSTER, Civilians.DOCTOR, Civilians.INVENTOR, Civilians.BROKER
    };
    
    final Batch <Base> hideouts = new Batch();
    final Person falcone = new Person(Villains.FALCONE, world);
    final Base falconeBase = new Base(Facilities.HIDEOUT, world);
    world.regionFor(Regions.CAPE_FINGER).setAttached(falconeBase, true);
    falconeBase.setLeader(falcone);
    falconeBase.incFunding(100);
    hideouts.add(falconeBase);
    
    /*
    final Person twoFace = new Person(Villains.TWO_FACE, world);
    final Base twoFaceBase = new Base(Facilities.HIDEOUT, world);
    world.regionFor(Regions.MILLER_BAY).setAttached(twoFaceBase, true);
    twoFaceBase.setLeader(twoFace);
    hideouts.add(twoFaceBase);
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
  
  
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        RunGame ex = new DefaultGame();
        ex.setVisible(true);
      }
    });
  }
}


