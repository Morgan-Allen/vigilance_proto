

package proto.game.person;
import proto.common.*;
import proto.game.scene.Volley;

import static proto.game.person.PersonStats.*;
import proto.util.*;



public class PersonHealth {
  

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
  
  final Person person;
  
  int luck = INIT_LUCK, stress = INIT_STRESS;
  float injury, stun, totalHarm;
  boolean bleed = false, alive = true, conscious = true;
  
  
  
  PersonHealth(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    luck      = s.loadInt();
    stress    = s.loadInt();
    injury    = s.loadFloat();
    stun      = s.loadFloat();
    totalHarm = s.loadFloat();
    bleed     = s.loadBool();
    alive     = s.loadBool();
    conscious = s.loadBool();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt  (luck     );
    s.saveInt  (stress   );
    s.saveFloat(injury   );
    s.saveFloat(stun     );
    s.saveFloat(totalHarm);
    s.saveBool (bleed    );
    s.saveBool (alive    );
    s.saveBool (conscious);
  }
  
  

  public int maxHealth() {
    return person.stats.levelFor(HEALTH);
  }
  
  
  public int maxStress() {
    return person.stats.levelFor(WILL);
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
  
  
  public float harmToStunRatio() {
    if (injury <= 0) return 0;
    if (stun   <= 0) return 1;
    return injury / (stun + injury);
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
    person.world().events.log(person+" suffered "+stun+" stun.");
  }
  
  
  public void receiveInjury(float injury) {
    this.injury    += injury;
    this.totalHarm += injury;
    checkState();
    person.world().events.log(person+" suffered "+injury+" injury.");
  }
  
  
  public void receiveAttack(Volley attack) {
    if (attack.didConnect) {
      receiveInjury(attack.injureDamage);
      receiveStun  (attack.stunDamage  );
    }
    float bleedRisk = totalHarm / maxHealth();
    if (Rand.num() < bleedRisk) {
      bleed = true;
      person.world().events.log(person+" began bleeding.");
    }
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
  
  
  public void liftTotalHarm(float lift) {
    totalHarm = Nums.max(0, totalHarm - lift);
  }
  
  
  public void toggleBleeding(boolean bleed) {
    this.bleed = bleed;
  }
  
  
  
  /**  Regular updates-
    */
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
      person.world().events.log(person+" fell unconscious!");
    }
    if (alive && injury > maxHealth * 1.5f) {
      this.alive = false;
      person.world().events.log(person+" was killed!");
    }
  }
  
  
}



