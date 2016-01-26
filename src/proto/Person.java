

package proto;
import util.*;



public class Person implements Session.Saveable {
  
  
  /**  Data fields and construction-
    */
  static class Stat extends Ability {
    public Stat(String name, String ID, String description) {
      super(name, ID, description, IS_PASSIVE, 0, NO_HARM, MINOR_POWER);
    }
  }
  final static Stat
    HEALTH = new Stat("Health", "health_stat", ""),
    ARMOUR = new Stat("Armour", "armour_stat", ""),
    MUSCLE = new Stat("Muscle", "muscle_stat", ""),
    BRAIN  = new Stat("Brain" , "brain_stat" , ""),
    SPEED  = new Stat("Speed" , "speed_stat" , ""),
    SIGHT  = new Stat("Sight" , "sight_stat" , "");
  final static int
    STATE_INIT    = -1,
    STATE_AS_PC   =  0,
    STATE_UNAWARE =  1,
    STATE_ACTIVE  =  2,
    STATE_RETREAT =  3;
  final static int
    INIT_LUCK   = 3,
    MAX_LUCK    = 3,
    INIT_STRESS = 0,
    MAX_STRESS  = 100;
  final static int
    FULL_HEAL_WEEKS   = 8   ,
    WAKEUP_PERCENT    = 50  ,
    WEEK_STRESS_DECAY = 2   ,
    WEEK_TRAINING_XP  = 250 ,
    MIN_LEVEL_XP      = 1000;
  final static int
    SLOT_WEAPON = 0,
    SLOT_ARMOUR = 1,
    SLOT_ITEMS  = 2,
    NUM_EQUIP_SLOTS = 4;
  
  
  Kind kind;
  String name;
  int AIstate = STATE_INIT;
  int totalXP = 0;
  int luck = INIT_LUCK, stress = INIT_STRESS;
  
  List <Ability> abilities = new List();
  Tally <Ability> abilityLevels = new Tally();
  Equipped equipSlots[] = new Equipped[NUM_EQUIP_SLOTS];
  
  float injury, fatigue;
  boolean alive, conscious;
  
  Assignment assignment;
  Tile location;
  float posX, posY;
  int actionPoints;
  Action lastAction;
  
  
  
  Person(Kind kind, String name) {
    this.kind = kind;
    this.name = name;
    for (int n = 0; n < kind.baseAbilities.length; n++) {
      Ability a = kind.baseAbilities[n];
      abilities.add(a);
      abilityLevels.set(a, kind.baseAbilityLevels[n]);
    }
    for (int n = 0; n < kind.baseEquipment.length; n++) {
      equipItem(kind.baseEquipment[n]);
    }
    alive = conscious = true;
  }
  
  
  public Person(Session s) throws Exception {
    s.cacheInstance(this);
    kind    = (Kind) s.loadObject();
    name    = s.loadString();
    AIstate = s.loadInt();
    
    totalXP = s.loadInt();
    luck    = s.loadInt();
    stress  = s.loadInt();
    
    s.loadObjects(abilities);
    s.loadTally(abilityLevels);
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      equipSlots[i] = (Equipped) s.loadObject();
    }
    
    injury    = s.loadFloat();
    fatigue   = s.loadFloat();
    alive     = s.loadBool();
    conscious = s.loadBool();
    
    assignment   = (Assignment) s.loadObject();
    location     = (Tile) s.loadObject();
    posX         = s.loadFloat();
    posY         = s.loadFloat();
    actionPoints = s.loadInt();
    lastAction   = (Action) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind);
    s.saveString(name);
    s.saveInt(AIstate);
    s.saveInt(totalXP);
    s.saveInt(luck);
    s.saveInt(stress);
    
    s.saveObjects(abilities);
    s.saveTally(abilityLevels);
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      s.saveObject(equipSlots[i]);
    }
    
    s.saveFloat(injury);
    s.saveFloat(fatigue);
    s.saveBool(alive);
    s.saveBool(conscious);
    
    s.saveObject(assignment);
    s.saveObject(location);
    s.saveFloat(posX);
    s.saveFloat(posY);
    s.saveInt(actionPoints);
    s.saveObject(lastAction);
  }
  
  
  
  /**  General state queries-
    */
  public Series <Ability> abilities() {
    return abilities;
  }
  
  
  public int levelFor(Ability ability) {
    return (int) abilityLevels.valueFor(ability);
  }
  
  
  public int maxHealth() {
    return levelFor(HEALTH);
  }
  
  
  public float injury() {
    return injury;
  }
  
  
  public float fatigue() {
    return fatigue;
  }
  
  
  public int sightRange() {
    return levelFor(SIGHT) / 2;
  }
  
  
  public int maxAP() {
    return levelFor(SPEED) / 4;
  }
  
  
  public int currentAP() {
    return actionPoints;
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
  
  
  public boolean canTakeAction() {
    return conscious() && actionPoints > 0;
  }
  
  
  public Kind kind() {
    return kind;
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
  
  
  public void setAssignment(Assignment assigned) {
    this.assignment = assigned;
  }
  
  
  public Scene currentScene() {
    if (assignment instanceof Scene) return (Scene) assignment;
    return null;
  }
  
  
  public Tile location() {
    return location;
  }
  
  
  public Vec3D exactPosition() {
    return new Vec3D(posX, posY, 0);
  }
  
  
  public void setExactPosition(float x, float y, float z, Scene scene) {
    final Tile oldLoc = location;
    posX = x;
    posY = y;
    location = scene.tileAt((int) (x + 0.5f), (int) (y + 0.5f));
    if (oldLoc != location) {
      if (oldLoc   != null) oldLoc  .standing = null;
      if (location != null) location.standing = this;
    }
  }
  
  
  
  /**  Assigning equipment loadout-
    */
  public void equipItem(Equipped item) {
    equipSlots[item.slotID] = item;
  }
  
  
  public void emptyEquipSlot(int slotID) {
    equipSlots[slotID] = null;
  }
  
  
  public int equipmentBonus(int slotID, int properties) {
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
  
  
  public Series <Equipped> equipment() {
    Batch <Equipped> all = new Batch();
    for (Equipped e : equipSlots) if (e != null) all.add(e);
    return all;
  }
  
  
  
  
  /**  State adjustments-
    */
  public void takeDamage(float injury, float fatigue) {
    float total = injury + fatigue;
    float scale = (total - levelFor(ARMOUR)) / total;
    if (total <= 0 || scale <= 0) return;
    this.injury  += injury  * scale;
    this.fatigue += fatigue * scale;
    checkState();
  }
  
  
  public void liftInjury(float lift) {
    injury = Nums.max(0, injury - lift);
  }
  
  
  public void liftFatigue(float lift) {
    fatigue = Nums.max(0, fatigue - lift);
  }
  
  
  public void liftStress(int lift) {
    stress = Nums.max(0, stress - lift);
  }
  
  
  public void gainXP(int XP) {
    totalXP += XP;
  }
  
  
  public void setActionPoints(int AP) {
    actionPoints = AP;
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  void updateOnTurn() {
    
  }
  
  
  void updateOnBase(float numWeeks) {
    if (! alive) return;
    if (conscious()) fatigue = 0;
    
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
    if (injury + fatigue > maxHealth) this.conscious = false;
    if (injury > maxHealth * 1.5f   ) this.alive     = false;
  }
  
  
  
  /**  Rudimentary AI methods-
    */
  public boolean isHero() {
    return kind.type == Kind.TYPE_HERO;
  }
  
  
  public boolean isCriminal() {
    return kind.type == Kind.TYPE_MOOK || kind.type == Kind.TYPE_BOSS;
  }
  
  
  public boolean isCivilian() {
    return kind.type == Kind.TYPE_CIVILIAN;
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
    return isHero();
  }
  
  
  public boolean retreating() {
    return AIstate == STATE_RETREAT;
  }
  
  
  public boolean canSee(Tile point) {
    Scene scene = currentScene();
    if (scene == null) return false;
    
    //  TODO:  This may require some subsequent refinement!
    if (isPlayerOwned()) return scene.fogAt(point) > 0;
    else {
      if (scene.fogAt(location) <= 0) return false;
      return scene.distance(location, point) < (sightRange() * 2);
    }
  }
  
  
  public Action selectActionAsAI() {
    Scene scene = currentScene();
    if (scene == null || ! conscious()) return null;
    Pick <Action> pick = new Pick(0);
    
    for (Person p : scene.persons) for (Ability a : abilities) {
      Action use = a.configAction(this, p.location, p, scene, null);
      if (use != null) pick.compare(use, a.rateUsage(use) * Rand.avgNums(2));
    }
    
    if (pick.empty()) {
      Series <Person> foes = scene.playerTeam;
      Action motion = retreating() ?
        pickRetreatAction(foes) :
        pickAdvanceAction(foes)
      ;
      pick.compare(motion, 1);
    }
    
    return pick.result();
  }
  
  
  
  /**  Other supplementary action-creation methods-
    */
  Action pickAdvanceAction(Series <Person> foes) {
    Scene scene = currentScene();
    for (Person p : foes) {
      Action motion = Common.MOVE.bestMotionToward(p, this, scene);
      if (motion != null) return motion;
    }
    return null;
  }
  
  
  Action pickRetreatAction(Series <Person> foes) {
    Scene scene = currentScene();
    int hS = scene.size / 2, sD = scene.size - 1;
    Tile exits[] = {
      scene.tileAt(0 , hS),
      scene.tileAt(hS, 0 ),
      scene.tileAt(sD, hS),
      scene.tileAt(hS, sD)
    };
    final Pick <Tile> pick = new Pick();
    for (Tile t : exits) pick.compare(t, 0 - scene.distance(t, location));
    return Common.MOVE.bestMotionToward(pick.result(), this, scene);
  }
  
  
  
  /**  Interface, rendering and debug methods-
    */
  public String name() {
    return name;
  }
  
  
  public String toString() {
    return name;
  }
}
















