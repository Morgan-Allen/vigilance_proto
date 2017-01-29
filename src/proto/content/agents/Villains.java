

package proto.content.agents;
import proto.common.*;
import static proto.game.person.PersonStats.*;



public class Villains {
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    MORETTI  = Kind.ofPerson(
      "Moretti", "villain_kind_moretti", IMG_DIR+"icon_moretti.png",
      "<moretti description>", null,
      Kind.SUBTYPE_BOSS,
      BRAINS  , 8 ,
      REFLEXES, 5 ,
      WILL    , 7 ,
      MUSCLE  , 3 ,
      
      QUESTION, 5 ,
      PERSUADE, 8
    ),
    SNAKE_EYES = Kind.ofPerson(
      "Snake Eyes", "villain_kind_snake_eyes", IMG_DIR+"icon_snake_eyes.png",
      "<snake-eyes description>", null,
      Kind.SUBTYPE_BOSS,
      BRAINS  , 7 ,
      REFLEXES, 7 ,
      WILL    , 5 ,
      MUSCLE  , 6 ,
      
      QUESTION, 8 ,
      PERSUADE, 4
    )
  ;
}









