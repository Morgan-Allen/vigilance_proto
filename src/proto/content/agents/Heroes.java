

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
      "The son of Thomas and Martha Wayne, wealthy socialites and "+
      "philanthropists, Bruce Wayne swore to repay their deaths at the hands "+
      "of a criminal by warring upon Gotham's underworld.",
      Kind.TYPE_HERO,
      INTELLECT , 7 ,
      REFLEX    , 7 ,
      SOCIAL    , 7 ,
      STRENGTH  , 7 ,
      HIT_POINTS, 21,
      WILLPOWER , 21,
      
      ENGINEERING  , 6,
      INFORMATICS  , 6,
      PHARMACY     , 6,
      ANATOMY      , 6,
      LAW_N_FINANCE, 5,
      THE_OCCULT   , 1,
      LANGUAGES    , 6,
      QUESTION     , 7,
      DISGUISE     , 7,
      SUASION      , 5,
      STEALTH      , 7,
      SURVEILLANCE , 7,
      VEHICLES     , 7,
      MARKSMAN     , 7,
      INTIMIDATE   , 8,
      GYMNASTICS   , 7,
      CLOSE_COMBAT , 7,
      STAMINA      , 7
    )
  ;
  
  final public static Kind
    HERO_ALFRED = Kind.ofPerson(
      "Alfred", "hero_kind_alfred", IMG_DIR+"icon_alfred.png",
      "Bruce's caretaker, mentor, occasional confidante, and old family "+
      "friend.",
      Kind.TYPE_HERO,
      INTELLECT , 6 ,
      REFLEX    , 5 ,
      SOCIAL    , 6 ,
      STRENGTH  , 3 ,
      HIT_POINTS, 16,
      WILLPOWER , 20,
      
      ENGINEERING  , 2,
      INFORMATICS  , 0,
      PHARMACY     , 3,
      ANATOMY      , 5,
      LAW_N_FINANCE, 6,
      THE_OCCULT   , 0,
      LANGUAGES    , 4,
      QUESTION     , 3,
      DISGUISE     , 4,
      SUASION      , 6,
      STEALTH      , 2,
      SURVEILLANCE , 4,
      VEHICLES     , 6,
      MARKSMAN     , 6,
      INTIMIDATE   , 2,
      GYMNASTICS   , 0,
      CLOSE_COMBAT , 5,
      STAMINA      , 5
    )
  ;
  
  final public static Kind
    HERO_SWARM = Kind.ofPerson(
      "Swarm", "hero_kind_swarm", IMG_DIR+"icon_swarm.png",
      "THE BATS!  THEY GET EVERYWHERE!",
      Kind.TYPE_HERO,
      INTELLECT , 2 ,
      REFLEX    , 8 ,
      SOCIAL    , 0 ,
      STRENGTH  , 2 ,
      HIT_POINTS, 8 ,
      WILLPOWER , 8
    )
  ;
  
  final public static Kind
    HERO_NIGHTWING = Kind.ofPerson(
      "Nightwing", "hero_kind_nightwing", IMG_DIR+"icon_nightwing.png",
      "A former ward of Bruce Wayne, united in his dedication to fighting "+
      "crime but separated by differences over methods and motivation.",
      Kind.TYPE_HERO,
      INTELLECT , 5 ,
      REFLEX    , 6 ,
      SOCIAL    , 7 ,
      STRENGTH  , 6 ,
      HIT_POINTS, 20,
      WILLPOWER , 18,
      
      ENGINEERING  , 3,
      INFORMATICS  , 2,
      PHARMACY     , 3,
      ANATOMY      , 2,
      LAW_N_FINANCE, 0,
      THE_OCCULT   , 0,
      LANGUAGES    , 2,
      QUESTION     , 5,
      DISGUISE     , 6,
      SUASION      , 7,
      STEALTH      , 7,
      SURVEILLANCE , 7,
      VEHICLES     , 7,
      MARKSMAN     , 7,
      INTIMIDATE   , 5,
      GYMNASTICS   , 7,
      CLOSE_COMBAT , 7,
      STAMINA      , 7
    )
  ;
  
  final public static Kind
    HERO_BATGIRL = Kind.ofPerson(
      "Batgirl", "hero_kind_batgirl", IMG_DIR+"icon_batgirl.png",
      "Indoctrinated from birth by her mother, Lady Shiva, to be the perfect "+
      "assassin, Ms. Cain possesses uncanny reflexes and steely resolve but "+
      "is functionally mute and easily manipulated.",
      Kind.TYPE_HERO,
      INTELLECT , 6 ,
      REFLEX    , 8 ,
      SOCIAL    , 2 ,
      STRENGTH  , 4 ,
      HIT_POINTS, 16,
      WILLPOWER , 16,
      
      ENGINEERING  , 0,
      INFORMATICS  , 0,
      PHARMACY     , 0,
      ANATOMY      , 2,
      LAW_N_FINANCE, 0,
      THE_OCCULT   , 0,
      LANGUAGES    , 0,
      QUESTION     , 0,
      DISGUISE     , 4,
      SUASION      , 0,
      STEALTH      , 7,
      SURVEILLANCE , 6,
      VEHICLES     , 6,
      MARKSMAN     , 7,
      INTIMIDATE   , 0,
      GYMNASTICS   , 7,
      CLOSE_COMBAT , 8,
      STAMINA      , 5
    )
  ;
  
  final public static Kind
    HERO_QUESTION = Kind.ofPerson(
      "Question", "hero_kind_question", IMG_DIR+"icon_question.png",
      "Paranoid conspiracist or the smartest guy left in the room?  That is "+
      "the Question.",
      Kind.TYPE_HERO,
      INTELLECT , 8 ,
      REFLEX    , 5 ,
      SOCIAL    , 6 ,
      STRENGTH  , 5 ,
      HIT_POINTS, 16,
      WILLPOWER , 14,
      
      ENGINEERING  , 4,
      INFORMATICS  , 8,
      PHARMACY     , 4,
      ANATOMY      , 6,
      LAW_N_FINANCE, 8,
      THE_OCCULT   , 4,
      LANGUAGES    , 8,
      QUESTION     , 8,
      DISGUISE     , 8,
      SUASION      , 5,
      STEALTH      , 7,
      SURVEILLANCE , 7,
      VEHICLES     , 3,
      MARKSMAN     , 5,
      INTIMIDATE   , 4,
      GYMNASTICS   , 4,
      CLOSE_COMBAT , 5,
      STAMINA      , 4
    )
  ;
  
  
}









