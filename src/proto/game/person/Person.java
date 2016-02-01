

package proto.game.person;
import proto.common.*;
import proto.game.content.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;



public class Person implements Session.Saveable {
  
  
  /**  Data fields and construction-
    */
  static class Stat extends Ability {
    public Stat(String name, String ID, String description) {
      super(name, ID, description, IS_PASSIVE, 0, NO_HARM, MINOR_POWER);
    }
  }
  final public static Stat
    HEALTH = new Stat("Health", "health_stat", ""),
    ARMOUR = new Stat("Armour", "armour_stat", ""),
    MUSCLE = new Stat("Muscle", "muscle_stat", ""),
    BRAIN  = new Stat("Brain" , "brain_stat" , ""),
    SPEED  = new Stat("Speed" , "speed_stat" , ""),
    SIGHT  = new Stat("Sight" , "sight_stat" , "");
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
    SLOT_WEAPON = 0,
    SLOT_ARMOUR = 1,
    SLOT_ITEMS  = 2,
    NUM_EQUIP_SLOTS = 4;
  public static enum Side {
    HEROES, VILLAINS
  };
  
  
  
  Kind kind;
  Side side;
  String name;
  
  int luck = INIT_LUCK, stress = INIT_STRESS;
  final public PersonStats stats = new PersonStats(this);
  Equipped equipSlots[] = new Equipped[NUM_EQUIP_SLOTS];
  float injury, stun;
  boolean alive, conscious;
  
  Assignment assignment;
  int AIstate = STATE_INIT;
  float confidence = 1.0f;
  
  Tile location;
  float posX, posY;
  int actionPoints;
  Action currentAction;
  boolean turnDone;
  
  
  
  public Person(Kind kind, String name) {
    this.kind = kind;
    this.name = name;
    stats.initFrom(kind);
    for (int n = 0; n < kind.baseEquipped().length; n++) {
      equipItem(kind.baseEquipped()[n]);
    }
    alive = conscious = true;
    
    if (kind.type() == Kind.TYPE_HERO) side = Side.HEROES;
    else side = Side.VILLAINS;
  }
  
  
  public Person(Session s) throws Exception {
    s.cacheInstance(this);
    kind    = (Kind) s.loadObject();
    side    = (Side) s.loadEnum(Side.values());
    name    = s.loadString();
    luck    = s.loadInt();
    stress  = s.loadInt();
    
    stats.loadState(s);
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
    
    location      = (Tile) s.loadObject();
    posX          = s.loadFloat();
    posY          = s.loadFloat();
    actionPoints  = s.loadInt();
    currentAction = (Action) s.loadObject();
    turnDone      = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind);
    s.saveEnum  (side);
    s.saveString(name);
    s.saveInt(luck   );
    s.saveInt(stress );
    
    stats.saveState(s);
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
    
    s.saveObject(location     );
    s.saveFloat (posX         );
    s.saveFloat (posY         );
    s.saveInt   (actionPoints );
    s.saveObject(currentAction);
    s.saveBool  (turnDone     );
  }
  
  
  
  /**  General state queries-
    */
  public int maxHealth() {
    return stats.levelFor(HEALTH);
  }
  
  
  public float injury() {
    return injury;
  }
  
  
  public float stun() {
    return stun;
  }
  
  
  public float healthLevel() {
    return Nums.clamp(1f - ((injury + stun) / maxHealth()), 0, 1);
  }
  
  
  public float bleedRisk() {
    if (injury <= 0) return 0;
    if (stun   <= 0) return 1;
    return injury / (stun + injury);
  }
  
  
  public int sightRange() {
    return 3 + (stats.levelFor(SIGHT) / 4);
  }
  
  
  public int maxAP() {
    return stats.levelFor(SPEED) / 4;
  }
  
  
  public int baseArmour() {
    return stats.levelFor(ARMOUR);
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
  
  
  public Action currentAction() {
    return currentAction;
  }
  
  
  public boolean turnDone() {
    return turnDone;
  }
  
  
  public boolean canTakeAction() {
    if (currentAction != null && currentAction.used.delayed()) return false;
    return conscious() && actionPoints > 0 && ! turnDone;
  }
  
  
  public Kind kind() {
    return kind;
  }
  
  
  public Side side() {
    return side;
  }
  
  
  
  /**  Assigning equipment loadout-
    */
  public void equipItem(Equipped item) {
    Equipped oldItem = equipSlots[item.slotID];
    equipSlots[item.slotID] = item;
    stats.toggleItemAbilities(oldItem, false);
    stats.toggleItemAbilities(item   , true );
  }
  
  
  public void emptyEquipSlot(int slotID) {
    equipSlots[slotID] = null;
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
    posX     = x;
    posY     = y;
    location = scene.tileAt((int) (x + 0.5f), (int) (y + 0.5f));
    if (oldLoc != location) {
      if (oldLoc   != null) oldLoc  .setInside(this, false);
      if (location != null) location.setInside(this, true );
    }
  }
  
  
  
  /**  State adjustments-
    */
  public void receiveAttack(Volley attack) {
    
    if (attack.didConnect) {
      this.injury += attack.injureDamage;
      this.stun   += attack.stunDamage  ;
    }
    else if (turnDone) {
      actionPoints -= 1;
    }
    checkState();
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
  
  
  public void setActionPoints(int AP) {
    actionPoints = AP;
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  public void onTurnStart() {
    actionPoints  = maxAP();
    currentAction = null;
    turnDone      = false;
    assessConfidence();
  }
  
  
  public void assignAction(Action action) {
    currentAction = action;
    this.actionPoints -= action.used.costAP(action);
  }
  
  
  public void updateDuringTurn() {
    Scene scene = currentScene();
    Action a = currentAction;
    if (scene == null || a == null) return;
    
    int elapsed = a.timeElapsed();
    float moveRate = 4;
    float timeSteps = elapsed * moveRate / RunGame.FRAME_RATE;
    
    float alpha = timeSteps % 1;
    Tile path[] = a.path();
    Tile l = path[Nums.clamp((int)  timeSteps     , path.length)];
    Tile n = path[Nums.clamp((int) (timeSteps + 1), path.length)];
    
    Tile oldLoc = location();
    setExactPosition(
      (alpha * n.x) + ((1 - alpha) * l.x),
      (alpha * n.y) + ((1 - alpha) * l.y),
      0, scene
    );
    if (location() != oldLoc) scene.updateFog();
    
    if (timeSteps > path.length) {
      if (! a.started()) {
        a.used.applyOnActionStart(a);
        a.used.checkForTriggers(a, true, false);
      }
      float extraTime = a.used.animDuration();
      a.setProgress((timeSteps - path.length) / (extraTime * moveRate));
    }
    if (a.complete()) {
      a.used.checkForTriggers(a, false, true);
      a.used.applyOnActionEnd(a);
      currentAction = null;
    }
  }
  
  
  public void onTurnEnd() {
    if (currentAction == null || ! currentAction.used.delayed()) {
      currentAction = null;
    }
    turnDone = true;
    //
    //  TODO:  Apply any conditions with stat effects, et cetera!
  }
  
  
  public void updateOnBase(float numWeeks) {
    if (! alive) return;
    if (conscious()) stun = 0;
    
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
    if (injury + stun > maxHealth) this.conscious = false;
    if (injury > maxHealth * 1.5f   ) this.alive     = false;
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
    return isHero();
  }
  
  
  public boolean retreating() {
    return AIstate == STATE_RETREAT;
  }
  
  
  public boolean canSee(Tile point) {
    Scene scene = currentScene();
    if (scene == null) return false;
    
    //  TODO:  Include a bonus for recently seen/unmoved enemies!
    return scene.fogAt(point, side) > 0;
  }
  
  
  public Action selectActionAsAI() {
    Scene scene = currentScene();
    if (scene == null || ! conscious()) return null;
    boolean report = I.talkAbout == this;
    if (report) I.say("\nGetting next AI action for "+this);
    
    Pick <Action> pick = new Pick(0);
    
    if (confidence < 1) {
      AIstate = STATE_RETREAT;
    }
    else {
      AIstate = STATE_ACTIVE;
      for (Person p : scene.persons()) for (Ability a : stats.listAbilities()) {
        Action use = a.configAction(this, p.location, p, scene, null);
        if (use == null) continue;
        float rating = a.rateUsage(use) * Rand.avgNums(2);
        if (report) I.say("  Rating for "+a+" is "+rating);
        pick.compare(use, rating);
      }
    }
    if (pick.empty()) {
      Series <Person> foes = scene.playerTeam();
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
  private void assessConfidence() {
    Scene scene = currentScene();
    float teamHealth = 0, teamPower = 0;
    
    for (Person p : scene.persons()) {
      if (! canSee(p.location())) continue;
      if (p.isAlly(this)) {
        teamPower  += p.powerLevel();
        teamHealth += p.powerLevel() * p.healthLevel();
      }
    }
    //  TODO:  Refine this?
    float courage = 0.2f;
    if (isHero    ()) courage = 1.5f;
    if (isCriminal()) courage = 0.5f;
    
    if (teamPower <= 0) {
      confidence = 0;
    }
    else {
      confidence = teamHealth / teamPower;
      confidence = (confidence + healthLevel()) / 2;
      if (! retreating()) confidence += courage;
      
      I.say("Confidence for "+this+": "+confidence);
    }
  }
  
  
  private float powerLevel() {
    //  TODO:  Refine this!
    if (isHero    ()) return 4;
    if (isCriminal()) return 1;
    return 0;
  }
  
  
  Action pickAdvanceAction(Series <Person> foes) {
    Scene scene = currentScene();
    for (Person p : foes) {
      Action motion = Common.MOVE.bestMotionToward(p, this, scene);
      if (motion != null) return motion;
    }
    
    int range = sightRange() / 2;
    Tile pick = scene.tileAt(
      location.x + (Rand.index(range + 1) * (Rand.yes() ? 1 : -1)),
      location.y + (Rand.index(range + 1) * (Rand.yes() ? 1 : -1))
    );
    
    I.say(this+" picked random tile to approach: "+pick+" (at "+location+")");
    
    return Common.MOVE.bestMotionToward(pick, this, scene);
  }
  
  
  Action pickRetreatAction(Series <Person> foes) {
    Scene scene = currentScene();
    int hS = scene.size() / 2, sD = scene.size() - 1;
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
  
  
  public String confidenceDescription() {
    if (! alive()) return "Dead";
    if (! conscious()) return "Unconscious";
    if (retreating()) return "Retreating";
    if (confidence < 1.33f) return "Shaken";
    if (confidence < 1.66f) return "Steady";
    return "Determined";
  }
}

















