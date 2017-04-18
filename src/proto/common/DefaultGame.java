

package proto.common;
import proto.content.agents.*;
import proto.content.items.*;
import proto.content.places.*;
import proto.content.events.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
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
    initDefaultWorld(world);
    return world;
  }
  
  
  public static void initDefaultWorld(World world) {
    DefaultGame.initDefaultTime   (world);
    DefaultGame.initDefaultRegions(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    DefaultGame.initDefaultBonds  (world);
  }
  
  
  public static void initDefaultTime(World world) {
    world.timing.setStartDate(9, 7, 1984);
  }
  
  
  public static void initDefaultRegions(World world) {
    boolean report = GameSettings.reportWorldInit;
    if (report) I.say("\nInitialising Regions...");
    
    int numN = Regions.ALL_REGIONS.length;
    Region[] regions = new Region[numN];
    for (int n = 0; n < numN; n++) {
      regions[n] = new Region(Regions.ALL_REGIONS[n], world);
    }
    world.attachRegions(regions);
    for (Region region : regions) {
      region.initialiseRegion();
      if (report) {
        I.say("  "+region);
        for (Place p : region.buildSlots()) if (p != null) {
          I.say("    "+p+" residents:");
          for (Person r : p.residents()) {
            I.say("      "+r);
          }
        }
      }
    }
  }
  
  
  public static void initDefaultBase(World world) {
    boolean report = GameSettings.reportWorldInit;
    if (report) I.say("\nInitialising Base...");
    //
    //  TODO:  Move this out to the region-types definitions?
    final Faction city = Civilians.THE_CITY_COUNCIL;
    for (Region r : world.regions()) for (Place b : r.buildSlots()) {
      if (b == null) continue;
      if (b.kind == Civilians.CITY_HALL) {
        world.council.assignCourt(b);
      }
      if (b.kind == Civilians.CITY_JAIL) {
        world.council.assignPrison(b);
      }
    }
    final Base hall = new Base(Civilians.CITY_HALL, world, city);
    world.regionFor(Regions.SECTOR04).setAttached(hall, true);
    world.council.assignCityHall(hall);
    //
    //  
    final Faction owns = Heroes.JANUS_INDUSTRIES;
    final Base base = new Base(Facilities.MANOR, world, owns);
    Person leader = new Person(Heroes.HERO_PHOBOS, world);
    base.setLeader(leader);
    
    Place.setResident(leader, base, true);
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
    
    final ItemType craftTechs[] = {
      Weapons.WING_BLADES,
      Weapons.REVOLVER,
      Armours.BODY_ARMOUR,
      Armours.KEVLAR_VEST,
      Gadgets.BOLAS,
      Gadgets.MED_KIT,
      Gadgets.TEAR_GAS,
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
    
    if (report) {
      I.say("  Roster for "+owns);
      for (Person p : base.roster()) I.say("    "+p);
      I.say("  Known technologies:");
      for (Object o : base.knownTech()) I.say("    "+o);
      I.say("  Current stocks:");
      for (ItemType t : base.stocks.availableItemTypes()) {
        I.say("    "+t+" ("+base.stocks.numStored(t)+")");
      }
    }
  }
  
  
  public static void initDefaultCrime(World world) {
    boolean report = GameSettings.reportWorldInit;
    if (report) I.say("\nInitialising Crime...");
    
    final PersonType goonTypes[] = {
      Crooks.BRUISER, Crooks.BRUISER, Crooks.GANGSTER
    };
    final PersonType seniorTypes[] = {
      Crooks.GANGSTER, Crooks.GANGSTER, Crooks.HITMAN,
      Civilians.DOCTOR, Civilians.INVENTOR, Civilians.BROKER
    };
    
    final Faction owns = Crooks.THE_MADE_MEN;
    final Batch <Base> hideouts = new Batch();
    final Person boss1 = new Person(Villains.MORETTI, world);
    final Base base1 = new Base(Facilities.LOUNGE, world, owns);
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
      base.plots.assignPlotTypes(PlotTypes.ALL_TYPES);
      
      for (PersonType type : seniorTypes) {
        Person senior = Person.randomOfKind(type, world);
        base.addToRoster(senior);
      }
      
      for (Person p : base.roster()) {
        world.setInside(p, true);
        base.setAttached(p, true);
      }
      
      if (report) {
        I.say("  Roster for "+base.faction());
        for (Person p : base.roster()) I.say("    "+p);
      }
    }
  }
  
  
  public static void initDefaultBonds(World world) {
    boolean report = GameSettings.reportWorldInit;
    Series <Person> civilians = world.civilians();
    if (report) I.say("\nInitialising Bonds...");
    //
    //  Establish random positive relationships between civilians in
    //  neighbouring sectors:
    for (Person p : civilians) {
      Batch <Person> neighbours = new Batch();
      for (Person o : civilians) if (o != p) {
        if (world.distanceBetween(p.region(), o.region()) > 1) continue;
        neighbours.add(o);
      }
      
      int numBonds = Nums.min(5, neighbours.size());
      while (numBonds-- > 0) {
        Person o = (Person) Rand.pickFrom(neighbours);
        p.history.incBond(o, 0.33f);
        o.history.incBond(p, 0.33f);
      }
    }
    //
    //  And establish some grudges between crime bosses and prominent citizens-
    for (Base base : world.bases()) {
      if (! base.faction().criminal) continue;
      Person boss = base.leader();
      
      Batch <Person> enemies = new Batch();
      for (Person p : civilians) if (p != boss) {
        if (Visit.arrayIncludes(Civilians.CIVIC_TYPES, p.kind())) {
          enemies.add(p);
        }
      }
      
      int numGrudges = Nums.min(5, enemies.size());
      while (numGrudges-- > 0) {
        Person mark = (Person) Rand.pickFrom(enemies);
        boss.history.setBond(mark, -1 * Rand.num());
        mark.history.setBond(boss, -1 * Rand.num());
      }
    }
    //
    //  
    if (report) {
      for (Person p : civilians) {
        I.say("  Civilian "+p+" has Bonds:");
        for (Element o : p.history.sortedBonds()) {
          I.say("    "+o+" ("+p.history.bondWith(o)+")");
        }
      }
      for (Base b : world.bases()) if (b.faction().criminal) {
        Person p = b.leader();
        I.say("  Boss "+p+" has Grudges:");
        for (Element o : p.history.sortedBonds()) {
          I.say("    "+o+" ("+p.history.bondWith(o)+")");
        }
      }
    }
  }
}






