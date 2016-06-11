

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class Person implements Session.Saveable {
  
  
  /**  Data fields and construction-
    */
  final public static int
    SLOT_WEAPON     = 0,
    SLOT_ARMOUR     = 1,
    SLOT_ITEMS      = 2,
    NUM_EQUIP_SLOTS = 3,
    ALL_SLOTS[] = { 0, 1, 2 };
  final public static String
    SLOT_NAMES[] = { "Weapon", "Armour", "Items" };
  public static enum Side {
    HEROES, CIVILIANS, VILLAINS
  };
  
  
  Kind kind;
  World world;
  Side side;
  
  String name;
  
  final public PersonHealth  health  = new PersonHealth (this);
  final public PersonStats   stats   = new PersonStats  (this);
  final public PersonBonds   bonds   = new PersonBonds  (this);
  final public PersonHistory history = new PersonHistory(this);
  
  
  Equipped equipSlots[] = new Equipped[NUM_EQUIP_SLOTS];
  
  Assignment assignment;
  float posX, posY;
  boolean turnDone;
  Object lastTarget;
  
  
  
  public Person(Kind kind, World world, String name) {
    this.kind  = kind;
    this.world = world;
    this.name  = name;
    
    history.setSummary(kind.defaultInfo());
    
    for (int n = 0; n < kind.baseEquipped().length; n++) {
      equipItem(kind.baseEquipped()[n], null);
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
    stats  .loadState(s);
    bonds  .loadState(s);
    history.loadState(s);
    
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      equipSlots[i] = (Equipped) s.loadObject();
    }
    
    assignment = (Assignment) s.loadObject();
    posX       = s.loadFloat();
    posY       = s.loadFloat();
    turnDone   = s.loadBool();
    lastTarget = s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind );
    s.saveObject(world);
    s.saveEnum  (side );
    s.saveString(name );
    
    health .saveState(s);
    stats  .saveState(s);
    bonds  .saveState(s);
    history.saveState(s);
    
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      s.saveObject(equipSlots[i]);
    }
    
    s.saveObject(assignment);
    s.saveFloat (posX      );
    s.saveFloat (posY      );
    s.saveBool  (turnDone  );
    s.saveObject(lastTarget);
  }
  
  
  
  /**  General state queries-
    */
  public Kind kind() {
    return kind;
  }
  
  
  public Side side() {
    return side;
  }
  
  
  
  /**  Assigning equipment loadout-
    */
  public void equipItem(Equipped item, Base from) {
    Equipped oldItem = equipSlots[item.slotID];
    equipSlots[item.slotID] = item;
    stats.toggleItemAbilities(oldItem, false);
    stats.toggleItemAbilities(item   , true );
    
    if (oldItem != null && from != null) from.stocks.incStock(oldItem,  1);
    if (item    != null && from != null) from.stocks.incStock(item   , -1);
  }
  
  
  public int equipBonus(int slotID, int properties) {
    Equipped item = equipSlots[slotID];
    if (item == null || ! item.hasProperty(properties)) return 0;
    return item.bonus;
  }
  
  
  public Equipped equippedInSlot(int slotID) {
    return equipSlots[slotID];
  }
  
  
  public boolean hasEquipped(int slotID) {
    return equipSlots[slotID] != null;
  }
  
  
  public boolean hasEquipped(Equipped item) {
    for (Equipped i : equipSlots) if (i == item) return true;
    return false;
  }
  
  
  public boolean canEquip(Equipped item) {
    for (int slotID : ALL_SLOTS) {
      if (item.slotID == slotID && equipSlots[slotID] == null) return true;
    }
    return false;
  }
  
  
  public Equipped currentWeapon() {
    Equipped weapon = equippedInSlot(SLOT_WEAPON);
    return weapon == null ? Common.UNARMED : weapon;
  }
  
  
  public Equipped currentArmour() {
    Equipped armour = equippedInSlot(SLOT_ARMOUR);
    return armour == null ? Common.UNARMOURED : armour;
  }
  
  
  public Series <Equipped> equipment() {
    Batch <Equipped> all = new Batch();
    for (Equipped e : equipSlots) if (e != null) all.add(e);
    return all;
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
  
  
  public boolean retreating() {
    return health.isDoing(PersonHealth.STATE_RETREAT);
  }
  
  
  public boolean captive() {
    return false;
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
    if (! health.alive     ()) return "Dead"       ;
    if (! health.conscious ()) return "Unconscious";
    if (  captive   ()) return "Captive"    ;
    if (  retreating()) return "Retreating" ;
    
    float confidence = health.confidence(), wariness = health.wariness();
    String moraleDesc = "Determined", alertDesc = "Alert";
    if (confidence < 1.66f) moraleDesc = "Steady"  ;
    if (confidence < 1.33f) moraleDesc = "Shaken"  ;
    if (wariness   < 0.66f) alertDesc  = "Watchful";
    if (wariness   < 0.33f) alertDesc  = "Unwary"  ;
    return moraleDesc+", "+alertDesc;
  }
}


















