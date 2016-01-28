

package proto.game.scene;
import proto.common.*;
import proto.util.*;
import static proto.game.scene.Person.*;



public class Volley implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Object self;
  Object hits;
  Equipped weaponType;
  Equipped armourType;
  
  int selfAccuracy;
  int selfDamageBase;
  int selfDamageRange;
  
  int damagePercent = 100;
  int armourPercent = 100;
  int critPercent   = 10;
  int stunPercent   = 0;

  int hitsDefence;
  int hitsArmour;
  
  int accuracyMargin;
  int damageRoll;
  int damageMargin;
  int injureDamage;
  int stunDamage;
  
  boolean didConnect;
  boolean didDamage;
  boolean didCrit;
  
  
  public Volley() {
    return;
  }
  
  
  public Volley(Session s) throws Exception {
    s.cacheInstance(this);
    self = s.loadObject();
    hits = s.loadObject();
    weaponType = (Equipped) s.loadObject();
    armourType = (Equipped) s.loadObject();
    
    selfAccuracy    = s.loadInt();
    selfDamageBase  = s.loadInt();
    selfDamageRange = s.loadInt();
    
    damagePercent   = s.loadInt();
    armourPercent   = s.loadInt();
    critPercent     = s.loadInt();
    stunPercent     = s.loadInt();

    hitsDefence     = s.loadInt();
    hitsArmour      = s.loadInt();
    
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
    s.saveObject((Session.Saveable) self);
    s.saveObject((Session.Saveable) hits);
    s.saveObject(weaponType);
    s.saveObject(armourType);

    s.saveInt(selfAccuracy   );
    s.saveInt(selfDamageBase );
    s.saveInt(selfDamageRange);
    
    s.saveInt(damagePercent  );
    s.saveInt(armourPercent  );
    s.saveInt(critPercent    );
    s.saveInt(stunPercent    );

    s.saveInt(hitsDefence    );
    s.saveInt(hitsArmour     );
    
    s.saveInt(accuracyMargin );
    s.saveInt(damageRoll     );
    s.saveInt(damageMargin   );
    s.saveInt(injureDamage   );
    s.saveInt(stunDamage     );
    
    s.saveBool(didConnect    );
    s.saveBool(didDamage     );
    s.saveBool(didCrit       );
  }
  
  
  Person selfAsPerson() {
    if (self instanceof Person) return (Person) self;
    return null;
  }
  
  
  Person hitsAsPerson() {
    if (hits instanceof Person) return (Person) hits;
    return null;
  }
  
  
  
  /**  Life cycle and execution methods-
    */
  void setupVolley(Person self, Person hits, boolean ranged, Scene scene) {
    
    this.self    = self;
    this.hits    = hits;
    selfAccuracy = (self.stats.levelFor(SPEED) * 5) + 50;
    hitsDefence  = (hits.stats.levelFor(SPEED) * 5) + 0 ;
    
    //  TODO:  It might not be a weapon though- could be an ability or
    //  triggered item, etc.
    weaponType = self.equippedInSlot(SLOT_WEAPON);
    armourType = hits.equippedInSlot(SLOT_ARMOUR);
    if (weaponType == null) weaponType = Common.UNARMED   ;
    if (armourType == null) armourType = Common.UNARMOURED;
    
    //  If it's a melee weapon, apply strength bonus.  If it's a ranged weapon,
    //  apply ranged penalties.
    if (weaponType.melee() && ! ranged) {
      int power = self.stats.levelFor(MUSCLE);
      selfDamageBase  = Nums.floor(power / 8f);
      selfDamageRange = Nums.ceil (power / 8f);
    }
    if (ranged) {
      int normRange = self.sightRange() - 2;
      int distance = (int) scene.distance(self.location, hits.location);
      selfAccuracy -= 5 * (distance - normRange);
    }
    
    selfDamageBase  += Nums.floor(weaponType.bonus / 2f);
    selfDamageRange += Nums.ceil (weaponType.bonus / 2f);
    hitsArmour      += armourType.bonus     ;
    
    //  TODO:  Only do this if it's not your turn!
    //hitsDefence     += hits.currentAP() * 10;
    
    float damageMult = damagePercent / 100f;
    float armourMult = armourPercent / 100f;
    selfDamageBase  *= damageMult;
    selfDamageRange *= damageMult;
    hitsArmour      *= armourMult;
  }
  
  
  void beginVolley() {
    return;
  }
  
  
  void completeVolley() {
    resolveAccuracy();
    if (didConnect) resolveDamage();
    if (didDamage ) resolveCrit  ();
  }
  
  
  
  void resolveAccuracy() {
    accuracyMargin = selfAccuracy - hitsDefence;
    if (Rand.index(100) < accuracyMargin) {
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
    damageMargin = damageRoll - hitsArmour;
    
    if (damageMargin > 0) {
      didDamage = true;
      
      if (accuracyMargin > 100) {
        damageMargin *= 1.2f;
      }
      else if (accuracyMargin <= 10) {
        damageMargin *= 0.2f;
      }
      else if (accuracyMargin <= 30) {
        damageMargin *= 0.5f;
      }
      else if (accuracyMargin <= 60) {
        damageMargin *= 1.0f;
      }
      I.say("  Volley did damage: "+damageMargin);
    }
    else {
      didDamage = false;
      I.say("  Volley did no damage!");
    }
  }
  
  
  void resolveCrit() {
    if (Rand.index(100) < (critPercent * accuracyMargin) / 100f) {
      didCrit = true;
      damageMargin *= 1 + (critPercent / 10);
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






