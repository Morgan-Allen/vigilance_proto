

package proto.game.person;
import proto.common.*;
import proto.game.scene.Volley;
import proto.game.world.World;

import static proto.game.person.PersonStats.*;
import proto.util.*;



public class PersonHealth {
  

  final public static int
    INIT_LUCK   = 3,
    MAX_LUCK    = 3,
    INIT_STRESS = 0,
    MAX_STRESS  = 100;
  final public static int
    HP_BRUISE_PERCENT  = 25  ,
    STOP_BLEED_PERCENT = 25  ,
    HP_DEATH_PERCENT   = 150 ,
    FULL_HEAL_WEEKS    = 8   ,
    WAKEUP_PERCENT     = 50  ,
    WEEK_STRESS_DECAY  = 2   ,
    WEEK_TRAINING_XP   = 250 ,
    MIN_LEVEL_XP       = 1000;
  
  public static enum State {
    HEALTHY, BRUISED, CRIPPLED, CRITICAL, DECEASED
  }
  
  final Person person;
  
  int luck = INIT_LUCK, stress = INIT_STRESS;
  float injury, stun, totalHarm;
  boolean bleed, conscious;
  State state;
  
  
  
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
    conscious = s.loadBool();
    state     = (State) s.loadEnum(State.values());
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt  (luck     );
    s.saveInt  (stress   );
    s.saveFloat(injury   );
    s.saveFloat(stun     );
    s.saveFloat(totalHarm);
    s.saveBool (bleed    );
    s.saveBool (conscious);
    s.saveEnum (state    );
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
  
  
  public float totalHarm() {
    return totalHarm;
  }
  
  
  public boolean bleeding() {
    return bleed;
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
  
  
  public boolean healthy() {
    return state == State.HEALTHY;
  }
  
  
  public boolean bruised() {
    return state == State.BRUISED;
  }
  
  
  public boolean critical() {
    return state == State.CRITICAL;
  }
  
  
  public boolean alive() {
    return state != State.DECEASED;
  }
  
  
  public boolean dead() {
    return state == State.DECEASED;
  }
  
  
  public boolean conscious() {
    return conscious && alive();
  }
  
  
  public boolean breathing() {
    return true;
  }
  
  
  public State state() {
    return state;
  }
  
  
  public void setState(State state) {
    this.state = state;
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
  
  
  public void incTotalHarm(float harm) {
    this.totalHarm += injury;
    checkState();
    person.world().events.log(person+" suffered "+harm+" total harm.");
  }
  
  
  public void receiveAttack(Volley attack) {
    if (attack.didConnect()) {
      receiveInjury(attack.injureDamage.value());
      receiveStun  (attack.stunDamage  .value());
    }
    float bleedRisk = totalHarm / maxHealth();
    bleedRisk *= 1 - (attack.stunPercent.value() / 100f);
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
  void onTurnStart() {
    float stanchChance = healthLevel() + (STOP_BLEED_PERCENT / 100f);
    if (bleed && Rand.num() < stanchChance) {
      bleed = false;
      person.world().events.log(person+" stopped bleeding.");
    }
  }
  
  
  void onTurnEnd() {
    if (bleed) {
      receiveInjury(1);
    }
  }
  
  
  public void updateHealth(float numWeeks) {
    //
    //  Note:  This is called regularly outside of Scenes, when an agent is
    //  back at base.
    if (! alive()) return;
    if (conscious()) stun = 0;
    
    numWeeks /= World.DAYS_PER_WEEK * World.HOURS_PER_DAY;
    int maxHealth = maxHealth();
    float wakeLevel   = maxHealth * 100f / WAKEUP_PERCENT   ;
    float bruiseLevel = maxHealth * 100f / HP_BRUISE_PERCENT;
    float regen = maxHealth * numWeeks * 2f / FULL_HEAL_WEEKS;
    
    if (totalHarm > 0) {
      totalHarm = Nums.max(0, totalHarm - regen);
      injury    = Nums.max(injury, totalHarm);
    }
    else injury = Nums.max(0, injury - regen);
    
    if (conscious()) {
      stress = Nums.max(0, stress - (int) (WEEK_STRESS_DECAY * numWeeks));
    }
    else if (state == State.CRITICAL || state == State.CRIPPLED) {
      conscious = false;
      if (totalHarm == 0) {
        setState(State.BRUISED);
      }
    }
    else {
      conscious = injury < wakeLevel;
      if (injury < bruiseLevel) {
        setState(State.HEALTHY);
      }
      else if (injury < wakeLevel) {
        setState(State.BRUISED);
      }
    }
  }
  
  
  void checkState() {
    //
    //  Note this is called during Scenes, after taking injuries, for example.
    int maxHealth = maxHealth();
    
    if (conscious && injury + stun > maxHealth) {
      this.conscious = false;
      person.world().events.log(person+" fell unconscious!");
    }
    if (healthy() && totalHarm > (maxHealth * HP_BRUISE_PERCENT / 100f)) {
      setState(State.BRUISED);
      person.world().events.log(person+" takes a beating!");
    }
    if (alive() && injury > (maxHealth * HP_DEATH_PERCENT / 100f)) {
      setState(State.DECEASED);
      person.world().events.log(person+" was killed!");
    }
  }
  
  
}



