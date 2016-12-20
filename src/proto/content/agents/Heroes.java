

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import static proto.game.person.Common.*;
import static proto.game.person.PersonStats.*;



public class Heroes {
  
  
  final static String IMG_DIR = "media assets/character icons/";
  
  final public static Kind
    HERO_BATMAN = Kind.ofPerson(
      "Batman", "hero_kind_batman", IMG_DIR+"icon_batman.png",
      "The son of Thomas and Martha Wayne, wealthy socialites and "+
      "philanthropists, Bruce Wayne swore to repay their deaths at the hands "+
      "of a criminal by warring upon Gotham's underworld.",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 8 ,
      REFLEXES, 8 ,
      WILL    , 8 ,
      MUSCLE  , 8 ,
      
      ENGINEERING  , 6,
      MEDICINE     , 6,
      QUESTION     , 7,
      PERSUADE     , 5
    )
  ;
  
  final public static Kind
    HERO_NIGHTWING = Kind.ofPerson(
      "Nightwing", "hero_kind_nightwing", IMG_DIR+"icon_nightwing.png",
      "A former ward of Bruce Wayne, united in his dedication to fighting "+
      "crime but separated by differences over methods and motivation.",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 5 ,
      REFLEXES, 6 ,
      WILL    , 7 ,
      MUSCLE  , 6 ,
      
      ENGINEERING  , 3,
      MEDICINE     , 3,
      QUESTION     , 5,
      PERSUADE     , 7
    )
  ;
  
  final public static Kind
    HERO_BATGIRL = Kind.ofPerson(
      "Batgirl", "hero_kind_batgirl", IMG_DIR+"icon_batgirl.png",
      "Indoctrinated from birth by her mother, Lady Shiva, to be the perfect "+
      "assassin, Ms. Cain possesses uncanny reflexes and steely resolve but "+
      "is functionally mute and easily manipulated.",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 6 ,
      REFLEXES, 8 ,
      WILL    , 2 ,
      MUSCLE  , 4 ,
      
      ENGINEERING  , 0,
      MEDICINE     , 0,
      QUESTION     , 0,
      PERSUADE     , 0
    )
  ;
  
  final public static Kind
    HERO_QUESTION = Kind.ofPerson(
      "Question", "hero_kind_question", IMG_DIR+"icon_question.png",
      "Paranoid conspiracist or the smartest guy left in the room?  That is "+
      "the Question.",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 8 ,
      REFLEXES, 5 ,
      WILL    , 6 ,
      MUSCLE  , 5 ,
      
      ENGINEERING  , 4,
      MEDICINE     , 4,
      QUESTION     , 8,
      PERSUADE     , 5
    )
  ;
  
  
  
  //  TODO:  I'm taking these out for the moment...
  
  /*
  final public static Kind
    HERO_ALFRED = Kind.ofPerson(
      "Alfred", "hero_kind_alfred", IMG_DIR+"icon_alfred.png",
      "Bruce's caretaker, mentor, occasional confidante, and old family "+
      "friend.",
      Kind.TYPE_HERO,
      BRAINS , 6 ,
      REFLEXES    , 5 ,
      WILL    , 6 ,
      MUSCLE  , 3 ,
      HIT_POINTS, 16,
      WILLPOWER , 20,
      
      ENGINEERING  , 2,
      INFORMATICS  , 0,
      MEDICINE     , 3,
      ANATOMY      , 5,
      LAW_N_FINANCE, 6,
      THE_OCCULT   , 0,
      LANGUAGES    , 4,
      QUESTION     , 3,
      DISGUISE     , 4,
      PERSUADE      , 6,
      STEALTH      , 2,
      SURVEILLANCE , 4,
      VEHICLES     , 6,
      MARKSMAN     , 6,
      INTIMIDATE   , 2,
      GYMNASTICS   , 0,
      CLOSE_COMBAT , 5,
      STAMINA      , 5,
      
      TRAIT_CIVILIAN, 1
    )
  ;
  
  final public static Kind
    HERO_SWARM = Kind.ofPerson(
      "Swarm", "hero_kind_swarm", IMG_DIR+"icon_swarm.png",
      "THE BATS!  THEY GET EVERYWHERE!",
      Kind.TYPE_HERO,
      BRAINS , 2 ,
      REFLEXES    , 8 ,
      WILL    , 0 ,
      MUSCLE  , 2 ,
      HIT_POINTS, 8 ,
      WILLPOWER , 8 ,
      
      TRAIT_ANIMAL, 1
    )
  ;
  
  final static Ability TRAIT_ANIMAL = new Ability(
    "Animal", "trait_animal", null,
    "This agent is an animal.  They cannot be assigned to train or craft "+
    "items, and may only accompany other agents on physical assignments.",
    Ability.IS_PASSIVE, 0, Ability.NO_HARM, Ability.NO_POWER
  ) {
    public boolean allowsAssignment(Person p, Assignment a) {
      if (a instanceof TaskTrain) return false;
      if (a instanceof TaskCraft) return false;
      if (a instanceof Task) {
        final Task l = (Task) a;
        if (l.mental()) return false;
        
        boolean company = false;
        for (Person o : a.assigned()) if (p != o) company = true;
        if (! company) return false;
        
        return true;
      }
      return false;
    }
  };
  
  
  final static Ability TRAIT_CIVILIAN = new Ability(
    "Civilian", "trait_civilian", null,
    "This agent is a civilian.  They can assist in training, crafting and "+
    "research, but will not become directly involved in investigations.",
    Ability.IS_PASSIVE, 0, Ability.NO_HARM, Ability.NO_POWER
  ) {
    public boolean allowsAssignment(Person p, Assignment a) {
      if (a instanceof TaskTrain) return true;
      if (a instanceof TaskCraft) return true;
      return false;
    }
  };
  //*/
  
  //*/
}









