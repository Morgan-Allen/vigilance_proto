
package proto.content.items;
import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;
import static proto.game.person.PersonStats.*;
import proto.game.person.*;



public class Clues {
  
  
  final static String
    ICONS_DIR = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  final public static ItemType EVIDENCE = new ItemType(
    "Evidence", "item_evidence",
    "",
    ICONS_DIR+"icon_evidence.png",
    SPRITE_DIR+null,
    SLOT_ITEMS, 0, new Object[] {},
    IS_CUSTOM, 0
  ) {
  };
  
  final public static ItemType BLUEPRINT = new ItemType(
    "Blueprint", "item_blueprint",
    "",
    ICONS_DIR+"icon_blueprint.png",
    SPRITE_DIR+null,
    SLOT_ITEMS, 0, new Object[] {},
    IS_CUSTOM, 0
  ) {
  };
  
  final public static ItemType BOMB = new ItemType(
    "Bomb", "item_bomb",
    "",
    ICONS_DIR+"icon_bomb.png",
    SPRITE_DIR+null,
    SLOT_ITEMS, 0, new Object[] {},
    IS_CONSUMED, 0
  ) {
  };
  
  final public static ItemType CASH = new ItemType(
    "Cash", "item_cash",
    "",
    ICONS_DIR+"icon_cash.png",
    SPRITE_DIR+null,
    SLOT_ITEMS, 0, new Object[] {},
    IS_CONSUMED, 0
  ) {
  };
}








