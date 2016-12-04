

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
    DOCTOR = Kind.ofPerson(
      "Doctor", "person_kind_doctor", IMG_DIR+"icon_civilian_2.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      INTELLECT , 7 ,
      REFLEX    , 3 ,
      SOCIAL    , 5 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 5 ,
      
      PHARMACY   , 7,
      ANATOMY    , 7
    ),
    INVENTOR = Kind.ofPerson(
      "Inventor", "person_kind_inventor", IMG_DIR+"icon_civilian_2.png",
      "", COMMON_NAMES,
      Kind.SUBTYPE_CIVILIAN,
      INTELLECT , 7 ,
      REFLEX    , 3 ,
      SOCIAL    , 5 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 5 ,
      
      ENGINEERING, 7,
      INFORMATICS, 7
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
      WILLPOWER , 6 ,
      
      INFORMATICS  , 5,
      LAW_N_FINANCE, 5,
      SUASION      , 7
    ),
    POLITICIAN = null,
    POLICE     = null
  ;
  
}



