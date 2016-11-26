
package proto.content.items;
import static proto.game.person.Equipped.*;
import static proto.game.person.PersonGear.*;
import static proto.game.person.PersonStats.*;
import proto.game.person.*;



public class Clues {
  
  
  final public static Equipped EVIDENCE = new Equipped(
    "Evidence", "item_evidence",
    "",
    "media assets/item icons/icon_evidence.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CUSTOM, 0
  ) {
  };
  
  final public static Equipped BLUEPRINT = new Equipped(
    "Blueprint", "item_blueprint",
    "",
    "media assets/item icons/icon_blueprint.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CUSTOM, 0
  ) {
  };
  
  final public static Equipped BOMB = new Equipped(
    "Bomb", "item_bomb",
    "",
    "media assets/item icons/icon_bomb.png",
    SLOT_ITEMS, 0, new Object[] {},
    IS_CONSUMED, 0
  ) {
  };
}



