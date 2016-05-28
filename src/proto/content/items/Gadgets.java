

package proto.content.items;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;
import static proto.game.person.Equipped.*;
import static proto.game.person.Person.*;


public class Gadgets {
  
  
  
  final public static Equipped
    BATARANGS = new Equipped(
      "Batarang Set", "item_batarangs",
      "Lightweight, throwable projectiles, useful to disarm or startle foes.",
      "media assets/item icons/icon_batarangs.png",
      SLOT_WEAPON, 10, new Object[] {
        ENGINEERING, 2
      },
      IS_WEAPON | IS_RANGED | IS_KINETIC | IS_CONSUMED, 0
    ) {
      
    }
  ;
  final public static Equipped
    CABLE_GUN = new Equipped(
      "Cable Gun", "item_cable_gun",
      "Launches a climbing cable over long distances, assisting infiltration "+
      "and escape.",
      "media assets/item icons/icon_cable_gun.png",
      SLOT_WEAPON, 80, new Object[] {
        ENGINEERING, 5
      },
      IS_WEAPON | IS_RANGED | IS_KINETIC, 0
    ) {
      
    }
  ;
  final public static Equipped
    BODY_ARMOUR = new Equipped(
      "Body Armour", "item_body_armour",
      "Heavy ceramic body armour, virtually impervious to handgun fire.",
      "media assets/item icons/icon_body_armour.png",
      SLOT_ARMOUR, 200, new Object[] {
        ENGINEERING, 3
      },
      IS_ARMOUR, 0
    ) {
      
    }
  ;
  final public static Equipped
    KEVLAR_VEST = new Equipped(
      "Kevlar Vest", "item_kevlar_vest",
      "Lightweight kevlar provides reasonable protection and good mobility.",
      "media assets/item icons/icon_kevlar_vest.png",
      SLOT_ARMOUR, 140, new Object[] {
        ENGINEERING, 4
      },
      IS_ARMOUR, 0
    ) {
      
    }
  ;
  
  
  //  Body Armour.
  //  Kevlar Vest.
  //  Glide Cape.
  
  //  Sonic Visor.
  //  Telescope.
  //  Terminal.
  
  //  Batarangs.
  //  Bolas.
  //  Grapple Gun.
  
  //  Tranq Gun.
  //  Medkit.
  //  Smoke Bombs.
  
}





