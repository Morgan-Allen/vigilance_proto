

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
      BRAINS  , 2 ,
      REFLEXES, 4 ,
      WILL    , 4 ,
      MUSCLE  , 8
    ),
    MOBSTER = Kind.ofPerson(
      "Mobster", "person_kind_mobster", IMG_DIR+"icon_mobster.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_MOOK,
      BRAINS  , 4 ,
      REFLEXES, 6 ,
      WILL    , 4 ,
      MUSCLE  , 6
    ),
    HITMAN = null,
    PSYCHO = null
  ;
}








