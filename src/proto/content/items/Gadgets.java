

package proto.content.items;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;
import static proto.game.person.Equipped.*;
import static proto.game.person.PersonGear.*;



public class Gadgets {
  
  final public static Equipped BATARANGS = new Equipped(
    "Batarang Set", "item_batarangs",
    "Lightweight, throwable projectiles, useful to disarm or startle foes.",
    "media assets/item icons/icon_batarangs.png",
    SLOT_WEAPON, 10, new Object[] {
      ENGINEERING, 2
    },
    IS_WEAPON | IS_RANGED | IS_KINETIC | IS_CONSUMED, 0
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == MARKSMAN) return 2;
      return 0;
    }
  };
  
  final public static Equipped CABLE_GUN = new Equipped(
    "Cable Gun", "item_cable_gun",
    "Launches a climbing cable over long distances, assisting infiltration "+
    "and escape.",
    "media assets/item icons/icon_cable_gun.png",
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
  
  final public static Equipped BODY_ARMOUR = new Equipped(
    "Body Armour", "item_body_armour",
    "Heavy ceramic body armour, virtually impervious to handgun fire.",
    "media assets/item icons/icon_body_armour.png",
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
  
  final public static Equipped KEVLAR_VEST = new Equipped(
    "Kevlar Vest", "item_kevlar_vest",
    "Lightweight kevlar provides reasonable protection and good mobility.",
    "media assets/item icons/icon_kevlar_vest.png",
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
  
  final public static Equipped MED_KIT = new Equipped(
    "Med Kit", "item_med_kit",
    "Med Kits can provide vital first aid to bleeding or incapacitated "+
    "subjects.",
    "media assets/item icons/icon_med_kit.png",
    SLOT_ITEMS, 25, new Object[] {
      PHARMACY, 4
    },
    IS_CONSUMED, 0
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == ANATOMY) return 2;
      return 0;
    }
  };

  final public static Equipped TEAR_GAS = new Equipped(
    "Tear Gas", "item_tear_gas",
    "Tear Gas can blind and suffocate opponents long enough to finish them "+
    "with relative impunity.",
    "media assets/item icons/icon_tear_gas.png",
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








