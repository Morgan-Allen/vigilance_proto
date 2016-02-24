

package proto.game.content;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;

import static proto.game.person.Ability.*;
import static proto.game.person.Common.*;
import static proto.game.person.Person.*;
import static proto.game.person.PersonStats.*;

import java.awt.Image;



public class Banshee {
  
  
  final static String IMG_DIR = "media assets/hero sprites/";
  
  final public static Ability
    
    NINJUTSU = new Ability(
      "Ninjutsu", "ability_ninjutsu",
      "Improves stealth, evasion and accuracy.  Can be used to perform "+
      "silent takedown of unwary foes with 50% bonus damage.",
      IS_ACTIVE | IS_PASSIVE | TRIGGER_ON_ATTACK | TRIGGER_ON_DEFEND,
      1, MINOR_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (! (target instanceof Person)) return false;
        return ! ((Person) target).canNotice(acting);
      }
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        Volley volley = new Volley();
        volley.setupVolley(use.acting, (Person) target, false, scene);
        volley.damagePercent = 150;
        volley.stunPercent   = 75 ;
        return volley;
      }
      
      public void applyOnActionEnd(Action use) {
        Volley volley     = use.volley();
        Person struck     = volley.targAsPerson();
        float  resistance = struck.stats.levelFor(MUSCLE) / 100f;
        
        if (! volley.didConnect) return;
        //if (struck.canNotice(use.acting)) return;
        
        float downChance = 1.0f;
        downChance += use.acting.stats.levelFor(this) / 10f;
        downChance -= resistance;
        
        if (Rand.num() < downChance) {
          I.say(struck+" received a silent takedown!");
          struck.emptyEquipSlot(Person.SLOT_WEAPON);
          struck.receiveStun(struck.maxHealth());
        }
      }
      
      public Trait[] passiveTraitsModified() {
        return new Trait[] { STEALTH, PRECISION, DODGE };
      }
      
      public float passiveModifierFor(Person person, Trait trait) {
        float modBonus = (person.stats.levelFor(this) * 5);
        if (trait == STEALTH  ) return modBonus + 5;
        if (trait == PRECISION) return modBonus + 5;
        if (trait == DODGE    ) return modBonus + 5;
        return 0;
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      public Image missileSprite() { return missile; }
    },
    
    BATSUIT = new Ability(
      "Batsuit", "ability_batsuit",
      "Grants additional Hit Points and Armour- stacks with Powered Armour.",
      IS_PASSIVE, 0, NO_HARM, MINOR_POWER
    ) {
      
      public Trait[] passiveTraitsModified() {
        return new Trait[] { HIT_POINTS, ARMOUR };
      }
      
      public float passiveModifierFor(Person person, Trait trait) {
        final float level = person.stats.levelFor(this);
        if (trait == HIT_POINTS) return level * 5;
        if (trait == ARMOUR    ) return level * 2;
        return 0;
      }
    },
    
    BATARANG = new Ability(
      "Batarang", "ability_batarang",
      "Throw a Batarang for mild damage and 66% chance to disarm. (Note that "+
      "strong opponents may resist.)",
      IS_RANGED, 1, MINOR_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return target instanceof Person;
      }
      
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        Volley volley = new Volley();
        volley.setupVolley(use.acting, (Person) target, true, scene);
        volley.selfDamageBase  = 2;
        volley.selfDamageRange = 2;
        return volley;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Volley volley       = use.volley();
        Person struck       = volley.targAsPerson();
        float  resistance   = struck.stats.levelFor(MUSCLE) / 100f;
        float  disarmChance = 0.66f - resistance;
        
        if (! volley.didConnect) return;
        if (! struck.hasEquipped(SLOT_WEAPON)) return;
        
        if (Rand.num() < disarmChance) {
          I.say(struck+" dropped their weapon!");
          struck.emptyEquipSlot(Person.SLOT_WEAPON);
        }
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_batarang.png");
      public Image missileSprite() { return missile; }
    },
    
    ULTRASOUND = new Ability(
      "Ultrasound", "ability_ultrasound",
      "Reveal hidden areas of the map nearby.",
      IS_RANGED | NO_NEED_SIGHT, 2, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return scene.tileUnder(target) != null;
      }
      
      
      public int maxRange() {
        return 5;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Scene scene = use.acting.currentScene();
        Tile under = scene.tileUnder(use.target);
        scene.liftFogAround(under, 5, use.acting, false);
      }
    };
  
  
  
  final public static Kind
    BANSHEE = Kind.ofPerson(
      "Banshee", "hero_kind_banshee", IMG_DIR+"sprite_batman.png",
      Kind.TYPE_HERO,
      MUSCLE, 20 ,
      REFLEX, 20 ,
      BRAIN , 20 ,
      WILL  , 20 ,
      MOVE, 1, STRIKE, 1, EVASION, 1, GUARD, 1,
      NINJUTSU, 2, BATSUIT, 1, BATARANG, 1, ULTRASOUND, 1
    ),
    SWIFT = Kind.ofPerson(
      "Swift", "hero_kind_swift", IMG_DIR+"sprite_robin.png",
      Kind.TYPE_HERO, 
      MUSCLE, 16 ,
      REFLEX, 18 ,
      BRAIN , 15 ,
      WILL  , 17 ,
      MOVE, 1, STRIKE, 1, EVASION, 1, GUARD, 1,
      NINJUTSU, 1, BATARANG, 1
    );
}






