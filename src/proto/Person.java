

package proto;
import util.*;



public class Person {
  
  
  /**  Data fields and construction-
    */
  Kind kind;
  String name;
  
  static class Stat extends Ability {
    public Stat(String name, String description) {
      super(name, description, IS_PASSIVE, 0, NO_HARM, MINOR_POWER);
    }
  }
  final static Stat
    HEALTH = new Stat("Health", ""),
    ARMOUR = new Stat("Armour", ""),
    MUSCLE = new Stat("Muscle", ""),
    BRAIN  = new Stat("Brain" , ""),
    SPEED  = new Stat("Speed" , ""),
    SIGHT  = new Stat("Sight" , "");
  
  List <Ability> abilities = new List();
  Tally <Ability> abilityLevels = new Tally();
  int luck, stress;
  
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
  
  
  boolean canSee(Tile point) {
    if (scene == null) return false;
    
    //  TODO:  This may require some subsequent refinement!
    if (isPlayerOwned()) return scene.fogAt(point) > 0;
    else return scene.distance(location, point) < sightRange();
  }
  
  
  Action selectActionAsAI() {
    if (scene == null || ! conscious()) return null;
    Pick <Action> pick = new Pick(0);
    
    //  TODO:  In the event that no action is viable, move towards known
    //  enemies and/or take cover?  ...Yeah.
    
    //  Either take cover if you have ranged abilities, or get as close as
    //  possible otherwise...
    
    
    for (Person p : scene.persons) for (Ability a : abilities) {
      Action use = scene.configAction(this, p.location, p, a);
      if (use != null) pick.compare(use, a.rateUsage(use));
    }
    return pick.result();
  }
  
  
  Tile selectVantage() {
    
    
    return null;
  }
  
  
  
  /**  Interface, rendering and debug methods-
    */
  public String toString() {
    return name;
  }
}
















