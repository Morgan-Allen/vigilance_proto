
package proto.content.items;
import static proto.game.person.Equipped.*;
import static proto.game.person.PersonGear.*;
import static proto.game.person.PersonStats.*;
import proto.game.person.*;



public class Clues {
  
  
  final static String ITEM_IMG_DIR = "media assets/item icons/";
  
  final public static Equipped EVIDENCE = new Equipped(
    "Evidence", "item_evidence",
    "",
    ITEM_IMG_DIR+"icon_evidence.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CUSTOM, 0
  ) {
  };
  
  final public static Equipped BLUEPRINT = new Equipped(
    "Blueprint", "item_blueprint",
    "",
    ITEM_IMG_DIR+"icon_blueprint.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CUSTOM, 0
  ) {
  };
  
  final public static Equipped BOMB = new Equipped(
    "Bomb", "item_bomb",
    "",
    ITEM_IMG_DIR+"icon_bomb.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CONSUMED, 0
  ) {
  };
  
  final public static Equipped CASH = new Equipped(
    "Cash", "item_cash",
    "",
    ITEM_IMG_DIR+"icon_cash.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CONSUMED, 0
  ) {
  };
}








