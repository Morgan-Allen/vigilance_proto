

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
      BRAINS  , 6 ,
      REFLEXES, 5 ,
      WILL    , 7 ,
      MUSCLE  , 5 ,
      
      QUESTION, 6 ,
      PERSUADE, 7
    ),
    TWO_FACE = Kind.ofPerson(
      "Two-Face", "person_kind_two_face", IMG_DIR+"icon_two_face.png",
      "", null,
      Kind.SUBTYPE_BOSS,
      BRAINS  , 8 ,
      REFLEXES, 7 ,
      WILL    , 5 ,
      MUSCLE  , 7 ,
      
      QUESTION, 8 ,
      PERSUADE, 6
    )
  ;
}
