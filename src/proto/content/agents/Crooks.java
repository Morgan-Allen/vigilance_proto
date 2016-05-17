

package proto.content.agents;
import proto.common.*;
import proto.util.Rand;

import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    MOBSTER = Kind.ofPerson(
      "Mobster", "person_kind_mobster", IMG_DIR+"icon_mobster.png",
      Kind.TYPE_MOOK,
      PERCEPTION, 4 ,
      EVASION   , 4 ,
      SOCIAL  , 4 ,
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
      SOCIAL  , 2 ,
      COMBAT    , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 4
    )
  ;
  
  final public static String
    COMMON_FIRST_NAMES[] = {
      "Jerry", "Stan", "Louis", "Abed", "Nico", "Zoe"
    },
    COMMON_LAST_NAMES[] = {
      "Stanfeld", "Turner", "Lewis", "Walker", "Bryant", "Cole"
    };
  
  public static String randomCommonName() {
    return
      Rand.pickFrom(COMMON_FIRST_NAMES)+" "+Rand.pickFrom(COMMON_LAST_NAMES);
  }
  
  
}








