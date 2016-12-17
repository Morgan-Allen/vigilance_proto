

package proto.content.items;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;
import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;



public class Gadgets {
  
  
  final static String
    ICONS_DIR  = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  final public static ItemType BATARANGS = new ItemType(
    "Batarang Set", "item_batarangs",
    "Lightweight, throwable projectiles, useful to disarm or startle foes.",
    ICONS_DIR+"icon_batarangs.png",
    SPRITE_DIR+"sprite_batarang.png",
    SLOT_WEAPON, 10, new Object[] {
      ENGINEERING, 2
    },
    IS_WEAPON | IS_RANGED | IS_KINETIC | IS_CONSUMED, 0
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == MIN_DAMAGE) return 2;
      if (trait == RNG_DAMAGE) return 3;
      if (trait == MARKSMAN  ) return 5;
      return 0;
    }
  };
  
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
      if (trait == GYMNASTICS) return 3;
      return 0;
    }
  };
  
  final public static ItemType BODY_ARMOUR = new ItemType(
    "Body Armour", "item_body_armour",
    "Heavy ceramic body armour, virtually impervious to handgun fire.",
    ICONS_DIR+"icon_body_armour.png",
    SPRITE_DIR+"sprite_deflect.png",
    SLOT_ARMOUR, 200, new Object[] {
      ENGINEERING, 3
    },
    IS_ARMOUR, 0
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == STAMINA   ) return  3;
      if (trait == GYMNASTICS) return -1;
      return 0;
    }
  };
  
  final public static ItemType KEVLAR_VEST = new ItemType(
    "Kevlar Vest", "item_kevlar_vest",
    "Lightweight kevlar provides reasonable protection and good mobility.",
    ICONS_DIR+"icon_kevlar_vest.png",
    SPRITE_DIR+"sprite_deflect.png",
    SLOT_ARMOUR, 140, new Object[] {
      ENGINEERING, 4
    },
    IS_ARMOUR, 0
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == STAMINA) return 2;
      return 0;
    }
  };
  
  final public static ItemType MED_KIT = new ItemType(
    "Med Kit", "item_med_kit",
    "Med Kits can provide vital first aid to bleeding or incapacitated "+
    "subjects.",
    ICONS_DIR+"icon_med_kit.png",
    SPRITE_DIR+"sprite_treatment.png",
    SLOT_ITEMS, 25, new Object[] {
      PHARMACY, 4
    },
    IS_CONSUMED, 0
  ) {
    
    //  TODO:  Provide an active 'stabilise/heal' ability instead
    
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == ANATOMY) return 2;
      return 0;
    }
  };

  final public static ItemType TEAR_GAS = new ItemType(
    "Tear Gas", "item_tear_gas",
    "Tear Gas can blind and suffocate opponents long enough to finish them "+
    "with relative impunity.",
    ICONS_DIR+"icon_tear_gas.png",
    SPRITE_DIR+"sprite_smoke.png",
    SLOT_ITEMS, 35, new Object[] {
      PHARMACY, 6
    },
    IS_CONSUMED, 0
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == CLOSE_COMBAT) return 1;
      if (trait == MARKSMAN    ) return 2;
      return 0;
    }
  };
  
  
    
  
  
  
  
}








