

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
      PERSUADE, 8 ,
      
      Common.HAIR_BROWN,
      Common.EYES_GREY,
      Common.BUILD_SKINNY,
      Common.RACE_WHITE,
      Common.SEX_FEMALE
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
      
      Common.HAIR_WHITE,
      Common.EYES_GREEN,
      Common.BUILD_TALL,
      Common.RACE_WHITE,
      Common.SEX_MALE
    )
  ;
}









