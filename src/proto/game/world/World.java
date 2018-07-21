

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.common.MainView;



public class World implements Session.Saveable {
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    MINUTES_PER_HOUR   = 60,
    HOURS_PER_DAY      = 24,
    MINUTES_PER_DAY    = 60 * 24,
    HOURS_PER_SHIFT    = 8 ,
    SHIFTS_PER_DAY     = 3 ,
    DAYS_PER_WEEK      = 7 ,
    WEEKS_PER_YEAR     = 52
  ;
  
  MainView view = new MainView(this);
  RunGame game;
  String savePath;
  
  Region regions[];
  Table <String, Integer> distCache = new Table();
  
  Base played;
  List <Base> bases = new List();
  
  int nextID = 0;
  List <Element> elements = new List();
  
  final public Timing  timing  = new Timing (this);
  final public Events  events  = new Events (this);
  final public Council council = new Council(this);
  
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
    played  = (Base    ) s.loadObject();
    s.loadObjects(bases);
    nextID = s.loadInt();
    s.loadObjects(elements);
    
    events .loadState(s);
    timing .loadState(s);
    council.loadState(s);
    
    activeScene = (Scene) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveObjectArray(regions);
    s.saveObject(played);
    s.saveObjects(bases);
    s.saveInt(nextID);
    s.saveObjects(elements);
    
    events .saveState(s);
    timing .saveState(s);
    council.saveState(s);
    
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
  
  
  
  /**  Supplementary methods for bases and world-entry:
    */
  public Series <Element> inside() {
    return elements;
  }
  
  
  public void setInside(Element e, boolean is) {
    if (is && e.uniqueID == -1) {
      e.uniqueID = nextID++;
    }
    elements.toggleMember(e, is);
  }
  
  
  public Base playerBase() {
    return played;
  }
  
  
  public void setPlayerBase(Base base) {
    this.played = base;
  }
  
  
  public Base baseFor(Faction faction) {
    for (Base b : bases) if (b.faction == faction) return b;
    Base base = new Base(this, faction);
    bases.add(base);
    return base;
  }
  
  
  public Series <Base> bases() {
    return bases;
  }
  
  
  
  /**  Handling regions and large-scale distances-
    */
  public void attachRegions(Region... districts) {
    this.regions = districts;
  }
  
  
  public Region[] regions() {
    return regions;
  }
  
  
  public Region regionFor(RegionType r) {
    for (Region d : regions) if (d.kind() == r) return d;
    return null;
  }
  
  
  public float distanceBetween(Region a, Region b) {
    
    String key = a.kind().uniqueID()+"_"+b.kind().uniqueID();
    Integer dist = distCache.get(key);
    if (dist != null) return dist;
    
    Table <RegionType, RegionType> used = new Table();
    Batch <RegionType> frontier = new Batch();
    frontier.add(a.kind());
    used.put(a.kind(), a.kind());
    
    dist = 0;
    search: while (! frontier.empty()) {
      Batch nextGen = new Batch();
      for (RegionType f : frontier) {
        if (f == b.kind()) {
          break search;
        }
        for (RegionType near : f.bordering) {
          if (used.containsKey(near)) continue;
          used.put(near, near);
          nextGen.add(near);
        }
      }
      dist += 1;
      frontier = nextGen;
    }
    
    distCache.put(key, dist);
    return dist;
  }
  
  
  public Series <Region> regionsInRange(Region a, float range) {
    Batch <Region> matches = new Batch();
    for (Region r : regions) if (distanceBetween(r, a) <= range) {
      matches.add(r);
    }
    return matches;
  }
  
  
  
  /**  Assorted common batch-query methods:
    */
  public Series <Person> persons() {
    Batch <Person> all = new Batch();
    for (Element e : inside()) if (e.isPerson()) {
      all.add((Person) e);
    }
    return all;
  }
  
  
  public Series <Person> civilians() {
    Batch <Person> all = new Batch();
    for (Element e : inside()) if (e.isPerson()) {
      Person p = (Person) e;
      if (p.isCivilian()) all.add(p);
    }
    return all;
  }
  
  
  public Series <Place> places() {
    Batch <Place> all = new Batch();
    for (Element e : inside()) if (e.isPlace()) {
      all.add((Place) e);
    }
    return all;
  }
  
  
  public Series <Place> publicPlaces() {
    Batch <Place> all = new Batch();
    for (Element e : inside()) if (e.isPlace() && ! e.isHQ()) {
      Place p = (Place) e;
      all.add(p);
    }
    return all;
  }
  
  
  
  /**  Regular updates and activity cycle:
    */
  /*
  public void updateWorldInRealTime(float realSeconds) {
    float hoursGone = RunGame.SLOW_HOURS_PER_REAL_SECOND;
    if (events.active().empty()) hoursGone = RunGame.FAST_HOURS_PER_REAL_SECOND;
    hoursGone *= realSeconds;
    
    Event next = events.nextEvent();
    if (next != null) {
      float startGap = next.timeBegins() - timing.totalHours();
      hoursGone = Nums.max(startGap + 0.5f, hoursGone);
    }
    
    updateWorld(hoursGone);
  }
  //*/
  
  
  public void updateWorld(int numDays) {
    timing.advanceDays(numDays);
    for (Region d : regions) {
      d.updateRegion();
    }
    for (Base base : bases) {
      base.updateBase();
    }
    events .updateEvents ();
    council.updateCouncil();
  }
  
  
  public Scene activeScene() {
    return activeScene;
  }
  
  
  public void enterScene(Scene mission) {
    I.say("ENTERING SCENE: "+mission);
    this.activeScene = mission;
    if (mission != null) mission.beginScene();
  }
  
  
  public void exitFromScene(Scene mission) {
    I.say("EXITING SCENE: "+activeScene);
    this.activeScene = null;
  }
  
  
  
  /**  Graphical/display routines:
    */
  public MainView view() {
    return view;
  }
}



