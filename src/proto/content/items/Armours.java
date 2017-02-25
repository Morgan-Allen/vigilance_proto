

package proto.content.items;
import proto.common.*;
import proto.game.person.*;
import static proto.game.person.ItemType   .*;
import static proto.game.person.PersonGear .*;
import static proto.game.person.PersonStats.*;



public class Armours {
  

  final static String
    ICONS_DIR  = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  final public static ItemType BODY_ARMOUR = new ItemType(
    "Body Armour", "item_body_armour",
    "Heavy ceramic body armour.  Grants excellent protection but impedes "+
    "stealth.",
    ICONS_DIR+"icon_body_armour.png",
    SPRITE_DIR+"sprite_deflect.png",
    Kind.SUBTYPE_ARMOUR, SLOT_TYPE_ARMOUR,
    200, new Object[] { ENGINEERING, 4 },
    IS_ARMOUR
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == ARMOUR    ) return  5;
      if (trait == HEALTH    ) return  5;
      if (trait == HIDE_RANGE) return -3;
      return 0;
    }
  };
  
  final public static ItemType KEVLAR_VEST = new ItemType(
    "Kevlar Vest", "item_kevlar_vest",
    "Lightweight kevlar provides reasonable protection and good mobility.",
    ICONS_DIR+"icon_kevlar_vest.png",
    SPRITE_DIR+"sprite_deflect.png",
    Kind.SUBTYPE_ARMOUR, SLOT_TYPE_ARMOUR, 140,
    new Object[] { ENGINEERING, 4 },
    IS_ARMOUR
  ) {
    public float passiveModifierFor(Person person, Trait trait) {
      if (trait == ARMOUR) return 2;
      if (trait == HEALTH) return 2;
      return 0;
    }
  };
  
}



