

package proto.content.items;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
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
    SLOT_TYPE_WEAPON, 4, new Object[] {
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
    "Reduces accuracy and limits action.  Lasts three turns.",
    Ability.IS_CONDITION | Ability.IS_AREA_EFFECT | Ability.IS_RANGED, 2,
    Ability.MINOR_HARM, Ability.MEDIUM_POWER
  ) {
    final Image GRENADE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_grenade.png");
    final Image BURST_IMG = Kind.loadImage(SPRITE_DIR+"sprite_smoke.png");
    
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
    
    public Image missileSprite() {
      return GRENADE_IMG;
    }
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, g);
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
  
  //  TODO:  Create a 'sonic probe' item...
  
  
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





