

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.content.items.*;
import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/common/";
  
  
  final public static Faction THE_MADE_MEN = new Faction(
    "The Made Men", "faction_the_made_men", true
  );
  
  
  final public static PersonType
    BRUISER = new PersonType(
      "Bruiser", "person_kind_bruiser", IMG_DIR+"icon_bruiser.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_MOOK,
      BRAINS  , 2 ,
      REFLEXES, 4 ,
      WILL    , 4 ,
      MUSCLE  , 8 ,
      Weapons.BRASS_KNUCKLES, Weapons.BASEBALL_BAT
    ),
    GANGSTER = new PersonType(
      "Gangster", "person_kind_gangster", IMG_DIR+"icon_gangster.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_MOOK,
      BRAINS  , 4 ,
      REFLEXES, 6 ,
      WILL    , 4 ,
      MUSCLE  , 6 ,
      Weapons.REVOLVER, Weapons.AUTOMATIC
    ),
    HITMAN = new PersonType(
      "Hitman", "person_kind_hitman", IMG_DIR+"icon_gangster.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_MOOK,
      BRAINS  , 6 ,
      REFLEXES, 8 ,
      WILL    , 6 ,
      MUSCLE  , 6 ,
      Weapons.REVOLVER, Weapons.RIFLE
    ),
    PSYCHO = null
  ;
}







