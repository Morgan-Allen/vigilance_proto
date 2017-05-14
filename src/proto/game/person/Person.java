

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
  
  String name;
  
  final public PersonMind    mind    = new PersonMind   (this);
  final public PersonHistory history = new PersonHistory(this);
  final public PersonActions actions = new PersonActions(this);
  final public PersonStats   stats   = new PersonStats  (this);
  final public PersonHealth  health  = new PersonHealth (this);
  final public PersonGear    gear    = new PersonGear   (this);

  Side side;
  Base base;
  Place resides;
  Scene scene;
  
  List <Assignment> assignments = new List <Assignment> () {
    protected float queuePriority(Assignment r) {
      return r.assignmentPriority();
    }
  };
  boolean captive = false;
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
    
    name = s.loadString();
    
    mind   .loadState(s);
    history.loadState(s);
    actions.loadState(s);
    stats  .loadState(s);
    health .loadState(s);
    gear   .loadState(s);

    side    = (Side ) s.loadEnum(Side .values());
    base    = (Base ) s.loadObject();
    resides = (Place) s.loadObject();
    scene   = (Scene) s.loadObject();
    s.loadObjects(assignments);
    captive  = s.loadBool();
    location = (Tile) s.loadObject();
    exactPos.loadFrom(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    
    s.saveString(name);
    
    mind   .saveState(s);
    history.saveState(s);
    actions.saveState(s);
    stats  .saveState(s);
    health .saveState(s);
    gear   .saveState(s);

    s.saveEnum  (side   );
    s.saveObject(base   );
    s.saveObject(resides);
    s.saveObject(scene  );
    s.saveObjects(assignments);
    s.saveBool  (captive );
    s.saveObject(location);
    exactPos.saveTo(s.output());
  }
  
  
  public static Person randomOfKind(PersonType kind, World world) {
    final String
      firsts[] = kind.firstNames(), lasts[] = kind.lastNames(),
      name = Rand.pickFrom(firsts)+" "+Rand.pickFrom(lasts);
    
    Person person = new Person(kind, world, name);
    person.stats.addTrait((Trait) Rand.pickFrom(Common.BUILD       ));
    person.stats.addTrait((Trait) Rand.pickFrom(Common.RACES       ));
    person.stats.addTrait((Trait) Rand.pickFrom(Common.EYE_COLOURS ));
    person.stats.addTrait((Trait) Rand.pickFrom(Common.HAIR_COLOURS));
    person.stats.addTrait((Trait) Rand.pickFrom(Common.SEX         ));
    
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
    
    ///I.say(this+" assigned to "+assigned);
    return true;
  }
  
  
  public boolean removeAssignment(Assignment assigned) {
    if (! assignments.includes(assigned)) return false;
    
    assignments.remove(assigned);
    assigned.setAssigned(this, false);
    if (scene == assigned) scene = null;
    
    ///I.say(this+" removed from assignment "+assigned);
    return true;
  }
  
  
  public void clearAssignments() {
    for (Assignment a : assignments) removeAssignment(a);
  }
  
  
  public Scene currentScene() {
    return scene;
  }
  
  
  
  
  /**  Setting base, residence and captivity:
    */
  public void setBase(Base base) {
    this.base = base;
  }
  
  
  public void setResidence(Place resides) {
    this.resides = resides;
  }
  
  
  public void setCaptive(boolean captive) {
    this.captive = captive;
  }
  
  
  public Base base() {
    return base;
  }
  
  
  public Place resides() {
    return resides;
  }
  
  
  public boolean isCaptive() {
    return captive;
  }
  
  
  
  /**  Scene-specific methods and accessors-
    */
  public void setExactPosition(Scene scene, float x, float y, float z) {
    final Tile old = this.location;
    location = scene == null ? null : scene.tileAt(x, y);
    exactPos.set(x, y, z);
    
    if (old != location) {
      if (old      != null) old     .setOccupant(this, false);
      if (location != null) location.setOccupant(this, true );
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
  
  
  public void updateInScene(boolean duringOwnTurn, Action doing) {
    if (duringOwnTurn) {
      actions.updateDuringTurn(doing);
      stats.updateStats(0);
    }
    else {
      stats.updateStats(0);
    }
  }
  
  
  public void onTurnStart() {
    actions.onTurnStart();
    health .onTurnStart();
    stats  .onTurnStart();
  }
  
  
  public void onTurnEnd() {
    actions.onTurnEnd();
    health .onTurnEnd();
    stats  .onTurnEnd();
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  public void updateOnBase() {
    for (Assignment a : assignments) {
      if (a.complete()) { removeAssignment(a); continue; }
    }

    float numWeeks = world().timing.hoursInTick();
    health.updateHealth(numWeeks);
    if (health.alive()) {
      stats.updateStats(numWeeks);
    }
  }
  
  
  
  /**  Rudimentary AI methods-
    */
  //  TODO:  Clean these up a bit.
  public boolean isHero() {
    return side == Side.HEROES && ! isCivilian();
  }
  
  
  public boolean isVillain() {
    return isCriminal() && kind().subtype() == Kind.SUBTYPE_BOSS;
  }
  
  
  public boolean isCriminal() {
    if (side == Side.VILLAINS) return true;
    return base != null && base.faction().criminal;
  }
  
  
  public boolean isCivilian() {
    if (isCriminal()) return false;
    return kind.subtype() == Kind.SUBTYPE_CIVILIAN;
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
    if (isHero() || isVillain()) return name;
    return name+" ("+kind.name()+")";
  }
  
  
  public String confidenceDescription() {
    if (! health .alive     ()) return "Dead"       ;
    if (! health .conscious ()) return "Unconscious";
    if (          isCaptive ()) return "Captive"    ;
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
  
  
  protected void log(String message) {
    //  TODO:  Pipe this output to the view/debug window?
    return;
  }
}






