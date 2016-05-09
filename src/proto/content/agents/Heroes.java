

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import static proto.game.person.PersonStats.*;



public class Heroes {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    BATMAN = Kind.ofPerson(
      "Batman", "hero_kind_batman", IMG_DIR+"icon_batman.png",
      Kind.TYPE_HERO,
      PERCEPTION, 7 ,
      EVASION   , 7 ,
      QUESTION  , 7 ,
      COMBAT    , 7 ,
      HIT_POINTS, 21,
      WILLPOWER , 21
    )
  ;

  final public static Kind
    ALFRED = Kind.ofPerson(
      "Alfred", "hero_kind_alfred", IMG_DIR+"icon_alfred.png",
      Kind.TYPE_HERO,
      PERCEPTION, 6 ,
      EVASION   , 4 ,
      QUESTION  , 5 ,
      COMBAT    , 5 ,
      HIT_POINTS, 16,
      WILLPOWER , 20
    )
  ;

  final public static Kind
    SWARM = Kind.ofPerson(
      "Swarm", "hero_kind_swarm", IMG_DIR+"icon_swarm.png",
      Kind.TYPE_HERO,
      PERCEPTION, 10,
      EVASION   , 8 ,
      QUESTION  , 0 ,
      COMBAT    , 2 ,
      HIT_POINTS, 8 ,
      WILLPOWER , 8
    )
  ;
  
  
}









