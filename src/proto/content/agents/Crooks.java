

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  
  final public static Kind
    GOON = Kind.ofPerson(
      "Goon", "person_kind_goon", IMG_DIR+"icon_goon.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_MOOK,
      INTELLECT , 4 ,
      REFLEX    , 4 ,
      SOCIAL    , 4 ,
      STRENGTH  , 4 ,
      HIT_POINTS, 10,
      WILLPOWER , 8
    ),
    MOBSTER = Kind.ofPerson(
      "Mobster", "person_kind_mobster", IMG_DIR+"icon_mobster.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_MOOK,
      INTELLECT , 6 ,
      REFLEX    , 6 ,
      SOCIAL    , 6 ,
      STRENGTH  , 6 ,
      HIT_POINTS, 12,
      WILLPOWER , 10
    ),
    BROKER = Kind.ofPerson(
      "Broker", "person_kind_broker", IMG_DIR+"icon_broker.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      INTELLECT , 6 ,
      REFLEX    , 3 ,
      SOCIAL    , 5 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 9
    ),
    HITMAN = null,
    PSYCHO = null
  ;
}








