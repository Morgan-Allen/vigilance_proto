

package proto.game.person;
import static proto.game.person.Person.*;



public class Common {
  
  
  final static String IMG_DIR = "media assets/hero sprites/";
  
  
  final public static Equipped
    UNARMED = new Equipped(
      "Unarmed", "item_unarmed",
      "Bare fists and moxy.",
      null,
      SLOT_WEAPON, 0,
      Equipped.IS_WEAPON | Equipped.IS_MELEE | Equipped.IS_KINETIC, 0
    ),
    UNARMOURED = new Equipped(
      "Unarmoured", "item_unarmoured",
      "Nothin' but the clothes on your back.",
      null,
      SLOT_ARMOUR, 0,
      Equipped.IS_ARMOUR, 0
    );
  
}














