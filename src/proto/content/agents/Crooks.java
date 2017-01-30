

package proto.content.agents;
import proto.common.*;
import proto.content.items.Gadgets;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/common/";
  
  
  final public static Kind
    BRUISER = Kind.ofPerson(
      "Bruiser", "person_kind_bruiser", IMG_DIR+"icon_bruiser.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_MOOK,
      BRAINS  , 2 ,
      REFLEXES, 4 ,
      WILL    , 4 ,
      MUSCLE  , 8
    ),
    GANGSTER = Kind.ofPerson(
      "Gangster", "person_kind_gangster", IMG_DIR+"icon_gangster.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_MOOK,
      BRAINS  , 4 ,
      REFLEXES, 6 ,
      WILL    , 4 ,
      MUSCLE  , 6 ,
      Gadgets.REVOLVER
    ),
    HITMAN = null,
    PSYCHO = null
  ;
}




