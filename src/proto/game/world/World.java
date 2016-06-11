

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.items.*;
import proto.content.rooms.*;



public class World implements Session.Saveable {
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    SECONDS_PER_MINUTE = 60,
    MINUTES_PER_HOUR   = 60,
    HOURS_PER_DAY      = 24,
    HOURS_PER_SHIFT    = 8,
    SHIFTS_PER_DAY     = 3,
    DAYS_PER_WEEK      = 7,
    WEEKS_PER_YEAR     = 52,
    
    GAME_HOURS_PER_REAL_SECOND = 8
  ;
  
  RunGame game;
  String savePath;
  
  MainView view = new MainView(this);
  
  District districts[];
  Base base;
  Events events = new Events(this);
  
  
  int timeDays = 0;
  float timeHours = 0;
  boolean amWatching = false;
  
  
  
  public World(RunGame game, String savePath) {
    attachToGame(game, savePath);
  }
  
  
  public void attachToGame(RunGame game, String savePath) {
    this.game     = game;
    this.savePath = savePath;
  }
  
  
  public World(Session s) throws Exception {
    s.cacheInstance(this);
    
    districts = (District[]) s.loadObjectArray(District.class);
    base    = (Base) s.loadObject();
    events.loadState(s);
    
    timeDays  = s.loadInt  ();
    timeHours = s.loadFloat();
    amWatching = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(districts);
    s.saveObject(base);
    events.saveState(s);
    s.saveInt  (timeDays );
    s.saveFloat(timeHours);
    s.saveBool(amWatching);
  }
  
  
  public RunGame game() {
    return game;
  }
  
  
  public void performSave() {
    try {
      Session.saveSession(savePath, this);
      I.say("Saving complete...");
    }
    catch (Exception e) { I.say("Could not save world!"); I.report(e); }
  }
  
  
  public void performSaveAndQuit() {
    performSave();
    System.exit(0);
  }
  
  
  public void reloadFromSave() {
    if (game == null || savePath == null) return;
    game.attemptReload(savePath);
  }
  
  
  public void initDefaultNations() {
    int numN = Region.ALL_REGIONS.length;
    this.districts = new District[numN];
    
    for (int n = 0; n < numN; n++) {
      districts[n] = new District(Region.ALL_REGIONS[n], this);
    }
    events.addType(Kidnapping.TYPE);
    events.addType(Robbery   .TYPE);
    events.addType(Murder    .TYPE);
    
    for (District d : districts) d.initialiseDistrict();
  }
  
  
  public void initDefaultBase() {
    this.base = new Base(this, "Wayne Foundation");
    
    Person leader = base.addToRoster(new Person(Heroes.HERO_BATMAN, this));
    base.addToRoster(new Person(Heroes.HERO_ALFRED   , this));
    base.addToRoster(new Person(Heroes.HERO_SWARM    , this));
    base.addToRoster(new Person(Heroes.HERO_BATGIRL  , this));
    base.addToRoster(new Person(Heroes.HERO_NIGHTWING, this));
    base.addToRoster(new Person(Heroes.HERO_QUESTION , this));
    
    for (Person p : base.roster()) for (Person o : base.roster()) {
      if (p != o) p.bonds.incBond(o, 0.2f);
    }
    base.setLeader(leader);
    
    base.addFacility(Gymnasium .BLUEPRINT, 0, 1f);
    base.addFacility(Library   .BLUEPRINT, 1, 1f);
    base.addFacility(Workshop  .BLUEPRINT, 2, 1f);
    base.addFacility(Laboratory.BLUEPRINT, 3, 1f);
    
    base.stocks.incStock(Gadgets.BATARANGS  , 5);
    base.stocks.incStock(Gadgets.BODY_ARMOUR, 2);
    
    base.setIncomeFloor(20);
    base.incFunding(500);
    base.updateBase(0);
  }
  
  
  
  /**  General query methods-
    */
  public District[] nations() {
    return districts;
  }
  
  
  public District nationFor(Region r) {
    for (District n : districts) if (n.region == r) return n;
    return null;
  }
  
  
  public Base base() {
    return base;
  }
  
  
  public Events events() {
    return events;
  }
  
  
  public boolean monitorActive() {
    return amWatching;
  }
  
  
  public int timeDays() {
    return timeDays;
  }
  
  
  public int timeHours() {
    return (int) timeHours;
  }
  
  
  public int timeMinutes() {
    return (int) ((timeHours % 1) * MINUTES_PER_HOUR);
  }
  
  
  public int totalMinutes() {
    return (timeDays * 24 * 60) + (int) (timeHours * 60);
  }
  
  

  /**  Regular updates and activity cycle:
    */
  public void updateWorld() {

    /*
    if (enteredScene != null) {
      enteredScene.updateScene();
    }
    //*/
    if (amWatching) {
      final float realGap = 1f / RunGame.FRAME_RATE;
      final float timeGap = realGap * GAME_HOURS_PER_REAL_SECOND;
      timeHours += timeGap;
      
      while (timeHours > HOURS_PER_DAY) {
        timeDays++;
        timeHours -= HOURS_PER_DAY;
        for (District d : districts) d.updateDistrict();
        base.updateBaseDaily();
      }
      
      events.updateEvents();
      base.updateBase(timeGap / (HOURS_PER_DAY * DAYS_PER_WEEK));
    }
  }
  
  
  public void beginMonitoring() {
    this.amWatching = true;
  }
  
  
  public void pauseMonitoring() {
    this.amWatching = false;
  }
  
  
  public void beginNextMission() {
    /*
    Scene toEnter = missions().first();
    if (toEnter != null) {
      toEnter.setupScene();
      toEnter.beginScene();
    }
    else {
      base.updateBase(1);
      currentTime += 1;
    }
    this.enteredScene = toEnter;
    //*/
  }
  
  
  
  /**  Graphical/display routines:
    */
  public MainView view() {
    return view;
  }
}






