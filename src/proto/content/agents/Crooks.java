

package proto.content.agents;
import proto.common.*;
import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    MOBSTER = Kind.ofPerson(
      "Mobster", "person_kind_mobster", IMG_DIR+"icon_mobster.png",
      Kind.TYPE_MOOK,
      PERCEPTION, 4 ,
      EVASION   , 4 ,
      QUESTION  , 4 ,
      COMBAT    , 4 ,
      HIT_POINTS, 10,
      WILLPOWER , 8
    )
  ;
  
  final public static Kind
    CIVILIAN = Kind.ofPerson(
      "Civilian", "person_kind_civilian", IMG_DIR+"icon_civilian_2.png",
      Kind.TYPE_CIVILIAN,
      PERCEPTION, 2 ,
      EVASION   , 2 ,
      QUESTION  , 2 ,
      COMBAT    , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 4
    )
  ;
}








