

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;



public class Person implements Session.Saveable {
  
  
  /**  Data fields and construction-
    */
  public static enum Side {
    HEROES, CIVILIANS, VILLAINS
  };
  
  
  Kind kind;
  World world;
  Side side;
  
  String name;
  
  final public PersonHealth  health  = new PersonHealth (this);
  final public PersonActions actions = new PersonActions(this);
  final public PersonStats   stats   = new PersonStats  (this);
  final public PersonGear    gear    = new PersonGear   (this);
  final public PersonBonds   bonds   = new PersonBonds  (this);
  final public PersonHistory history = new PersonHistory(this);
  
  Assignment assignment;
  
  Tile location;
  Vec3D exactPos = new Vec3D();
  
  
  
  public Person(Kind kind, World world, String name) {
    this.kind  = kind;
    this.world = world;
    this.name  = name;
    
    history.setSummary(kind.defaultInfo());
    
    for (int n = 0; n < kind.baseEquipped().length; n++) {
      gear.equipItem(kind.baseEquipped()[n], null);
    }
    
    if      (kind.type() == Kind.TYPE_HERO    ) side = Side.HEROES   ;
    else if (kind.type() == Kind.TYPE_CIVILIAN) side = Side.CIVILIANS;
    else side = Side.VILLAINS;
    
    stats.initStats();
  }
  
  
  public Person(Kind kind, World world) {
    this(kind, world, kind.name());
  }
  
  
  public Person(Session s) throws Exception {
    s.cacheInstance(this);
    
    kind  = (Kind ) s.loadObject();
    world = (World) s.loadObject();
    side  = (Side ) s.loadEnum(Side.values());
    name  = s.loadString();
    
    health .loadState(s);
    actions.loadState(s);
    stats  .loadState(s);
    gear   .loadState(s);
    bonds  .loadState(s);
    history.loadState(s);
    
    assignment = (Assignment) s.loadObject();
    
    location = (Tile) s.loadObject();
    exactPos.loadFrom(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind );
    s.saveObject(world);
    s.saveEnum  (side );
    s.saveString(name );
    
    health .saveState(s);
    actions.saveState(s);
    stats  .saveState(s);
    gear   .saveState(s);
    bonds  .saveState(s);
    history.saveState(s);
    
    s.saveObject(assignment);
    
    s.saveObject(location);
    exactPos.saveTo(s.output());
  }
  
  
  
  /**  General state queries-
    */
  public Kind kind() {
    return kind;
  }
  
  
  public Side side() {
    return side;
  }
  
  
  
  
  /**  Assigning jobs & missions-
    */
  public boolean freeForAssignment() {
    if (! health.conscious()) return false;
    return assignment == null;
  }
  
  
  public Assignment assignment() {
    return assignment;
  }
  
  
  public boolean canAssignTo(Assignment a) {
    for (Ability t : stats.abilities) {
      if (! t.allowsAssignment(this, a)) return false;
    }
    return true;
  }
  
  
  public void setAssignment(Assignment assigned) {
    this.assignment = assigned;
  }
  
  
  public Scene currentScene() {
    if (assignment instanceof Scene) return (Scene) assignment;
    return null;
  }
  
  
  
  
  
  /**  Scene-specific methods and accessors-
    */
  public void setExactPosition(Scene scene, float x, float y, float z) {
    this.location = scene.tileAt((int) x, (int) y);
    this.exactPos.set(x, y, z);
    
    I.say(this+" entered scene, location: "+location);
  }
  
  
  public Tile currentTile() {
    return location;
  }
  
  
  public Vec3D exactPosition() {
    return exactPos;
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  public void updateOnBase(float numWeeks) {
    if (assignment != null && assignment.complete()) {
      assignment = null;
    }
    
    health.updateHealth(numWeeks);
    if (health.alive()) {
      stats.updateStats();
    }
  }
  
  
  
  /**  Rudimentary AI methods-
    */
  public boolean isHero() {
    return side == Side.HEROES && ! isCivilian();
  }
  
  
  public boolean isCriminal() {
    return side == Side.VILLAINS && ! isCivilian();
  }
  
  
  public boolean isCivilian() {
    return kind.type() == Kind.TYPE_CIVILIAN;
  }
  
  
  public boolean isEnemy(Person other) {
    if (isHero    ()) return other.isCriminal();
    if (isCriminal()) return other.isHero    ();
    return false;
  }
  
  
  public boolean isAlly(Person other) {
    if (isHero    ()) return other.isHero    ();
    if (isCriminal()) return other.isCriminal();
    return false;
  }
  
  
  public boolean isNeutral(Person other) {
    return ! (isEnemy(other) || isAlly(other));
  }
  
  
  public boolean isPlayerOwned() {
    return side == Side.HEROES;
  }
  
  
  
  
  /**  Interface, rendering and debug methods-
    */
  public String name() {
    return name;
  }
  
  
  public String toString() {
    return name;
  }
  
  
  public String confidenceDescription() {
    if (! health.alive      ()) return "Dead"       ;
    if (! health.conscious  ()) return "Unconscious";
    if (  actions.captive   ()) return "Captive"    ;
    if (  actions.retreating()) return "Retreating" ;
    
    float confidence = actions.confidence(), wariness = actions.wariness();
    String moraleDesc = "Determined", alertDesc = "Alert";
    if (confidence < 1.66f) moraleDesc = "Steady"  ;
    if (confidence < 1.33f) moraleDesc = "Shaken"  ;
    if (wariness   < 0.66f) alertDesc  = "Watchful";
    if (wariness   < 0.33f) alertDesc  = "Unwary"  ;
    return moraleDesc+", "+alertDesc;
  }
}


















