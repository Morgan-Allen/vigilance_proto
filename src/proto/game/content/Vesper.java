

package proto.game.content;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;

import static proto.game.content.Common.*;
import static proto.game.person.Ability.*;
import static proto.game.person.Person.*;

import java.awt.Image;



public class Vesper {
  
  final public static Ability
    
    BATARANG = new Ability(
      "Batarang", "ability_batarang",
      "Throw a Batarang for mild damage and 66% chance to disarm. (Note that "+
      "strong opponents may resist disarming.)",
      IS_RANGED, 1, MINOR_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return target instanceof Person;
      }
      
      
      protected Volley createVolley(Action use) {
        Scene  scene  = use.acting.currentScene();
        Person struck = (Person) use.target;
        Volley volley = new Volley();
        volley.setupVolley(use.acting, struck, true, scene);
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
    VESPER = Kind.ofPerson(
      "Nocturne", "hero_kind_vesper", IMG_DIR+"sprite_batman.png",
      Kind.TYPE_HERO,
      HIT_POINTS, 30 ,
      ARMOUR, 2  ,
      MUSCLE, 20 ,
      BRAIN , 20 ,
      SPEED_ACT , 20 ,
      SIGHT , 10 ,
      MOVE, 1, STRIKE, 1, EVASION, 1, GUARD, 1,
      BATARANG, 1, ULTRASOUND, 1
    ),
    KESTREL  = Kind.ofPerson(
      "Kestrel", "hero_kind_kestrel", IMG_DIR+"sprite_robin.png",
      Kind.TYPE_HERO, 
      HIT_POINTS, 20 ,
      ARMOUR, 0  ,
      MUSCLE, 16 ,
      BRAIN , 15 ,
      SPEED_ACT , 18 ,
      SIGHT , 12 ,
      MOVE, 1, STRIKE, 1, EVASION, 1, GUARD, 1,
      BATARANG, 1
    );
}






