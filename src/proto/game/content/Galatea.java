

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



public class Galatea {
  
  
  final static String IMG_DIR = "media assets/hero sprites/";
  
  
  final public static Ability
    
    AEGIS_ARMOUR = new Ability(
      "Aegis Armour", "ability_aegis_armour",
      "Grants additional Hit Points, Armour and Parry bonus.",
      IS_PASSIVE, 0, NO_HARM, MINOR_POWER
    ) {
      
      public Trait[] passiveTraitsModified() {
        return new Trait[] { HIT_POINTS, ARMOUR, PARRY };
      }
      
      
      public float passiveModifierFor(Person person, Trait trait) {
        final float level = person.stats.levelFor(this);
        float muscle = person.stats.levelFor(MUSCLE);
        if (trait == HIT_POINTS) return 60 + (level * 15) + (muscle * 1);
        if (trait == ARMOUR    ) return level * 2;
        if (trait == PARRY     ) return 5 + (level * 5);
        return 0;
      }
    },
    
    LASSO = new Ability(
      "Lasso", "ability_lasso",
      "Pulls a distant target to within closer range.  (Note- strong enemies "+
      "may resist, and an adjacent tile must be free.)",
      IS_RANGED, 1, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (! (target instanceof Person)) return false;
        Tile close = closestTo((Person) target, acting);
        return ! close.blocked();
      }
      
      
      public int maxRange() {
        return 9;
      }
      
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        Volley volley = new Volley();
        volley.setupVolley(use.acting, (Person) target, true, scene);
        volley.damagePercent = 0;
        return volley;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Scene  scene       = use.acting.currentScene();
        Person acting      = use.acting;
        Person struck      = use.volley().targAsPerson();
        float  resistance  = 0.5f + (struck.stats.levelFor(MUSCLE) / 50f);
               resistance -=         acting.stats.levelFor(MUSCLE) / 50f;
        
        I.say("Lasso resistance: "+resistance);
        if (Rand.num() >= resistance && use.volley().didConnect) {
          Tile close = closestTo(struck, use.acting);
          struck.setExactPosition(close.x, close.y, 0, scene);
        }
      }
      
      
      Tile closestTo(Person target, Person from) {
        Scene scene = from.currentScene();
        Pick <Tile> pick = new Pick();
        for (Tile t : from.location().tilesAdjacent()) {
          if (t == null || t.blocked()) continue;
          pick.compare(t, 0 - scene.distance(t, target.location()));
        }
        return pick.result();
      }
      
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_lasso.png");
      public Image missileSprite() { return missile; }
    }
  ;
  
  
  final public static Kind
    GALATEA  = Kind.ofPerson(
      "Galatea", "hero_kind_galatea", IMG_DIR+"sprite_wonder_woman.png",
      Kind.TYPE_HERO,
      MUSCLE, 45 ,
      REFLEX, 25 ,
      BRAIN , 16 ,
      WILL  , 18 ,
      MOVE, 1, STRIKE, 1, DISARM, 1, EVASION, 1, GUARD, 1,
      AEGIS_ARMOUR, 1, LASSO, 1
    );
}






