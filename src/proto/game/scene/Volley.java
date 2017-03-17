

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;


//
//  Analogous to the Attempt class, but for Actions instead of Tasks...

public class Volley implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Ability source;
  Object orig;
  Object targ;
  ItemType weaponType;
  ItemType armourType;
  int damageType;
  boolean ranged;
  
  //  Provides a 'gatekeeper' around increments and decrements to relevant
  //  volley parameters, to ensure that all modifiers can be recorded (see
  //  below.)
  public class Stat {
    String label;
    int value;
    
    Stat(String label, int defValue) {
      this.label = label;
      this.value = defValue;
      allStats.add(this);
    }
    
    public void set(float val, Object source) {
      inc(val - value, source);
    }
    
    public void inc(float inc, Object source) {
      value += inc;
      if (inc != 0 && source != null) recordModifier(this, (int) inc, source);
    }
    
    public int value() {
      return value;
    }
    
    public String toString() { return ""+value; }
  }
  List <Stat> allStats = new List();
  
  final public Stat
    selfAccuracy    = new Stat("self_accuracy"    , 0  ),
    selfDamageBase  = new Stat("self_damage_base" , 0  ),
    selfDamageRange = new Stat("self_damage_range", 0  ),
    damagePercent   = new Stat("damage_percent"   , 100),
    armourPercent   = new Stat("armour_percent"   , 100),
    critPercent     = new Stat("crit_percent"     , 0  ),
    stunPercent     = new Stat("stun_percent"     , 0  ),
    hitsDefence     = new Stat("hits_defence"     , 0  ),
    hitsArmour      = new Stat("hits_armour"      , 0  ),
    accuracyRoll    = new Stat("accuracy_roll"    , 0  ),
    accuracyMargin  = new Stat("accuracy_margin"  , 0  ),
    damageRoll      = new Stat("damage_roll"      , 0  ),
    damageMargin    = new Stat("damage_margin"    , 0  ),
    injureDamage    = new Stat("injure_damage"    , 0  ),
    stunDamage      = new Stat("stun_damage"      , 0  );
  
  final public static String
    IN_MELEE = "In Melee",
    RANGE    = "Range",
    COVER    = "Cover"
  ;
  
  boolean didBegin  ;
  boolean didConnect;
  boolean didDamage ;
  boolean didCrit   ;
  
  //  Note- we don't save/load this for the moment, since it's purely used for
  //  UI-display purposes.
  public static class Modifier {
    public Stat stat;
    public Object source;
    public int modValue;
    
    public String toString() {
      return stat.label+" "+modValue+" ("+source+")";
    }
  }
  private Stack <Modifier> modifiers = new Stack();
  
  
  public Volley() {
    return;
  }
  
  
  public Volley(Session s) throws Exception {
    s.cacheInstance(this);
    orig = s.loadObject();
    targ = s.loadObject();
    
    source     = (Ability ) s.loadObject();
    weaponType = (ItemType) s.loadObject();
    armourType = (ItemType) s.loadObject();
    ranged     = s.loadBool();
    damageType = s.loadInt();
    
    for (Stat stat : allStats) {
      stat.value = s.loadInt();
    }
    
    didBegin   = s.loadBool();
    didConnect = s.loadBool();
    didDamage  = s.loadBool();
    didCrit    = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject((Session.Saveable) orig);
    s.saveObject((Session.Saveable) targ);
    
    s.saveObject(source    );
    s.saveObject(weaponType);
    s.saveObject(armourType);
    s.saveBool  (ranged    );
    s.saveInt   (damageType);
    
    for (Stat stat : allStats) {
      s.saveInt(stat.value);
    }
    
    s.saveBool(didBegin  );
    s.saveBool(didConnect);
    s.saveBool(didDamage );
    s.saveBool(didCrit   );
  }
  
  
  public Ability sourceAbility() {
    return source;
  }
  
  
  public Person origAsPerson() {
    if (orig instanceof Person) return (Person) orig;
    return null;
  }
  
  
  public Person targAsPerson() {
    if (targ instanceof Person) return (Person) targ;
    return null;
  }
  
  
  public boolean ranged() { return   ranged; }
  public boolean melee () { return ! ranged; }
  public ItemType weaponType() { return weaponType; }
  public ItemType armourType() { return armourType; }
  
  
  public boolean hasDamageType(int properties) {
    return (damageType & properties) == properties;
  }
  
  
  public float minDamage() {
    return selfDamageBase.value * damagePercent.value / 100f;
  }
  
  
  public float maxDamage() {
    int maxDamage = selfDamageRange.value + selfDamageBase.value;
    return maxDamage * damagePercent.value / 100f;
  }
  
  
  
  /**  Methods for initial setup of the volley, calculation of chances to hit,
    *  and any other contributing factors-
    */
  public void setupVolley(
    Person self, Person hits, Ability source, boolean ranged, Scene scene
  ) {
    Item weapon = self.gear.weapon(), armour = hits.gear.armour();
    setupVolley(self, weapon, hits, armour, source, ranged, scene);
  }
  
  
  public void setupVolley(
    Person self, Item weapon,
    Person hits, Item armour,
    Ability source, boolean ranged, Scene scene
  ) {
    this.orig       = self  ;
    this.targ       = hits  ;
    this.ranged     = ranged;
    this.weaponType = weapon == null ? Common.UNARMED    : weapon.kind();
    this.armourType = armour == null ? Common.UNARMOURED : armour.kind();
    
    damageType      = weaponType.properties;
    selfDamageBase .inc(self.stats.levelFor(MIN_DAMAGE), MIN_DAMAGE);
    selfDamageRange.inc(self.stats.levelFor(RNG_DAMAGE), RNG_DAMAGE);
    hitsArmour     .inc(hits.stats.levelFor(ARMOUR    ), ARMOUR    );
    
    if (weaponType.melee() && ! ranged) {
      selfAccuracy.inc(self.stats.levelFor(ACCURACY), ACCURACY);
      selfAccuracy.inc(50, IN_MELEE);
      float brawnBonus = self.stats.levelFor(MUSCLE) / 2f;
      selfDamageBase .inc(Nums.ceil (brawnBonus), MUSCLE);
      selfDamageRange.inc(Nums.floor(brawnBonus), MUSCLE);
      hitsDefence.inc(hits.stats.levelFor(DEFENCE), DEFENCE);
    }
    if (ranged) {
      selfAccuracy.inc(self.stats.levelFor(ACCURACY), ACCURACY);
      float normRange  = self.stats.sightRange();
      float distance   = scene.distance(self.currentTile(), hits.currentTile());
      float coverBonus = coverBonus(self, targ, scene);
      float rangeMod   = 100 * ((distance - 1) - normRange) / normRange;
      selfAccuracy.inc(0 - (int) rangeMod, RANGE);
      hitsDefence.inc(hits.stats.levelFor(DEFENCE), DEFENCE);
      hitsDefence.inc((int) coverBonus, COVER);
    }
    
    calcMargins();
  }
  
  
  public void calcMargins() {
    int margin = selfAccuracy.value - hitsDefence.value;
    int crit   = ((selfAccuracy.value - 25) * margin) / (5 * 50);
    accuracyMargin.value = (int) Nums.clamp(margin, 1, 100);
    critPercent   .value = (int) Nums.clamp(crit  , 1, 100);
  }
  
  
  protected float coverBonus(Person self, Object targ, Scene scene) {
    Tile targT = scene.tileUnder(targ);
    Tile origT = scene.vision.bestVantage(self, targT, false);
    //
    //  TODO:  This will not count in the case of elliptical trajectories!  You
    //  need to model that with a different equation.
    int cover = scene.vision.coverFor(targT, origT, false);
    if (cover == Kind.BLOCK_PARTIAL) return 30;
    if (cover == Kind.BLOCK_FULL   ) return 45;
    return 0;
  }
  
  
  protected void recordModifier(Stat stat, int inc, Object source) {
    Modifier match = null;
    for (Modifier m : modifiers) if (m.source == source && m.stat == stat) {
      match = m;
      break;
    }
    if (match == null) modifiers.add(match = new Modifier());
    
    match.stat     = stat;
    match.modValue = inc ;
    match.source   = source;
  }
  
  
  public Series <Modifier> extractModifiers(Stat... affected) {
    Batch <Modifier> all = new Batch();
    for (Modifier m : modifiers) {
      if (Visit.arrayIncludes(affected, m.stat)) all.add(m);
    }
    return all;
  }
  
  
  public int modifierFor(Stat stat, Object source) {
    for (Modifier m : modifiers) if (m.source == source && m.stat == stat) {
      return m.modValue;
    }
    return 0;
  }
  
  
  
  /**  Methods for actual execution of the volley-
    */
  public void beginVolley() {
    return;
  }
  
  
  public void completeVolley() {
    I.say("Resolving volley between "+orig+" and "+targ);
    if (true      ) resolveConnection();
    if (didConnect) resolveDamage    ();
    if (didDamage ) resolveCrit      ();
  }
  
  
  
  void resolveConnection() {
    calcMargins();
    I.say("  Resolving connection (hit chance "+accuracyMargin+"%)");
    
    accuracyRoll.value = Rand.index(100);
    if (accuracyRoll.value < accuracyMargin.value) {
      didConnect = true;
      I.say("  Volley connected!");
    }
    else {
      didConnect = false;
      I.say("  Volley missed!");
    }
  }
  
  
  void resolveDamage() {
    int damage = 0;
    damage = selfDamageBase.value + Rand.index(selfDamageRange.value + 1);
    damage *= damagePercent.value / 100f;
    damageRoll.value = damage;
    
    int rollMargin = accuracyMargin.value - accuracyRoll.value;
    int armourSoak = (int) (hitsArmour.value * (armourPercent.value / 100f));
    damageMargin.value = damageRoll.value - armourSoak;
    
    if (damageMargin.value > 0) {
      didDamage = true;
      
      if (rollMargin <= 10) {
        I.say("  Glancing blow!");
        damageMargin.value *= 0.5f;
      }
      else if (rollMargin <= 30) {
        I.say("  Flesh wound!");
        damageMargin.value *= 0.75f;
      }
      else if (rollMargin <= 60) {
        I.say("  Palpable hit!");
        damageMargin.value *= 1.0f;
      }
      else {
        I.say("  Dead centre!");
        damageMargin.value *= 1.25f;
      }
      I.say("  Full damage roll:  "+damageRoll);
      I.say("  Armour absorbed:   "+armourSoak);
      I.say("  Volley did damage: "+damageMargin);
    }
    else {
      didDamage = false;
      I.say("  Volley did no damage!");
    }
  }
  
  
  void resolveCrit() {
    I.say("  Resolving critical (crit chance "+critPercent+")");
    if (Rand.index(100) < critPercent.value) {
      didCrit = true;
      damageMargin.value *= 1 + (critPercent.value / 50f);
      I.say("  Volley did critical damage: "+damageMargin);
    }
    else {
      didCrit = false;
      I.say("  Volley did not crit.");
    }
    
    stunDamage.value = (int) (damageMargin.value * (stunPercent.value / 100f));
    injureDamage.value = damageMargin.value - stunDamage.value;
  }
  
  
  public boolean didConnect() { return didConnect; }
  public boolean didDamage () { return didDamage ; }
  public boolean didCrit   () { return didCrit   ; }
  
}






