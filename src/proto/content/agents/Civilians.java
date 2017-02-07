

package proto.content.agents;
import proto.common.*;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;



public class Civilians {
  
  
  final public static String
    COMMON_FIRST_NAMES[] = {
      "Jerry", "Stan", "Louis", "Abed", "Nico", "Zoe"
    },
    COMMON_LAST_NAMES[] = {
      "Stanfeld", "Turner", "Lewis", "Walker", "Bryant", "Cole"
    },
    COMMON_NAMES[][] = { COMMON_FIRST_NAMES, COMMON_LAST_NAMES };
  
  
  final static String IMG_DIR = "media assets/character icons/common/";
  
  final public static PersonType
    CIVILIAN = new PersonType(
      "Civilian", "person_kind_civilian", IMG_DIR+"icon_civilian.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 2 ,
      REFLEXES, 2 ,
      WILL    , 2 ,
      MUSCLE  , 2
    ),
    DOCTOR = new PersonType(
      "Doctor", "person_kind_doctor", IMG_DIR+"icon_doctor.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 7 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      MEDICINE, 7
    ),
    INVENTOR = new PersonType(
      "Inventor", "person_kind_inventor", IMG_DIR+"icon_inventor.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 7 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      ENGINEERING, 7
    ),
    BROKER = new PersonType(
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



