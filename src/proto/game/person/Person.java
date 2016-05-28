

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class Person implements Session.Saveable {
  
  
  /**  Data fields and construction-
    */
  final public static int
    STATE_INIT    = -1,
    STATE_AS_PC   =  0,
    STATE_UNAWARE =  1,
    STATE_ACTIVE  =  2,
    STATE_RETREAT =  3;
  final public static int
    INIT_LUCK   = 3,
    MAX_LUCK    = 3,
    INIT_STRESS = 0,
    MAX_STRESS  = 100;
  final public static int
    FULL_HEAL_WEEKS   = 8   ,
    WAKEUP_PERCENT    = 50  ,
    WEEK_STRESS_DECAY = 2   ,
    WEEK_TRAINING_XP  = 250 ,
    MIN_LEVEL_XP      = 1000;
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
  
  final public PersonStats   stats   = new PersonStats  (this);
  final public PersonBonds   bonds   = new PersonBonds  (this);
  final public PersonHistory history = new PersonHistory(this);
  
  int luck = INIT_LUCK, stress = INIT_STRESS;
  Equipped equipSlots[] = new Equipped[NUM_EQUIP_SLOTS];
  float injury, stun;
  boolean alive, conscious;
  
  Assignment assignment;
  
  int   AIstate    = STATE_INIT;
  float confidence = 1.0f;
  float wariness   = 0.0f;
  
  float posX, posY;
  int actionPoints;
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
    
    alive = conscious = true;
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
    
    stats    .loadState(s);
    bonds.loadState(s);
    history  .loadState(s);
    
    luck   = s.loadInt();
    stress = s.loadInt();
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      equipSlots[i] = (Equipped) s.loadObject();
    }
    
    injury    = s.loadFloat();
    stun      = s.loadFloat();
    alive     = s.loadBool();
    conscious = s.loadBool();
    
    assignment = (Assignment) s.loadObject();
    AIstate    = s.loadInt();
    confidence = s.loadFloat();
    wariness   = s.loadFloat();
    
    posX         = s.loadFloat();
    posY         = s.loadFloat();
    actionPoints = s.loadInt();
    turnDone     = s.loadBool();
    lastTarget   = s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind );
    s.saveObject(world);
    s.saveEnum  (side );
    s.saveString(name );
    
    stats    .saveState(s);
    bonds.saveState(s);
    history  .saveState(s);
    
    s.saveInt(luck  );
    s.saveInt(stress);
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      s.saveObject(equipSlots[i]);
    }
    
    s.saveFloat(injury   );
    s.saveFloat(stun     );
    s.saveBool (alive    );
    s.saveBool (conscious);
    
    s.saveObject(assignment);
    s.saveInt   (AIstate   );
    s.saveFloat (confidence);
    s.saveFloat (wariness  );
    
    s.saveFloat (posX        );
    s.saveFloat (posY        );
    s.saveInt   (actionPoints);
    s.saveBool  (turnDone    );
    s.saveObject(lastTarget  );
  }
  
  
  
  /**  General state queries-
    */
  public int maxHealth() {
    return stats.levelFor(HIT_POINTS);
  }
  
  
  public int maxStress() {
    return stats.levelFor(WILLPOWER);
  }
  
  
  public float injury() {
    return injury;
  }
  
  
  public float stun() {
    return stun;
  }
  
  
  public float stress() {
    return stress;
  }
  
  
  public float healthLevel() {
    return Nums.clamp(1f - ((injury + stun) / maxHealth()), 0, 1);
  }
  
  
  public float bleedRisk() {
    if (injury <= 0) return 0;
    if (stun   <= 0) return 1;
    return injury / (stun + injury);
  }
  
  
  public int currentAP() {
    return actionPoints;
  }
  
  
  public float confidence() {
    return confidence;
  }
  
  
  public boolean alive() {
    return alive;
  }
  
  
  public boolean breathing() {
    return true;
  }
  
  
  public boolean conscious() {
    return conscious && alive;
  }
  
  
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
    if (! conscious()) return false;
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
  
  
  
  /**  State adjustments-
    */
  public void receiveStun(float stun) {
    this.stun += stun;
    checkState();
    world.events().log(name()+" suffered "+stun+" stun.");
  }
  
  
  public void receiveInjury(float injury) {
    this.injury += injury;
    checkState();
    world.events().log(name()+" suffered "+injury+" injury.");
  }
  
  
  public void liftInjury(float lift) {
    injury = Nums.max(0, injury - lift);
  }
  
  
  public void liftStun(float lift) {
    stun = Nums.max(0, stun - lift);
  }
  
  
  public void liftStress(int lift) {
    stress = Nums.max(0, stress - lift);
  }
  
  
  public void modifyAP(int modifier) {
    actionPoints += modifier;
  }
  
  
  public void setActionPoints(int AP) {
    actionPoints = AP;
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  public void updateOnBase(float numWeeks) {
    if (assignment != null && assignment.complete()) {
      assignment = null;
    }
    
    if (! alive) return;
    if (conscious()) stun = 0;
    
    stats.updateStats();
    
    int maxHealth = maxHealth();
    float regen = maxHealth * numWeeks / FULL_HEAL_WEEKS;
    injury = Nums.max(0, injury - regen);
    
    if (conscious) {
      stress = Nums.max(0, stress - (int) (WEEK_STRESS_DECAY * numWeeks));
    }
    else {
      float wakeUp = maxHealth * 100f / WAKEUP_PERCENT;
      if (injury < wakeUp) conscious = true;
    }
  }
  
  
  void checkState() {
    int maxHealth = maxHealth();
    if (conscious && injury + stun > maxHealth) {
      this.conscious = false;
      world.events().log(name()+" fell unconscious!");
    }
    if (alive && injury > maxHealth * 1.5f) {
      this.alive = false;
      world.events().log(name()+" was killed!");
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
    return AIstate == STATE_RETREAT;
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
    if (! alive     ()) return "Dead"       ;
    if (! conscious ()) return "Unconscious";
    if (  captive   ()) return "Captive"    ;
    if (  retreating()) return "Retreating" ;
    
    String moraleDesc = "Determined", alertDesc = "Alert";
    if (confidence < 1.66f) moraleDesc = "Steady"  ;
    if (confidence < 1.33f) moraleDesc = "Shaken"  ;
    if (wariness   < 0.66f) alertDesc  = "Watchful";
    if (wariness   < 0.33f) alertDesc  = "Unwary"  ;
    return moraleDesc+", "+alertDesc;
  }
}


















