

package proto.game.person;
import static proto.game.person.Equipped.*;
import static proto.game.person.PersonGear.*;



public class Common {
  
  
  final static String IMG_DIR = "media assets/hero sprites/";
  
  
  final public static Equipped
    UNARMED = new Equipped(
      "Unarmed", "item_unarmed",
      "Bare fists and moxy.",
      null,
      SLOT_WEAPON, 0, new Object[0],
      IS_WEAPON | IS_MELEE | IS_KINETIC, 0
    ),
    UNARMOURED = new Equipped(
      "Unarmoured", "item_unarmoured",
      "Nothin' but the clothes on your back.",
      null,
      SLOT_ARMOUR, 0, new Object[0],
      IS_ARMOUR, 0
    );
  
}














