

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import proto.game.content.Common;
import static proto.game.person.Person.*;



public class Volley implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Object orig;
  Object targ;
  Equipped weaponType;
  Equipped armourType;
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
    weaponType = (Equipped) s.loadObject();
    armourType = (Equipped) s.loadObject();
    ranged     = s.loadBool();
    
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
    s.saveObject((Session.Saveable) orig);
    s.saveObject((Session.Saveable) targ);
    s.saveObject(weaponType);
    s.saveObject(armourType);
    s.saveBool  (ranged    );

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
  
  
  
  /**  Life cycle and execution methods-
    */
  public void setupVolley(
    Person self, Person hits, boolean ranged, Scene scene
  ) {
    this.orig    = self;
    this.targ    = hits;
    this.ranged  = ranged;
    
    //  TODO:  USE THE FULL RANGE OF STATS FOR THIS!  AND SET THOSE UP
    //         BEFOREHAND, BASED OFF PRIMARY ATTRIBUTES!
    
    selfAccuracy = (int) (self.stats.levelFor(SPEED_ACT) * 2.5f) + 50;
    hitsDefence  = (int) (hits.stats.levelFor(SPEED_ACT) * 2.5f) + 0 ;
    
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
      selfDamageBase  = Nums.floor(power / 5f);
      selfDamageRange = Nums.ceil (power / 5f);
    }
    if (ranged) {
      int normRange = self.sightRange() - 2;
      int distance = (int) scene.distance(self.location(), hits.location());
      selfAccuracy -= 5 * (distance - normRange);
    }
    
    selfDamageBase  += Nums.floor(weaponType.bonus / 2f);
    selfDamageRange += Nums.ceil (weaponType.bonus / 2f);
    hitsArmour       = armourType.bonus + hits.baseArmour();
    if (hits.turnDone()) hitsDefence += hits.currentAP() * 10;
    
    float damageMult = damagePercent / 100f;
    float armourMult = armourPercent / 100f;
    selfDamageBase  *= damageMult;
    selfDamageRange *= damageMult;
    hitsArmour      *= armourMult;
  }
  
  
  public void beginVolley() {
    return;
  }
  
  
  public void completeVolley() {
    resolveAccuracy();
    if (didConnect) resolveDamage();
    if (didDamage ) resolveCrit  ();
  }
  
  
  
  void resolveAccuracy() {
    accuracyMargin = selfAccuracy - hitsDefence;
    I.say("  Resolving volley (hit chance "+accuracyMargin+"%)");
    
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
      I.say("  Full damage roll: "+damageRoll);
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






