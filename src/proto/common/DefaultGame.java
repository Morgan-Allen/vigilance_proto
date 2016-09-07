


package proto.common;
import proto.content.agents.*;
import proto.content.items.*;
import proto.content.rooms.*;
import proto.content.scenes.*;
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
    initDefaultNations(world);
    initDefaultBase(world);
    initDefaultCrime(world);
    return world;
  }
  
  
  public static void initDefaultNations(World world) {
    int numN = Regions.ALL_REGIONS.length;
    
    District[] districts = new District[numN];
    for (int n = 0; n < numN; n++) {
      districts[n] = new District(Regions.ALL_REGIONS[n], world);
    }
    for (District d : districts) d.initialiseDistrict();
    world.attachDistricts(districts);
  }
  
  
  public static void initDefaultBase(World world) {
    final Base base = new Base(world, "Wayne Foundation");
    
    Person leader = base.addToRoster(new Person(Heroes.HERO_BATMAN, world));
    base.addToRoster(new Person(Heroes.HERO_ALFRED   , world));
    base.addToRoster(new Person(Heroes.HERO_SWARM    , world));
    base.addToRoster(new Person(Heroes.HERO_BATGIRL  , world));
    base.addToRoster(new Person(Heroes.HERO_NIGHTWING, world));
    base.addToRoster(new Person(Heroes.HERO_QUESTION , world));
    
    for (Person p : base.roster()) for (Person o : base.roster()) {
      if (p != o) p.history.incBond(o, 0.2f);
    }
    base.setLeader(leader);
    
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
    final Events events = world.events();
    //events.addType(Kidnapping.TYPE);
    //events.addType(Robbery   .TYPE);
    //events.addType(Murder    .TYPE);
    
    //  TODO:  Generate an initial set of crooks (low-level hoods, lieutenants
    //  and bosses.)  And these all need unique characteristics.
    
    Batch <Person> bosses  = new Batch();
    Batch <Person> seniors = new Batch();
    Batch <Person> hoods   = new Batch();
    
    int numSeniors = 3, numHoods = 8;
    
    final Person
      falcone = new Person(Crooks.FALCONE , world),
      twoFace = new Person(Crooks.TWO_FACE, world)
    ;
    bosses.add(falcone);
    bosses.add(twoFace);
    
    //  TODO:  Actually, don't.  Have each boss control a certain number of
    //  assigned territories, and generate subordinates for each region.
    
    for (Person boss : bosses) {
      for (int n = numSeniors; n-- > 0;) {
        Person senior = Crooks.randomOfKind(Crooks.MOBSTER, world);
        seniors.add(senior);
      }
      for (int n = numHoods; n-- > 0;) {
        Person hood = Crooks.randomOfKind(Crooks.GOON, world);
        hoods.add(hood);
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







