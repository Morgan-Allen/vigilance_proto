

package proto.content.agents;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.Common;

import static proto.game.person.PersonStats.*;



public class Villains {
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static PersonType
    MORETTI  = new PersonType(
      "Moretti", "villain_kind_moretti", IMG_DIR+"icon_moretti.png",
      "<moretti description>", null, null,
      Kind.SUBTYPE_BOSS,
      BRAINS  , 8 ,
      REFLEXES, 5 ,
      WILL    , 7 ,
      MUSCLE  , 3 ,
      
      QUESTION, 5 ,
      PERSUADE, 8,
      
      //  TODO:  Allow for more explicit reference later...
      Common.HAIR_COLOURS[2],
      Common.EYE_COLOURS[0],
      Common.BUILD[1],
      Common.RACES[3],
      Common.SEX[1]
    ),
    SNAKE_EYES = new PersonType(
      "Snake Eyes", "villain_kind_snake_eyes", IMG_DIR+"icon_snake_eyes.png",
      "<snake-eyes description>", null, null,
      Kind.SUBTYPE_BOSS,
      BRAINS  , 7 ,
      REFLEXES, 7 ,
      WILL    , 5 ,
      MUSCLE  , 6 ,
      
      QUESTION, 8 ,
      PERSUADE, 4 ,
      
      //  TODO:  Allow for more explicit reference later...
      Common.HAIR_COLOURS[4],
      Common.EYE_COLOURS[1],
      Common.BUILD[2],
      Common.RACES[3],
      Common.SEX[0]
    )
  ;
}









