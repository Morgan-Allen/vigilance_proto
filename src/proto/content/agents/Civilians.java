

package proto.content.agents;
import static proto.game.person.PersonStats.*;
import proto.common.Kind;



public class Civilians {
  
  
  final public static String
    COMMON_FIRST_NAMES[] = {
      "Jerry", "Stan", "Louis", "Abed", "Nico", "Zoe"
    },
    COMMON_LAST_NAMES[] = {
      "Stanfeld", "Turner", "Lewis", "Walker", "Bryant", "Cole"
    },
    COMMON_NAMES[][] = { COMMON_FIRST_NAMES, COMMON_LAST_NAMES };
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    CIVILIAN = Kind.ofPerson(
      "Civilian", "person_kind_civilian", IMG_DIR+"icon_civilian_2.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      INTELLECT , 2 ,
      REFLEX    , 2 ,
      SOCIAL    , 2 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 4 
    ),
    DOCTOR     = null,
    INVENTOR   = null,
    POLITICIAN = null,
    POLICE     = null
  ;
  
}



