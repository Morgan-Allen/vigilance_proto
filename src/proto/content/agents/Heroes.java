

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import static proto.game.person.Common.*;
import static proto.game.person.PersonStats.*;



public class Heroes {
  
  
  final static String IMG_DIR = "media assets/character icons/heroes/";
  
  final public static Kind
    HERO_PHOBOS = Kind.ofPerson(
      "Phobos", "hero_kind_phobos", IMG_DIR+"icon_phobos.png",
      "<phobos description>",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 8 ,
      REFLEXES, 6 ,
      WILL    , 8 ,
      MUSCLE  , 6 ,
      
      ENGINEERING  , 8,
      MEDICINE     , 8,
      QUESTION     , 7,
      PERSUADE     , 5
    )
  ;
  
  final public static Kind
    HERO_DEIMOS = Kind.ofPerson(
      "Deimos", "hero_kind_deimos", IMG_DIR+"icon_deimos.png",
      "<deimos description>",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 6 ,
      REFLEXES, 8 ,
      WILL    , 6 ,
      MUSCLE  , 8 ,
      
      ENGINEERING  , 6,
      MEDICINE     , 6,
      QUESTION     , 5,
      PERSUADE     , 7
    )
  ;
  
  final public static Kind
    HERO_NIGHT_SWIFT = Kind.ofPerson(
      "Night Swift", "hero_kind_night_swift", IMG_DIR+"icon_night_swift.png",
      "<night swift description>",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 6 ,
      REFLEXES, 6 ,
      WILL    , 6 ,
      MUSCLE  , 6 ,
      
      ENGINEERING  , 3,
      MEDICINE     , 3,
      QUESTION     , 7,
      PERSUADE     , 8
    )
  ;
  
  final public static Kind
    HERO_DR_YANG = Kind.ofPerson(
      "Dr. Yang", "hero_kind_dr_yang", IMG_DIR+"icon_dr_yang.png",
      "<dr. yang description>",
      null,
      Kind.SUBTYPE_HERO,
      BRAINS  , 8 ,
      REFLEXES, 5 ,
      WILL    , 6 ,
      MUSCLE  , 5 ,
      
      ENGINEERING  , 6,
      MEDICINE     , 6,
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









