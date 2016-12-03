

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.*;
import proto.view.common.MainView;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.items.*;
import proto.content.places.*;
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
  
  Region regions[];
  Base played;
  List <Base> bases = new List();
  List <Element> elements = new List();
  
  final public Events events = new Events(this);
  int timeDays = 0;
  float timeHours = 0;
  boolean amWatching = false;
  
  Scene activeScene = null;
  
  
  public World() {
  }
  
  
  public World(RunGame game, String savePath) {
    attachToGame(game, savePath);
  }
  
  
  public void attachToGame(RunGame game, String savePath) {
    this.game     = game;
    this.savePath = savePath;
  }
  
  
  public World(Session s) throws Exception {
    s.cacheInstance(this);
    
    regions = (Region[]) s.loadObjectArray(Region.class);
    played    = (Base) s.loadObject();
    s.loadObjects(bases);
    events.loadState(s);
    
    timeDays   = s.loadInt  ();
    timeHours  = s.loadFloat();
    amWatching = s.loadBool ();
    
    activeScene = (Scene) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(regions);
    
    s.saveObject(played);
    s.saveObjects(bases);
    events.saveState(s);
    
    s.saveInt  (timeDays  );
    s.saveFloat(timeHours );
    s.saveBool (amWatching);
    
    s.saveObject(activeScene);
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
  
  
  
  /**  Supplementary setup methods:
    */
  public void attachDistricts(Region... districts) {
    this.regions = districts;
  }
  
  
  public void addBase(Base base, boolean played) {
    bases.add(base);
    if (played) this.played = base;
    base.updateBase(0);
  }
  
  
  public void setInside(Element e, boolean is) {
    elements.toggleMember(e, is);
  }
  
  
  
  /**  General query methods-
    */
  public Region[] districts() {
    return regions;
  }
  
  
  public Series <Element> inside() {
    return elements;
  }
  
  
  public Region regionFor(RegionType r) {
    for (Region d : regions) if (d.kind() == r) return d;
    return null;
  }
  
  
  public Base playerBase() {
    return played;
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
    
    if (activeScene != null) {
      activeScene.updateScene();
    }
    else if (amWatching) {
      final float realGap = 1f / RunGame.FRAME_RATE;
      final float timeGap = realGap * GAME_HOURS_PER_REAL_SECOND;
      timeHours += timeGap;
      
      while (timeHours > HOURS_PER_DAY) {
        timeDays++;
        timeHours -= HOURS_PER_DAY;
        for (Region d : regions) {
          d.updateRegion();
        }
        for (Base base : bases) {
          base.updateBaseDaily();
        }
      }
      
      events.updateEvents();
      for (Base base : bases) {
        base.updateBase(timeGap / (HOURS_PER_DAY * DAYS_PER_WEEK));
      }
    }
  }
  
  
  public void beginMonitoring() {
    this.amWatching = true;
  }
  
  
  public void pauseMonitoring() {
    this.amWatching = false;
  }
  
  
  public Scene activeScene() {
    return activeScene;
  }
  
  
  public void enterScene(Scene mission) {
    this.activeScene = mission;
  }
  
  
  public void exitFromScene(Scene mission) {
    if (this.activeScene != mission) I.complain(mission+" is not active!");
    this.activeScene = null;
  }
  
  
  
  /**  Graphical/display routines:
    */
  public MainView view() {
    return view;
  }
}






