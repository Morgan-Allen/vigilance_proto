

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
  
  public int accuracyPercent;
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
    
    accuracyPercent = s.loadInt();
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
    
    s.saveInt(accuracyPercent);
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
    this.orig   = self  ;
    this.targ   = hits  ;
    this.ranged = ranged;
    
    ItemType weapon = self.gear.currentWeapon();
    damageType = weapon.properties;
    
    selfDamageBase  = self.stats.levelFor(MIN_DAMAGE);
    selfDamageRange = self.stats.levelFor(RNG_DAMAGE);
    hitsArmour      = hits.stats.levelFor(ARMOUR    );
    
    if (weapon.melee() && ! ranged) {
      float brawnBonus = self.stats.levelFor(STAMINA) / 2f;
      selfAccuracy = self.stats.levelFor(CLOSE_COMBAT);
      selfDamageBase  += Nums.ceil (brawnBonus);
      selfDamageRange += Nums.floor(brawnBonus);
      hitsDefence = hits.stats.levelFor(CLOSE_COMBAT);
    }
    if (ranged) {
      selfAccuracy = self.stats.levelFor(MARKSMAN);
      float normRange = self.actions.sightRange() - 2;
      float distance  = scene.distance(self.currentTile(), hits.currentTile());
      selfAccuracy -= 5 * (distance - normRange);
      hitsDefence = hits.stats.levelFor(GYMNASTICS);
    }
    
    hitsDefence  = Nums.max(hitsDefence , 1);
    selfAccuracy = Nums.max(selfAccuracy, 1);
    if (selfAccuracy > hitsDefence) {
      float hitChance = 0;
      hitChance = ((selfAccuracy - hitsDefence) / selfAccuracy);
      hitChance += 0.5f * (1 - hitChance);
      accuracyPercent = (int) (hitChance * 100);
    }
    else {
      float defChance = 0;
      defChance = ((hitsDefence - selfAccuracy) / hitsDefence);
      defChance += 0.5f * (1 - defChance);
      accuracyPercent = 100 - (int) (defChance * 100);
    }
    critPercent = ((20 + selfAccuracy) * accuracyPercent) / 100;
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
    I.say("  Resolving connection (hit chance "+accuracyPercent+"%)");
    
    if (Rand.index(100) < accuracyPercent) {
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
    int armourSoak = (int) (hitsArmour * (armourPercent / 100f));
    damageMargin = damageRoll - armourSoak;
    
    if (damageMargin > 0) {
      didDamage = true;
      
      if (accuracyPercent > 60) {
        I.say("  Dead centre!");
        damageMargin *= 1.2f;
      }
      else if (accuracyPercent <= 10) {
        I.say("  Glancing blow!");
        damageMargin *= 0.2f;
      }
      else if (accuracyPercent <= 30) {
        I.say("  Flesh wound!");
        damageMargin *= 0.8f;
      }
      else if (accuracyPercent <= 60) {
        I.say("  Palpable hit!");
        damageMargin *= 1.0f;
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
    if (Rand.index(100) < (critPercent * accuracyPercent) / 100f) {
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






