

package proto.game.world;
import proto.common.*;
import proto.content.agents.Heroes;
import proto.content.events.Kidnapping;
import proto.game.person.*;
import proto.util.*;
import proto.view.*;



public class World implements Session.Saveable {
  
  /**  Data fields, construction and save/load methods-
    */
  RunGame game;
  String savePath;
  
  WorldView view = new WorldView(this);
  
  Nation nations[];
  Base base;
  Events events = new Events(this);
  
  int currentTime = 1;
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
    
    nations = (Nation[]) s.loadObjectArray(Nation.class);
    base    = (Base) s.loadObject();
    events.loadState(s);
    
    currentTime = s.loadInt();
    amWatching  = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(nations);
    s.saveObject(base);
    events.saveState(s);
    s.saveInt(currentTime);
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
  
  
  public void reloadFromSave() {
    if (game == null || savePath == null) return;
    game.attemptReload(savePath);
  }
  
  
  public void initDefaultNations() {
    int numN = Region.ALL_REGIONS.length;
    this.nations = new Nation[numN];
    for (int n = 0; n < numN; n++) {
      nations[n] = new Nation(Region.ALL_REGIONS[n]);
    }
    
    events.addType(Kidnapping.TYPE);
  }
  
  
  public void initDefaultBase() {
    this.base = new Base(this);
    
    base.addToRoster(new Person(Heroes.HERO_BATMAN   ));
    base.addToRoster(new Person(Heroes.HERO_ALFRED   ));
    base.addToRoster(new Person(Heroes.HERO_SWARM    ));
    base.addToRoster(new Person(Heroes.HERO_BATGIRL  ));
    base.addToRoster(new Person(Heroes.HERO_NIGHTWING));
    base.addToRoster(new Person(Heroes.HERO_QUESTION ));
    
    base.addFacility(Blueprint.INFIRMARY    , 0, 1f);
    base.addFacility(Blueprint.TRAINING_ROOM, 1, 1f);
    base.addFacility(Blueprint.GENERATOR    , 2, 1f);
    base.addFacility(Blueprint.ARBORETUM    , 3, 1f);
    
    base.updateBase(0);
    base.currentFunds = 500;
  }
  
  
  
  /**  General query methods-
    */
  public Nation[] nations() {
    return nations;
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
  
  
  public int currentTime() {
    return this.currentTime;
  }
  
  

  /**  Regular updates and activity cycle:
    */
  public void updateWorld() {
    
    if (amWatching) {
      events.updateEvents();
      if (events.active.size() > 0) amWatching = false;
      currentTime += 1;
    }
    /*
    if (enteredScene != null) {
      enteredScene.updateScene();
    }
    else if (amWatching) {
      for (Nation n : nations) {
        
        if (Rand.num() < n.crime && n.mission == null) {
          final Scene s = n.generateCrisis(this);
          n.mission = s;
          amWatching = false;
        }
        
        if (n.mission != null && n.mission.expireTime() <= currentTime) {
          n.mission.resolveAsIgnored();
          n.mission = null;
        }
      }
      base.updateBase(1);
      currentTime += 1;
    }
    //*/
  }
  
  
  public void beginMonitoring() {
    this.amWatching = true;
  }
  
  
  public void stopMonitoring() {
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
  public WorldView view() {
    return view;
  }
}






