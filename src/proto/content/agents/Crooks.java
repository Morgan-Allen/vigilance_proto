

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import static proto.game.person.Common.MOVE;
import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    MOBSTER = Kind.ofPerson(
      "Mobster", "person_kind_mobster", IMG_DIR+"icon_mobster.png",
      "",
      Kind.TYPE_MOOK,
      INTELLECT , 4 ,
      REFLEX    , 4 ,
      SOCIAL    , 4 ,
      STRENGTH  , 4 ,
      HIT_POINTS, 10,
      WILLPOWER , 8 ,
      
      MOVE, 1
    )
  ;
  
  final public static Kind
    CIVILIAN = Kind.ofPerson(
      "Civilian", "person_kind_civilian", IMG_DIR+"icon_civilian_2.png",
      "",
      Kind.TYPE_CIVILIAN,
      INTELLECT , 2 ,
      REFLEX    , 2 ,
      SOCIAL    , 2 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 4 ,
      
      MOVE, 1
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
  
  
  public static Person randomCivilian(World world) {
    return new Person(CIVILIAN, world, randomCommonName());
  }
  
  
  public static Person randomMobster(World world) {
    return new Person(MOBSTER, world, randomCommonName());
  }
  
  
}








