

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
  
  
  Kind kind;
  String name;
  int AIstate = STATE_INIT;
  int luck = INIT_LUCK, stress = INIT_STRESS;
  
  List <Ability> abilities = new List();
  Tally <Ability> abilityLevels = new Tally();
  
  float injury, fatigue;
  boolean alive, conscious;
  
  Scene scene;
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
    alive = conscious = true;
  }
  
  
  public Person(Session s) throws Exception {
    s.cacheInstance(this);
    kind    = (Kind) s.loadObject();
    name    = s.loadString();
    AIstate = s.loadInt();
    luck    = s.loadInt();
    stress  = s.loadInt();
    
    s.loadObjects(abilities);
    s.loadTally(abilityLevels);
    
    injury    = s.loadFloat();
    fatigue   = s.loadFloat();
    alive     = s.loadBool();
    conscious = s.loadBool();
    
    scene        = (Scene) s.loadObject();
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
    s.saveInt(luck);
    s.saveInt(stress);
    
    s.saveObjects(abilities);
    s.saveTally(abilityLevels);
    
    s.saveFloat(injury);
    s.saveFloat(fatigue);
    s.saveBool(alive);
    s.saveBool(conscious);
    
    s.saveObject(scene);
    s.saveObject(location);
    s.saveFloat(posX);
    s.saveFloat(posY);
    s.saveInt(actionPoints);
    s.saveObject(lastAction);
  }
  
  
  
  /**  Stat interactions/adjustments-
    */
  void takeDamage(float injury, float fatigue) {
    this.injury  += injury;
    this.fatigue += fatigue;
    checkState();
  }
  
  
  int levelFor(Ability ability) {
    return (int) abilityLevels.valueFor(ability);
  }
  
  
  int sightRange() {
    return levelFor(SIGHT) / 2;
  }
  
  
  int maxHealth() {
    return levelFor(HEALTH);
  }
  
  
  int maxAP() {
    return levelFor(SPEED) / 4;
  }
  
  
  int currentAP() {
    return actionPoints;
  }
  
  
  boolean alive() {
    return alive;
  }
  
  
  boolean conscious() {
    return conscious && alive;
  }
  
  
  boolean availableForMission() {
    return scene == null;
  }
  
  
  
  /**  Regular updates and life-cycle-
    */
  void updateOnTurn() {
    
  }
  
  
  void updateInWorld() {
    
  }
  
  
  void checkState() {
    int maxHealth = maxHealth();
    if (injury + fatigue > maxHealth) this.conscious = false;
    if (injury > maxHealth * 1.5f   ) this.alive     = false;
  }
  
  
  
  /**  Rudimentary AI methods-
    */
  boolean isHero() {
    return kind.type == Kind.TYPE_HERO;
  }
  
  
  boolean isCriminal() {
    return kind.type == Kind.TYPE_MOOK || kind.type == Kind.TYPE_BOSS;
  }
  
  
  boolean isCivilian() {
    return kind.type == Kind.TYPE_CIVILIAN;
  }
  
  
  boolean isEnemy(Person other) {
    if (isHero    ()) return other.isCriminal();
    if (isCriminal()) return other.isHero    ();
    return false;
  }
  
  
  boolean isAlly(Person other) {
    if (isHero    ()) return other.isHero    ();
    if (isCriminal()) return other.isCriminal();
    return false;
  }
  
  
  boolean isNeutral(Person other) {
    return ! (isEnemy(other) || isAlly(other));
  }
  
  
  boolean isPlayerOwned() {
    return isHero();
  }
  
  
  boolean retreating() {
    return AIstate == STATE_RETREAT;
  }
  
  
  boolean canSee(Tile point) {
    if (scene == null) return false;
    
    //  TODO:  This may require some subsequent refinement!
    if (isPlayerOwned()) return scene.fogAt(point) > 0;
    else {
      if (scene.fogAt(location) <= 0) return false;
      return scene.distance(location, point) < (sightRange() * 2);
    }
  }
  
  
  Action selectActionAsAI() {
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
    for (Person p : foes) {
      Action motion = Common.MOVE.bestMotionToward(p, this, scene);
      if (motion != null) return motion;
    }
    return null;
  }
  
  
  Action pickRetreatAction(Series <Person> foes) {
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
  public String toString() {
    return name;
  }
}
















