

package proto.content.agents;
import proto.common.*;
import static proto.game.person.PersonStats.*;



public class Villains {
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    FALCONE  = Kind.ofPerson(
      "Falcone", "person_kind_falcone", IMG_DIR+"icon_falcone.png",
      "", null,
      Kind.SUBTYPE_BOSS,
      INTELLECT , 6 ,
      REFLEX    , 5 ,
      SOCIAL    , 7 ,
      STRENGTH  , 5 ,
      HIT_POINTS, 14,
      WILLPOWER , 15
    ),
    TWO_FACE = Kind.ofPerson(
      "Two-Face", "person_kind_two_face", IMG_DIR+"icon_two_face.png",
      "", null,
      Kind.SUBTYPE_BOSS,
      INTELLECT , 8 ,
      REFLEX    , 7 ,
      SOCIAL    , 5 ,
      STRENGTH  , 7 ,
      HIT_POINTS, 16,
      WILLPOWER , 15
    )
  ;
}
