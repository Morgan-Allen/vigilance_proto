

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;



public class Heroes {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    HERO_BATMAN = Kind.ofPerson(
      "Batman", "hero_kind_batman", IMG_DIR+"icon_batman.png",
      Kind.TYPE_HERO,
      PERCEPTION, 7 ,
      EVASION   , 7 ,
      SOCIAL    , 7 ,
      COMBAT    , 7 ,
      HIT_POINTS, 21,
      WILLPOWER , 21
    )
  ;

  final public static Kind
    HERO_ALFRED = Kind.ofPerson(
      "Alfred", "hero_kind_alfred", IMG_DIR+"icon_alfred.png",
      Kind.TYPE_HERO,
      PERCEPTION, 6 ,
      EVASION   , 4 ,
      SOCIAL    , 5 ,
      COMBAT    , 5 ,
      HIT_POINTS, 16,
      WILLPOWER , 20
    )
  ;

  final public static Kind
    HERO_SWARM = Kind.ofPerson(
      "Swarm", "hero_kind_swarm", IMG_DIR+"icon_swarm.png",
      Kind.TYPE_HERO,
      PERCEPTION, 10,
      EVASION   , 8 ,
      SOCIAL    , 0 ,
      COMBAT    , 2 ,
      HIT_POINTS, 8 ,
      WILLPOWER , 8
    )
  ;

  final public static Kind
    HERO_NIGHTWING = Kind.ofPerson(
      "Nightwing", "hero_kind_nightwing", IMG_DIR+"icon_nightwing.png",
      Kind.TYPE_HERO,
      PERCEPTION, 5 ,
      EVASION   , 6 ,
      SOCIAL    , 5 ,
      COMBAT    , 6 ,
      HIT_POINTS, 20,
      WILLPOWER , 18
    )
  ;
  
  final public static Kind
    HERO_BATGIRL = Kind.ofPerson(
      "Batgirl", "hero_kind_batgirl", IMG_DIR+"icon_batgirl.png",
      Kind.TYPE_HERO,
      PERCEPTION, 6 ,
      EVASION   , 8 ,
      SOCIAL    , 2 ,
      COMBAT    , 7 ,
      HIT_POINTS, 16,
      WILLPOWER , 16
    )
  ;
  
  final public static Kind
    HERO_QUESTION = Kind.ofPerson(
      "Question", "hero_kind_question", IMG_DIR+"icon_question.png",
      Kind.TYPE_HERO,
      PERCEPTION, 8 ,
      EVASION   , 5 ,
      SOCIAL    , 7 ,
      COMBAT    , 5 ,
      HIT_POINTS, 16,
      WILLPOWER , 14
    )
  ;
  
  
}









