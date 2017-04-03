

package proto.content.items;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonGear.*;
import static proto.game.person.PersonStats.*;



public class Weapons {
  
  final static String
    ICONS_DIR  = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  public static class WeaponType extends ItemType {
    
    int minDamage, rangeDamage;
    
    public WeaponType(
      String name, String ID, String description,
      String iconImgPath, Object media,
      int subtype, int minDamage, int rangeDamage,
      int buildCost, Object[] craftArgs,
      Ability... abilities
    ) {
      super(
        name, ID, description, iconImgPath, media,
        subtype, SLOT_TYPE_WEAPON, buildCost, craftArgs,
        propertiesFromSubtype(subtype), abilities
      );
      this.minDamage   = minDamage  ;
      this.rangeDamage = rangeDamage;
    }
    
    static int propertiesFromSubtype(int subtype) {
      if (subtype == SUBTYPE_BLUNT || subtype == SUBTYPE_BLADE) {
        return IS_WEAPON | IS_KINETIC;
      }
      else {
        return IS_WEAPON | IS_RANGED | IS_KINETIC | IS_CONSUMED;
      }
    }
    
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == MIN_DAMAGE) return minDamage  ;
      if (trait == RNG_DAMAGE) return rangeDamage;
      return 0;
    }
    
    public void modifyAttackVolley(Volley volley) {
      if (subtype() == SUBTYPE_BLUNT) {
        volley.stunPercent.set(50, "Blunt Weapon");
      }
    }
    
    public void applyOnAttackEnd(Volley volley) {
      if (! volley.didConnect()) return;
      if (subtype() == SUBTYPE_HEAVY_GUN || subtype() == SUBTYPE_PRECISE_GUN) {
        Person mark = volley.targAsPerson();
        float trauma = minDamage + Rand.index(rangeDamage + 1);
        mark.health.incTotalHarm(trauma / 2);
        mark.health.toggleBleeding(true);
      }
      return;
    }
  }
  
  
  final public static ItemType WING_BLADES = new WeaponType(
    "Wing Blades", "item_wing_blades",
    "Lightweight, throwable projectiles.  Inflict modest damage, but can "+
    "bypass cover and disarm opponents.",
    ICONS_DIR+"icon_wing_blades.png",
    SPRITE_DIR+"sprite_wing_blade.png",
    Kind.SUBTYPE_WING_BLADE, 2, 3,
    20, new Object[] { ENGINEERING, 2 }
  ) {
    public void modifyAttackVolley(Volley volley) {
      int cover = volley.modifierFor(volley.hitsDefence, Volley.COVER);
      int bypass = Nums.round(cover / 2, 5, true);
      bypass = Nums.min(cover, Nums.max(20, bypass));
      volley.selfAccuracy.inc(bypass, "Wing Blade vs. Cover");
      super.modifyAttackVolley(volley);
    }
    
    public void applyOnAttackEnd(Volley volley) {
      super.applyOnAttackEnd(volley);
      
      Person struck = volley.targAsPerson();
      if (struck == null) return;
      
      ItemType weapon = struck.gear.weaponType();
      int disarmChance = 10;
      if (weapon.blade() || weapon.preciseGun()) disarmChance = 33;
      disarmChance -= Rand.index(struck.stats.levelFor(MUSCLE) + 1);
      
      if (volley.didConnect() && Rand.index(100) < disarmChance) {
        struck.gear.dropItem(SLOT_WEAPON);
      }
    }
  };
  
  
  final public static ItemType BRASS_KNUCKLES = new WeaponType(
    "Brass Knuckles", "item_brass_knuckles",
    "Old-fashioned knuckle-dusters.",
    ICONS_DIR+"icon_brass_knuckles.png",
    SPRITE_DIR+"sprite_brass_knuckles.png",
    Kind.SUBTYPE_BLUNT, 1, 1,
    5, new Object[] { ENGINEERING, 1 }
  );
  
  
  final public static ItemType BASEBALL_BAT = new WeaponType(
    "Baseball Bat", "item_baseball_bat",
    "Step up to the plate.",
    ICONS_DIR+"icon_baseball_bat.png",
    SPRITE_DIR+"sprite_baseball_bat.png",
    Kind.SUBTYPE_BLUNT, 2, 2,
    5, new Object[] { ENGINEERING, 1 }
  );
  
  
  final public static ItemType REVOLVER = new WeaponType(
    "Revolver", "item_revolver",
    "A light, portable sidearm.",
    ICONS_DIR+"icon_revolver.png",
    SPRITE_DIR+"sprite_bullets.png",
    Kind.SUBTYPE_PRECISE_GUN, 4, 5,
    30, new Object[] { ENGINEERING, 4 }
  );
  
  final public static ItemType AUTOMATIC = new WeaponType(
    "Automatic", "item_automatic",
    "A rapid-fire machine gun capable of shredding armour and light cover.",
    ICONS_DIR+"icon_revolver.png",
    SPRITE_DIR+"sprite_bullets.png",
    Kind.SUBTYPE_HEAVY_GUN, 6, 4,
    55, new Object[] { ENGINEERING, 4 }
  ) {
    public void modifyAttackVolley(Volley volley) {
      //  TODO:  Shred any incidental objects providing cover.
      volley.hitsArmour.inc(-2, this);
      super.modifyAttackVolley(volley);
    }
  };
  
  final public static ItemType RIFLE = new WeaponType(
    "Rifle", "item_rifle",
    "A hunter's rifle, suitable for long-range sniping.",
    ICONS_DIR+"icon_rifle.png",
    SPRITE_DIR+"sprite_bullets.png",
    Kind.SUBTYPE_PRECISE_GUN, 5, 5,
    40, new Object[] { ENGINEERING, 4 }
  ) {
    public void modifyAttackVolley(Volley volley) {
      int rangeMod = volley.modifierFor(volley.selfAccuracy, Volley.RANGE);
      int accBonus = Nums.max(0, (rangeMod / -2) + 10);
      volley.selfAccuracy.inc(accBonus, this);
      super.modifyAttackVolley(volley);
    }
  };
  
}





