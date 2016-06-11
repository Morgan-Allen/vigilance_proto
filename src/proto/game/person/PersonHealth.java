

package proto.game.person;
import proto.common.*;
import static proto.game.person.PersonStats.*;
import proto.util.*;



public class PersonHealth {
  

  final public static int
    INIT_LUCK   = 3,
    MAX_LUCK    = 3,
    INIT_STRESS = 0,
    MAX_STRESS  = 100;
  final public static int
    STATE_INIT    = -1,
    STATE_AS_PC   =  0,
    STATE_UNAWARE =  1,
    STATE_ACTIVE  =  2,
    STATE_RETREAT =  3;
  final public static int
    FULL_HEAL_WEEKS   = 8   ,
    WAKEUP_PERCENT    = 50  ,
    WEEK_STRESS_DECAY = 2   ,
    WEEK_TRAINING_XP  = 250 ,
    MIN_LEVEL_XP      = 1000;
  
  final Person person;
  
  int   AIstate    = STATE_INIT;
  float confidence = 1.0f;
  float wariness   = 0.0f;
  int actionPoints;
  
  int luck = INIT_LUCK, stress = INIT_STRESS;
  float injury, stun;
  boolean alive = true, conscious = true;
  
  
  
  PersonHealth(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    AIstate      = s.loadInt();
    confidence   = s.loadFloat();
    wariness     = s.loadFloat();
    actionPoints = s.loadInt();
    luck      = s.loadInt();
    stress    = s.loadInt();
    injury    = s.loadFloat();
    stun      = s.loadFloat();
    alive     = s.loadBool();
    conscious = s.loadBool();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt  (AIstate     );
    s.saveFloat(confidence  );
    s.saveFloat(wariness    );
    s.saveInt  (actionPoints);
    s.saveInt  (luck     );
    s.saveInt  (stress   );
    s.saveFloat(injury   );
    s.saveFloat(stun     );
    s.saveBool (alive    );
    s.saveBool (conscious);
  }
  
  

  public int maxHealth() {
    return person.stats.levelFor(HIT_POINTS);
  }
  
  
  public int maxStress() {
    return person.stats.levelFor(WILLPOWER);
  }
  
  
  public boolean isDoing(int AIstate) {
    return this.AIstate == AIstate;
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
  
  
  public float wariness() {
    return wariness;
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
  
  
  
  /**  State adjustments-
    */
  public void receiveStun(float stun) {
    this.stun += stun;
    checkState();
    person.world.events().log(person+" suffered "+stun+" stun.");
  }
  
  
  public void receiveInjury(float injury) {
    this.injury += injury;
    checkState();
    person.world.events().log(person+" suffered "+injury+" injury.");
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
  
  
  void updateHealth(float numWeeks) {
    if (! alive()) return;
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
    if (conscious && injury + stun > maxHealth) {
      this.conscious = false;
      person.world.events().log(person+" fell unconscious!");
    }
    if (alive && injury > maxHealth * 1.5f) {
      this.alive = false;
      person.world.events().log(person+" was killed!");
    }
  }
  
  
}



