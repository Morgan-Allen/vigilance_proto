

package proto.game.world;
import proto.common.*;
import proto.game.person.Person;
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
  Base played;
  List <Base> bases = new List();
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
  
  
  
  /**  Supplementary setup methods:
    */
  public void attachRegions(Region... districts) {
    this.regions = districts;
  }
  
  
  public void addBase(Base base, boolean played) {
    bases.add(base);
    if (played) this.played = base;
  }
  
  
  public void setInside(Element e, boolean is) {
    elements.toggleMember(e, is);
  }
  
  
  
  /**  General query methods-
    */
  public Series <Element> inside() {
    return elements;
  }
  
  
  public Base playerBase() {
    return played;
  }
  
  
  public Base baseFor(Faction faction) {
    for (Base b : bases) if (b.faction == faction) return b;
    return null;
  }
  
  
  public Series <Base> bases() {
    return bases;
  }
  
  
  
  /**  Handling regions and large-scale distances-
    */
  public Region[] regions() {
    return regions;
  }
  
  
  public Region regionFor(RegionType r) {
    for (Region d : regions) if (d.kind() == r) return d;
    return null;
  }
  
  
  public float distanceBetween(Region a, Region b) {
    RegionType ka = a.kind(), kb = b.kind();
    return Nums.max(Nums.abs(ka.mapX - kb.mapX), Nums.abs(ka.mapY - kb.mapY));
  }
  
  
  public Series <Region> regionsInRange(Region a, float range) {
    Batch <Region> matches = new Batch();
    for (Region r : regions) if (distanceBetween(r, a) <= range) {
      matches.add(r);
    }
    return matches;
  }
  
  
  public Series <Person> civilians() {
    Batch <Person> all = new Batch();
    for (Element e : inside()) if (e.isPerson()) {
      Person p = (Person) e;
      if (p.isCivilian()) all.add(p);
    }
    return all;
  }
  
  
  
  /**  Regular updates and activity cycle:
    */
  public void updateWorld(float hoursGone) {
    timing.updateTiming(hoursGone);
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
  }
  
  
  public void exitFromScene(Scene mission) {
    if (this.activeScene != mission) I.complain(mission+" is not active!");
    I.say("EXITING SCENE: "+activeScene);
    this.activeScene = null;
  }
  
  
  
  /**  Graphical/display routines:
    */
  public MainView view() {
    return view;
  }
}






