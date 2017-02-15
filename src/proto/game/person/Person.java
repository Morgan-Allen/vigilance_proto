

package proto.game.person;
import java.awt.Graphics2D;

import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.common.Surface;
import proto.view.scene.SceneView;



public class Person extends Element {
  
  
  /**  Data fields and construction-
    */
  public static enum Side {
    HEROES, CIVILIANS, VILLAINS
  };
  
  
  Side side;
  String name;
  
  final public PersonMind    mind    = new PersonMind   (this);
  final public PersonHistory history = new PersonHistory(this);
  final public PersonActions actions = new PersonActions(this);
  final public PersonStats   stats   = new PersonStats  (this);
  final public PersonHealth  health  = new PersonHealth (this);
  final public PersonGear    gear    = new PersonGear   (this);
  
  Base base;
  Place resides;
  Scene scene;
  
  List <Assignment> assignments = new List <Assignment> () {
    protected float queuePriority(Assignment r) {
      return r.assignmentPriority();
    }
  };
  
  Tile location;
  Vec3D exactPos = new Vec3D();
  
  
  public Person(Kind kind, World world, String name) {
    super(kind, world);
    this.name  = name;
    
    history.setSummary(kind.defaultInfo());
    
    for (int n = 0; n < kind.baseEquipped().length; n++) {
      final ItemType type = kind.baseEquipped()[n];
      final int freeSlotID = gear.nextFreeSlotID(type.slotType);
      gear.equipItem(type, freeSlotID);
    }
    
    if      (kind.subtype() == Kind.SUBTYPE_HERO    ) side = Side.HEROES   ;
    else if (kind.subtype() == Kind.SUBTYPE_CIVILIAN) side = Side.CIVILIANS;
    else side = Side.VILLAINS;
    
    stats.initStats();
  }
  
  
  public Person(PersonType kind, World world) {
    this(kind, world, kind.name());
  }
  
  
  public Person(Session s) throws Exception {
    super(s);
    
    side  = (Side ) s.loadEnum(Side.values());
    name  = s.loadString();
    
    mind   .loadState(s);
    history.loadState(s);
    actions.loadState(s);
    stats  .loadState(s);
    health .loadState(s);
    gear   .loadState(s);
    
    base    = (Base ) s.loadObject();
    resides = (Place) s.loadObject();
    scene   = (Scene) s.loadObject();
    s.loadObjects(assignments);
    
    location = (Tile) s.loadObject();
    exactPos.loadFrom(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    
    s.saveEnum  (side);
    s.saveString(name);
    
    mind   .saveState(s);
    history.saveState(s);
    actions.saveState(s);
    stats  .saveState(s);
    health .saveState(s);
    gear   .saveState(s);
    
    s.saveObject(base   );
    s.saveObject(resides);
    s.saveObject(scene  );
    s.saveObjects(assignments);
    
    s.saveObject(location);
    exactPos.saveTo(s.output());
  }
  
  
  public static Person randomOfKind(PersonType kind, World world) {
    final String
      firsts[] = kind.firstNames(), lasts[] = kind.lastNames(),
      name = Rand.pickFrom(firsts)+" "+Rand.pickFrom(lasts);
    Person person = new Person(kind, world, name);
    return person;
  }
  
  
  
  /**  General state queries-
    */
  public PersonType kind() {
    return (PersonType) kind;
  }
  
  
  public Side side() {
    return side;
  }
  
  
  
  /**  Assigning jobs-
    */
  public Assignment topAssignment() {
    return assignments.first();
  }
  
  
  public Series <Assignment> assignments() {
    return assignments;
  }
  
  
  public boolean canAssignTo(Assignment a) {
    for (Ability t : stats.abilities) {
      if (! t.allowsAssignment(this, a)) return false;
    }
    return true;
  }
  
  
  public boolean addAssignment(Assignment assigned) {
    if (assignments.includes(assigned)) return false;
    
    final int priority = assigned.assignmentPriority();
    for (Assignment a : assignments) if (a.assignmentPriority() == priority) {
      removeAssignment(a);
    }
    
    assignments.queueAdd(assigned);
    assigned.setAssigned(this, true);
    if (assigned instanceof Scene) scene = (Scene) assigned;
    
    I.say(this+" assigned to "+assigned);
    return true;
  }
  
  
  public boolean removeAssignment(Assignment assigned) {
    if (! assignments.includes(assigned)) return false;
    
    assignments.remove(assigned);
    assigned.setAssigned(this, false);
    if (scene == assigned) scene = null;
    
    I.say(this+" removed from assignment "+assigned);
    return true;
  }
  
  
  public Scene currentScene() {
    return scene;
  }
  
  
  
  
  /**  Setting base and residence:
    */
  public void setBase(Base base) {
    this.base = base;
  }
  
  
  public void setResidence(Place resides) {
    this.resides = resides;
  }
  
  
  public Base base() {
    return base;
  }
  
  
  public Place resides() {
    return resides;
  }
  
  
  
  /**  Scene-specific methods and accessors-
    */
  public void setExactPosition(Scene scene, float x, float y, float z) {
    final Tile old = this.location;
    
    location = scene == null ? null : scene.tileAt(
      (int) (x + 0.5f), (int) (y + 0.5f)
    );
    exactPos.set(x, y, z);
    
    if (old != location) {
      if (old      != null) old     .setInside(this, false);
      if (location != null) location.setInside(this, true );
    }
  }
  
  
  public Tile currentTile() {
    return location;
  }
  
  
  public Vec3D exactPosition() {
    return exactPos;
  }
  
  
  public int blockLevel() {
    return health.conscious() ? Kind.BLOCK_FULL : Kind.BLOCK_PARTIAL;
  }
  
  
  public boolean blockSight() {
    return false;
  }
  
  
  public void updateInScene(boolean duringOwnTurn) {
    if (duringOwnTurn) {
      actions.updateDuringTurn();
      stats.updateStats();
    }
    else {
      stats.updateStats();
    }
  }
  
  
  public void onTurnStart() {
    actions.onTurnStart();
    stats  .onTurnStart();
  }
  
  
  public void onTurnEnd() {
    actions.onTurnEnd();
    stats  .onTurnEnd();
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  public void updateOnBase() {
    for (Assignment a : assignments) {
      if (a.complete()) { removeAssignment(a); continue; }
    }
    
    health.updateHealth();
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
    return kind.type() == Kind.SUBTYPE_CIVILIAN;
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
  
  
  public Access accessLevel(Base base) {
    if (! isHero()) return Access.POSSIBLE;
    return base == this.base ? Access.GRANTED : Access.SECRET;
  }
  
  
  
  /**  Interface, rendering and debug methods-
    */
  public String name() {
    if (isHero()) return name;
    return name+" ("+kind.name()+")";
  }
  
  
  public String confidenceDescription() {
    if (! health .alive     ()) return "Dead"       ;
    if (! health .conscious ()) return "Unconscious";
    if (  actions.captive   ()) return "Captive"    ;
    if (  mind   .retreating()) return "Retreating" ;
    
    float confidence = mind.confidence(), wariness = mind.wariness();
    String moraleDesc = "Determined", alertDesc = "Alert";
    if (confidence < 1.66f) moraleDesc = "Steady"  ;
    if (confidence < 1.33f) moraleDesc = "Shaken"  ;
    if (wariness   < 0.66f) alertDesc  = "Watchful";
    if (wariness   < 0.33f) alertDesc  = "Unwary"  ;
    return moraleDesc+", "+alertDesc;
  }
  
  
  public void renderTo(Scene scene, SceneView view, Surface s, Graphics2D g) {
    Vec3D pos = exactPosition();
    view.renderSprite(pos.x + 0, pos.y + 0, 1, 1, 0, kind().sprite(), g);
  }
}




















