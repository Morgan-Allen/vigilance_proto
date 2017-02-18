

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;

import static proto.game.person.PersonStats.*;



public class Volley implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Object orig;
  Object targ;
  ItemType weaponType;
  ItemType armourType;
  int damageType;
  boolean ranged;
  
  public int selfAccuracy;
  public int selfDamageBase;
  public int selfDamageRange;
  
  public int damagePercent = 100;
  public int armourPercent = 100;
  public int critPercent   = 10;
  public int stunPercent   = 0;

  public int hitsDefence;
  public int hitsArmour;
  
  public int accuracyRoll;
  public int accuracyMargin;
  public int damageRoll;
  public int damageMargin;
  public int injureDamage;
  public int stunDamage;
  
  public boolean didConnect;
  public boolean didDamage;
  public boolean didCrit;
  
  
  public Volley() {
    return;
  }
  
  
  public Volley(Session s) throws Exception {
    s.cacheInstance(this);
    orig = s.loadObject();
    targ = s.loadObject();
    
    weaponType = (ItemType) s.loadObject();
    armourType = (ItemType) s.loadObject();
    ranged     = s.loadBool();
    damageType = s.loadInt();
    
    selfAccuracy    = s.loadInt();
    selfDamageBase  = s.loadInt();
    selfDamageRange = s.loadInt();
    
    damagePercent   = s.loadInt();
    armourPercent   = s.loadInt();
    critPercent     = s.loadInt();
    stunPercent     = s.loadInt();

    hitsDefence     = s.loadInt();
    hitsArmour      = s.loadInt();
    
    accuracyRoll    = s.loadInt();
    accuracyMargin  = s.loadInt();
    damageRoll      = s.loadInt();
    damageMargin    = s.loadInt();
    injureDamage    = s.loadInt();
    stunDamage      = s.loadInt();
    
    didConnect      = s.loadBool();
    didDamage       = s.loadBool();
    didCrit         = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject((Session.Saveable) orig);
    s.saveObject((Session.Saveable) targ);
    s.saveObject(weaponType);
    s.saveObject(armourType);
    s.saveBool  (ranged    );
    s.saveInt   (damageType);

    s.saveInt(selfAccuracy   );
    s.saveInt(selfDamageBase );
    s.saveInt(selfDamageRange);
    
    s.saveInt(damagePercent  );
    s.saveInt(armourPercent  );
    s.saveInt(critPercent    );
    s.saveInt(stunPercent    );

    s.saveInt(hitsDefence    );
    s.saveInt(hitsArmour     );
    
    s.saveInt(accuracyRoll   );
    s.saveInt(accuracyMargin );
    s.saveInt(damageRoll     );
    s.saveInt(damageMargin   );
    s.saveInt(injureDamage   );
    s.saveInt(stunDamage     );
    
    s.saveBool(didConnect    );
    s.saveBool(didDamage     );
    s.saveBool(didCrit       );
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
    return selfDamageBase * damagePercent / 100f;
  }
  
  
  public float maxDamage() {
    return (selfDamageRange + selfDamageBase) * damagePercent / 100f;
  }
  
  
  
  /**  Life cycle and execution methods-
    */
  public void setupVolley(
    Person self, Person hits, boolean ranged, Scene scene
  ) {
    Item weapon = self.gear.weapon(), armour = hits.gear.armour();
    setupVolley(self, weapon, hits, armour, ranged, scene);
  }
  
  
  public void setupMeleeVolley(Person self, Person hits, Scene scene) {
    Item weapon = self.gear.weapon(), armour = hits.gear.armour();
    if (weapon != null && ! weapon.kind().melee()) {
      weapon = new Item(Common.UNARMED, self.world());
    }
    setupVolley(self, weapon, hits, armour, ranged, scene);
  }
  
  
  public void setupVolley(
    Person self, Item weapon,
    Person hits, Item armour,
    boolean ranged, Scene scene
  ) {
    final Item nativeWeapon = self.gear.weapon();
    this.orig       = self  ;
    this.targ       = hits  ;
    this.ranged     = ranged;
    this.weaponType = weapon == null ? Common.UNARMED    : weapon.kind();
    this.armourType = armour == null ? Common.UNARMOURED : armour.kind();
    
    if (weapon != nativeWeapon) {
      self.gear.equipItem(weapon, PersonGear.SLOT_WEAPON);
    }
    
    damageType      = weaponType.properties;
    selfDamageBase  = self.stats.levelFor(MIN_DAMAGE);
    selfDamageRange = self.stats.levelFor(RNG_DAMAGE);
    hitsArmour      = hits.stats.levelFor(ARMOUR    );
    
    if (weaponType.melee() && ! ranged) {
      selfAccuracy = self.stats.levelFor(ACCURACY);
      selfAccuracy += 50;
      float brawnBonus = self.stats.levelFor(MUSCLE) / 2f;
      selfDamageBase  += Nums.ceil (brawnBonus);
      selfDamageRange += Nums.floor(brawnBonus);
      hitsDefence = hits.stats.levelFor(DEFENCE);
    }
    if (ranged) {
      selfAccuracy = self.stats.levelFor(ACCURACY);
      float normRange = self.stats.sightRange();
      float distance  = scene.distance(self.currentTile(), hits.currentTile());
      float coverBonus = coverBonus(orig, targ, scene);
      selfAccuracy -= 100 * ((distance - 1) - normRange) / normRange;
      hitsDefence = hits.stats.levelFor(DEFENCE);
      hitsDefence += coverBonus;
    }
    
    hitsDefence  = Nums.max(hitsDefence , 1);
    selfAccuracy = Nums.max(selfAccuracy, 1);
    
    if (weapon != nativeWeapon) {
      self.gear.equipItem(nativeWeapon, PersonGear.SLOT_WEAPON);
    }
  }
  
  
  protected void calcMargins() {
    accuracyMargin = (int) Nums.clamp(selfAccuracy - hitsDefence, 0, 100);
    critPercent = ((selfAccuracy - 25) * accuracyMargin) / (5 * 50);
  }
  
  
  protected float coverBonus(Object orig, Object targ, Scene scene) {
    Tile origT = scene.tileUnder(orig), targT = scene.tileUnder(targ);
    //
    //  TODO:  This will not count in the case of elliptical trajectories!  You
    //  need to model that with a different equation.
    int cover = scene.vision.coverFor(targT, origT, false);
    if (cover == Kind.BLOCK_PARTIAL) return 30;
    if (cover == Kind.BLOCK_FULL   ) return 45;
    return 0;
  }
  
  
  
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
    
    accuracyRoll = Rand.index(100);
    if (accuracyRoll < accuracyMargin) {
      didConnect = true;
      I.say("  Volley connected!");
    }
    else {
      didConnect = false;
      I.say("  Volley missed!");
    }
  }
  
  
  void resolveDamage() {
    damageRoll = selfDamageBase + Rand.index(selfDamageRange + 1);
    damageRoll *= damagePercent / 100f;
    
    int rollMargin = accuracyMargin - accuracyRoll;
    int armourSoak = (int) (hitsArmour * (armourPercent / 100f));
    damageMargin = damageRoll - armourSoak;
    
    if (damageMargin > 0) {
      didDamage = true;
      
      if (rollMargin <= 10) {
        I.say("  Glancing blow!");
        damageMargin *= 0.5f;
      }
      else if (rollMargin <= 30) {
        I.say("  Flesh wound!");
        damageMargin *= 0.75f;
      }
      else if (rollMargin <= 60) {
        I.say("  Palpable hit!");
        damageMargin *= 1.0f;
      }
      else {
        I.say("  Dead centre!");
        damageMargin *= 1.25f;
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
    if (Rand.index(100) < critPercent) {
      didCrit = true;
      damageMargin *= 1 + (critPercent / 50f);
      I.say("  Volley did critical damage: "+damageMargin);
    }
    else {
      didCrit = false;
      I.say("  Volley did not crit.");
    }
    
    stunDamage   = (int) (damageMargin * (stunPercent / 100f));
    injureDamage = damageMargin - stunDamage;
  }
  
}






