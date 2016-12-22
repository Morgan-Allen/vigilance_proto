

package proto.content.agents;
import proto.game.person.*;
import proto.game.scene.*;

import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;
import static proto.game.person.PersonStats.*;


/*
Couple of ideas for the moment...

Close Combat
  Punisher
    Joint Break
  Disarm
    Sleeper Hold
  Block & Counter
    Long Throw

Observation
  Evasion
    Low Profile
  Vigilance
    Steady Aim
  Rapid Volley
    Parting Shot

Stamina
  Diehard
    Iron Will
  Chest Work
    Wrestler
  Leg Day
    Long Jump
//*/



public class Techniques {
  
  final static String
    ICONS_DIR  = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  final public static Ability
    
    PUNISHMENT = new Ability(
      "Punishment", "ability_punishment",
      SPRITE_DIR+"sprite_punch.png",
      "Deals additional damage with increased risk of lethality and chance to "+
      "stun.",
      Ability.IS_MELEE, 1, Ability.REAL_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (target instanceof Person) {
          final Person other = (Person) target;
          return other.isEnemy(acting);
        }
        return false;
      }
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        int level = use.acting.stats.levelFor(this);
        Volley volley = new Volley();
        volley.setupVolley(use.acting, (Person) target, false, scene);
        volley.selfDamageRange += 1 + level;
        volley.stunPercent = 20;
        
        //  TODO:  Include actual stun effects!
        return volley;
      }
    },
    
    DISARM = new Ability(
      "Disarm", "ability_disarm",
      SPRITE_DIR+"sprite_punch.png",
      "Deals slightly reduced, but entirely nonlethal damage, with a chance "+
      "to remove the target's weapon.",
      Ability.IS_MELEE, 1, Ability.REAL_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (target instanceof Person) {
          final Person other = (Person) target;
          return other.isEnemy(acting);
        }
        return false;
      }
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        Volley volley = new Volley();
        volley.setupVolley(use.acting, (Person) target, false, scene);
        volley.selfDamageBase *= 0.75f;
        volley.selfDamageRange *= 0.75f;
        volley.stunPercent = 100;
        
        //  TODO:  Include actual weapon removal!
        return volley;
      }
    },
    
    EVASION = new Ability(
      "Evasion", "ability_evasion",
      SPRITE_DIR+"move.png",
      "Reserve AP to increase chance to dodge enemy attacks by at least 20%. "+
      "Ends turn.",
      Ability.IS_DELAYED | Ability.TRIGGER_ON_DEFEND, 1,
      Ability.NO_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return acting instanceof Person;
      }
      
      public void applyOnDefendStart(Volley volley) {
        Person self = volley.targAsPerson();
        Person hits = volley.origAsPerson();
        int level = self.stats.levelFor(this);
        
        FX.dodgePosition(self, hits, 0.33f);
        volley.hitsDefence += level * 5;
        volley.hitsDefence += 20 + (self.actions.currentAP() * 5);
      }
      
      public void applyOnDefendEnd(Volley volley) {
        Person self = volley.targAsPerson();
        Tile at = self.currentTile();
        self.setExactPosition(at.scene, at.x, at.y, 0);
        self.actions.modifyAP(-1);
      }
    },
    
    ENDURANCE = new Ability(
      "Endurance", "ability_endurance",
      SPRITE_DIR+"move.png",
      "Increases health reserves.",
      Ability.IS_PASSIVE, 1,
      Ability.NO_HARM, Ability.MINOR_POWER
    ) {
      public float passiveModifierFor(Person person, Trait trait) {
        int level = person.stats.levelFor(this);
        if (trait == HEALTH) return level * 2;
        return 0;
      }
    },
    
    PHYS_TECHNIQUES[] = { PUNISHMENT, DISARM, EVASION, ENDURANCE };
  
}









