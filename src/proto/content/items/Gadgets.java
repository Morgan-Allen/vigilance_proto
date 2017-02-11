

package proto.content.items;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.Rand;

import static proto.game.person.PersonStats.*;
import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;

import java.awt.Graphics2D;
import java.awt.Image;



public class Gadgets {
  
  
  final static String
    ICONS_DIR  = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  final public static ItemType WING_BLADES = new ItemType(
    "Wing Blades", "item_wing_blades",
    "Lightweight, throwable projectiles, useful to disarm or startle foes.",
    ICONS_DIR+"icon_wing_blades.png",
    SPRITE_DIR+"sprite_wing_blade.png",
    SLOT_TYPE_WEAPON, 20, new Object[] {
      ENGINEERING, 2
    },
    IS_WEAPON | IS_RANGED | IS_KINETIC | IS_CONSUMED
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == MIN_DAMAGE) return 2;
      if (trait == RNG_DAMAGE) return 3;
      return 0;
    }
  };
  
  final public static ItemType REVOLVER = new ItemType(
    "Revolver", "item_revolver",
    "A light, portable sidearm.  Deals significant damage, but with a higher "+
    "risk of death or lasting injury.",
    ICONS_DIR+"icon_revolver.png",
    SPRITE_DIR+"sprite_bullets.png",
    SLOT_TYPE_WEAPON, 40, new Object[] {
      ENGINEERING, 4
    },
    IS_WEAPON | IS_RANGED | IS_KINETIC 
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == MIN_DAMAGE) return 4;
      if (trait == RNG_DAMAGE) return 5;
      return 0;
    }
    
    public void applyOnAttackEnd(Volley volley) {
      Person mark = volley.targAsPerson();
      mark.health.receiveTrauma(2 + Rand.index(4));
      mark.health.toggleBleeding(true);
      return;
    }
  };
  
  final public static ItemType BODY_ARMOUR = new ItemType(
    "Body Armour", "item_body_armour",
    "Heavy ceramic body armour.  Grants excellent protection but impedes "+
    "stealth.",
    ICONS_DIR+"icon_body_armour.png",
    SPRITE_DIR+"sprite_deflect.png",
    SLOT_TYPE_ARMOUR, 200, new Object[] {
      ENGINEERING, 3
    },
    IS_ARMOUR
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == ARMOUR ) return  3;
      if (trait == HIDE_RANGE) return -2;
      return 0;
    }
  };
  
  final public static ItemType KEVLAR_VEST = new ItemType(
    "Kevlar Vest", "item_kevlar_vest",
    "Lightweight kevlar provides reasonable protection and good mobility.",
    ICONS_DIR+"icon_kevlar_vest.png",
    SPRITE_DIR+"sprite_deflect.png",
    SLOT_TYPE_ARMOUR, 140, new Object[] {
      ENGINEERING, 4
    },
    IS_ARMOUR
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == ARMOUR) return 1;
      return 0;
    }
  };
  
  
  final public static Ability BOLAS_THROW = new Ability(
    "Throw Bolas", "ability_bolas_throw",
    ICONS_DIR+"icon_bolas.png",
    "Temporarily entangles a subject, preventing most physical actions. "+
    "High-strength targets or escape artists can break free.",
    Ability.IS_ACTIVE | Ability.IS_EQUIPPED | Ability.IS_RANGED, 1,
    Ability.MINOR_HARM, Ability.MEDIUM_POWER
  ) {
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      if (! (target instanceof Person)) return false;
      return true;
    }
    
    public void applyOnActionEnd(Action use) {
      final Person target = (Person) use.target;
      target.stats.applyCondition(this, use.acting, 3);
      use.acting.gear.useCharge(BOLAS, -1);
    }
    
    public void applyConditionOnTurn(Person person, Person source) {
      float strength = person.stats.levelFor(MUSCLE  ) / 20f;
      float reflex   = person.stats.levelFor(REFLEXES) / 20f;
      if      (Rand.num() < strength) {
        person.stats.removeCondition(this, source);
      }
      else if (Rand.num() < reflex  ) {
        person.stats.removeCondition(this, source);
      }
    }
    
    public boolean conditionAllowsAbility(Ability a) {
      if (a.equipped()) return false;
      if (a.melee   ()) return false;
      return false;
    }
    
    final Image MISSILE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_bolas.png");
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, MISSILE_IMG, g);
    }
  };
  
  final public static ItemType BOLAS = new ItemType(
    "Bolas", "item_bolas",
    "Temporarily entangles a subject, preventing most physical actions. "+
    "High-strength targets or escape artists can break free.",
    ICONS_DIR+"icon_bolas.png",
    SPRITE_DIR+"sprite_bolas.png",
      SLOT_TYPE_ITEM, 15, new Object[] {
        ENGINEERING, 2
      },
      IS_CONSUMED, BOLAS_THROW
  ) {
    
  };
  
  
  final static Ability MED_KIT_HEAL = new Ability(
    "First Aid (Medkit)", "ability_med_kit_heal",
    ICONS_DIR+"icon_med_kit.png",
    "Patches up injury sustained in combat (up to 6 health), halts bleeding "+
    "and reduces recovery time.",
    Ability.IS_ACTIVE | Ability.IS_EQUIPPED | Ability.IS_MELEE, 3,
    Ability.REAL_HELP, Ability.MEDIUM_POWER
  ) {
    
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      if (! (target instanceof Person)) return false;
      return ((Person) target).health.injury() > 0;
    }
    
    public void applyOnActionEnd(Action use) {
      Person healed = (Person) use.target;
      healed.health.liftInjury(6);
      healed.health.liftTotalHarm(2);
      healed.health.toggleBleeding(false);
      use.acting.gear.useCharge(Gadgets.MED_KIT, 0.5f);
    }
  };
  
  final public static ItemType MED_KIT = new ItemType(
    "Med Kit", "item_med_kit",
    "Med Kits can provide vital first aid to bleeding or incapacitated "+
    "subjects.",
    ICONS_DIR+"icon_med_kit.png",
    SPRITE_DIR+"sprite_treatment.png",
    SLOT_TYPE_ITEM, 25, new Object[] {
      MEDICINE, 4
    },
    IS_CONSUMED, MED_KIT_HEAL
  ) {
  };
  
  
  final static Ability TEAR_GAS_ABILITY = new Ability(
    "Tear Gas", "tear_gas_condition",
    ICONS_DIR+"sprite_grenade.png",
    "Reduces accuracy, sight range and action-points.  Lasts three turns.",
    Ability.IS_CONDITION | Ability.IS_AREA_EFFECT | Ability.IS_RANGED, 2,
    Ability.MINOR_HARM, Ability.MEDIUM_POWER
  ) {
    final Image GRENADE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_grenade.png");
    final Image BURST_IMG   = Kind.loadImage(SPRITE_DIR+"sprite_smoke.png"  );
    
    public int maxRange() {
      //  TODO:  Base this off the thrower's strength (as with other
      //  projectiles.)
      return 8;
    }
    
    public int maxEffectRange() {
      return 3;
    }
    
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      return true;
    }
    
    protected boolean affectsTargetInRange(
      Element affects, Scene scene, Person acting
    ) {
      return affects.isPerson();
    }
    
    public void applyOnActionEnd(Action use) {
      use.acting.gear.useCharge(Gadgets.TEAR_GAS, 1);
      final Tile at = use.scene().tileUnder(use.target);
      use.scene().view().addTempFX(BURST_IMG, 3, at.x, at.y, 0, 1);
    }
    
    public float conditionModifierFor(Person person, Trait trait) {
      if (trait == ACT_POINTS ) return -1;
      if (trait == SIGHT_RANGE) return -2;
      if (trait == ACCURACY   ) return -30;
      return 0;
    }
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, GRENADE_IMG, g);
    }
  };
  
  final public static ItemType TEAR_GAS = new ItemType(
    "Tear Gas", "item_tear_gas",
    "Tear Gas can blind and suffocate opponents long enough to finish them "+
    "with relative impunity.",
    ICONS_DIR+"icon_tear_gas.png",
    SPRITE_DIR+"sprite_grenade.png",
    SLOT_TYPE_ITEM, 35, new Object[] {
      MEDICINE, 6
    },
    IS_CONSUMED, TEAR_GAS_ABILITY
  ) {
    
  };
  
  
  //  TODO:  The area revealed should last until the end of the next turn.
  final static Ability SONIC_PROBE_ABILITY = new Ability(
    "Sonic Probe", "sonic_probe_ability",
    ICONS_DIR+"sprite_sonic_probe.png",
    "Reveals an area hidden in the fog of war.",
    Ability.IS_RANGED | Ability.NO_NEED_FOG, 1,
    Ability.NO_HARM, Ability.MINOR_POWER
  ) {
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      return true;
    }
    
    final Image GRENADE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_grenade.png");
    final Image BURST_IMG   = Kind.loadImage(SPRITE_DIR+"sprite_sonic.png"  );
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, GRENADE_IMG, g);
    }
    
    public void applyOnActionEnd(Action use) {
      final Tile at = use.scene().tileUnder(use.target);
      use.scene().vision.liftFogAround(at, 4, use.acting, true);
      use.scene().view().addTempFX(BURST_IMG, 3, at.x, at.y, 0, 1);
    }
  };
  
  final public static ItemType SONIC_PROBE = new ItemType(
    "Sonic Probe", "item_sonic_probe",
    "Can be thrown to reveal areas of hidden terrain.",
    ICONS_DIR+"icon_sonic_probe.png",
    SPRITE_DIR+"sprite_sonic_probe.png",
    SLOT_TYPE_ITEM, 65, new Object[] {
      ENGINEERING, 7
    },
    IS_CONSUMED, SONIC_PROBE_ABILITY
  ) {
    
  };
  
  
}





//  TODO:  This won't be properly useful until you have 3-dimensional terrain
//  implemented.  Leave out for now.
/*
final public static ItemType CABLE_GUN = new ItemType(
  "Cable Gun", "item_cable_gun",
  "Launches a climbing cable over long distances, assisting infiltration "+
  "and escape.",
  ICONS_DIR+"icon_cable_gun.png",
  SPRITE_DIR+"sprite_batarang.png",
  SLOT_WEAPON, 80, new Object[] {
    ENGINEERING, 5
  },
  IS_WEAPON | IS_RANGED | IS_KINETIC, 0
) {
  public float passiveModifierFor(Person person, Trait trait) {
    //if (trait == GYMNASTICS) return 3;
    return 0;
  }
};
//*/





