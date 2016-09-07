

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class Crooks {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  
  final public static Kind
    FALCONE  = Kind.ofPerson(
      "Falcone", "person_kind_falcone", IMG_DIR+"icon_falcone.png",
      "",
      Kind.TYPE_BOSS,
      INTELLECT , 6 ,
      REFLEX    , 5 ,
      SOCIAL    , 7 ,
      STRENGTH  , 5 ,
      HIT_POINTS, 14,
      WILLPOWER , 15
    ),
    TWO_FACE = Kind.ofPerson(
      "Two-Face", "person_kind_two_face", IMG_DIR+"icon_two_face.png",
      "",
      Kind.TYPE_BOSS,
      INTELLECT , 8 ,
      REFLEX    , 7 ,
      SOCIAL    , 5 ,
      STRENGTH  , 7 ,
      HIT_POINTS, 16,
      WILLPOWER , 15
    )
  ;
  
  final public static Kind
    MOBSTER = Kind.ofPerson(
      "Mobster", "person_kind_mobster", IMG_DIR+"icon_mobster.png",
      "",
      Kind.TYPE_MOOK,
      INTELLECT , 6 ,
      REFLEX    , 6 ,
      SOCIAL    , 6 ,
      STRENGTH  , 6 ,
      HIT_POINTS, 12,
      WILLPOWER , 10
    ),
    GOON = Kind.ofPerson(
      "Goon", "person_kind_goon", IMG_DIR+"icon_goon.png",
      "",
      Kind.TYPE_MOOK,
      INTELLECT , 4 ,
      REFLEX    , 4 ,
      SOCIAL    , 4 ,
      STRENGTH  , 4 ,
      HIT_POINTS, 10,
      WILLPOWER , 8
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
      WILLPOWER , 4 
    ),
    BROKER = Kind.ofPerson(
      "Broker", "person_kind_broker", IMG_DIR+"icon_broker.png",
      "",
      Kind.TYPE_CIVILIAN,
      INTELLECT , 6 ,
      REFLEX    , 3 ,
      SOCIAL    , 5 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 6 ,
      WILLPOWER , 9
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
  
  
  public static Person randomOfKind(Kind kind, World world) {
    Person person = new Person(kind, world, randomCommonName());
    return person;
  }
  
  
}








