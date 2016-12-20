

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
      BRAINS  , 2 ,
      REFLEXES, 2 ,
      WILL    , 2 ,
      MUSCLE  , 2
    ),
    DOCTOR = Kind.ofPerson(
      "Doctor", "person_kind_doctor", IMG_DIR+"icon_civilian_2.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 7 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      MEDICINE, 7
    ),
    INVENTOR = Kind.ofPerson(
      "Inventor", "person_kind_inventor", IMG_DIR+"icon_civilian_2.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 7 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      ENGINEERING, 7
    ),
    BROKER = Kind.ofPerson(
      "Broker", "person_kind_broker", IMG_DIR+"icon_broker.png",
      "", Civilians.COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 6 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      PERSUADE, 7
    ),
    POLITICIAN = null,
    POLICE     = null
  ;
  
}



