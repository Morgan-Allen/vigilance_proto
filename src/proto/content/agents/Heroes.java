

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.content.items.*;
import static proto.game.person.PersonStats.*;



public class Heroes {
  
  
  final static String IMG_DIR = "media assets/character icons/heroes/";
  
  
  final public static Faction JANUS_INDUSTRIES = new Faction(
    "Janus Industries", "faction_janus_industries", false
  );
  
  
  final public static PersonType
    HERO_PHOBOS = new PersonType(
      "Phobos", "hero_kind_phobos", IMG_DIR+"icon_phobos.png",
      "<phobos description>",
      null, Techniques.CORE_TECHNIQUES,
      Kind.SUBTYPE_HERO,
      BRAINS  , 8 ,
      REFLEXES, 8 ,
      WILL    , 8 ,
      MUSCLE  , 6 ,
      
      ENGINEERING  , 8,
      MEDICINE     , 8,
      QUESTION     , 7,
      PERSUADE     , 5,
      
      Techniques.STEADY_AIM, 1,
      Techniques.SPRINTER  , 1,
      Techniques.EVASION   , 1,
      
      Weapons.WING_BLADES,
      Armours.KEVLAR_VEST
    )
  ;
  
  final public static PersonType
    HERO_DEIMOS = new PersonType(
      "Deimos", "hero_kind_deimos", IMG_DIR+"icon_deimos.png",
      "<deimos description>",
      null, Techniques.CORE_TECHNIQUES,
      Kind.SUBTYPE_HERO,
      BRAINS  , 6 ,
      REFLEXES, 8 ,
      WILL    , 8 ,
      MUSCLE  , 8 ,
      
      ENGINEERING  , 6,
      MEDICINE     , 6,
      QUESTION     , 5,
      PERSUADE     , 7,
      
      Techniques.FLESH_WOUND, 1,
      Techniques.BRAWLER    , 1,
      Techniques.ENDURANCE  , 1,
      
      Weapons.REVOLVER,
      Armours.BODY_ARMOUR
    )
  ;
  
  final public static PersonType
    HERO_NIGHT_SWIFT = new PersonType(
      "Night Swift", "hero_kind_night_swift", IMG_DIR+"icon_night_swift.png",
      "<night swift description>",
      null, Techniques.CORE_TECHNIQUES,
      Kind.SUBTYPE_HERO,
      BRAINS  , 6 ,
      REFLEXES, 6 ,
      WILL    , 6 ,
      MUSCLE  , 6 ,
      
      ENGINEERING  , 3,
      MEDICINE     , 3,
      QUESTION     , 7,
      PERSUADE     , 8,
      
      Techniques.ENDURANCE, 1,
      Techniques.BRAWLER  , 1,
      Techniques.DISARM   , 1,
      
      Weapons.WING_BLADES,
      Armours.BODY_ARMOUR
    )
  ;
  
  final public static PersonType
    HERO_DR_YANG = new PersonType(
      "Dr. Yang", "hero_kind_dr_yang", IMG_DIR+"icon_dr_yang.png",
      "<dr. yang description>",
      null, Techniques.CORE_TECHNIQUES,
      Kind.SUBTYPE_HERO,
      BRAINS  , 8 ,
      REFLEXES, 5 ,
      WILL    , 6 ,
      MUSCLE  , 5 ,
      
      ENGINEERING  , 6,
      MEDICINE     , 6,
      QUESTION     , 8,
      PERSUADE     , 5,
      
      Techniques.STEADY_AIM, 1,
      Techniques.VIGILANCE , 1,
      Techniques.SPRINTER  , 1,
      
      Weapons.REVOLVER,
      Armours.KEVLAR_VEST
    )
  ;
  
  
  
  //  TODO:  I'm taking these out for the moment...
  /*
  
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









