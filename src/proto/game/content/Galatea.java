

package proto.game.content;
import proto.common.Kind;
import proto.game.person.Ability;
import proto.game.person.Person;
import proto.game.scene.*;
import proto.util.*;

import static proto.game.content.Common.*;
import static proto.game.person.Ability.*;
import static proto.game.person.Person.*;

import java.awt.Image;



public class Galatea {
  
  
  final public static Ability
    LASSO = new Ability(
      "Lasso", "ability_lasso",
      "Pulls a distant target to within closer range.  (Note- strong enemies "+
      "may resist, and an adjacent tile must be free.)",
      IS_RANGED, 2, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (! (target instanceof Person)) return false;
        Tile close = closestTo((Person) target, acting);
        return ! close.blocked();
      }
      
      
      public int maxRange() {
        return 9;
      }
      
      
      protected Volley createVolley(Action use) {
        Scene  scene  = use.acting.currentScene();
        Person struck = (Person) use.target;
        Volley volley = new Volley();
        volley.setupVolley(use.acting, struck, true, scene);
        volley.damagePercent = 0;
        return volley;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Scene  scene       = use.acting.currentScene();
        Person acting      = use.acting;
        Person struck      = use.volley().targAsPerson();
        float  resistance  = 0.5f + (struck.stats.levelFor(MUSCLE) / 100f);
               resistance -=         acting.stats.levelFor(MUSCLE) / 100f;
        
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
      HEALTH, 180,
      ARMOUR, 2  ,
      MUSCLE, 45 ,
      BRAIN , 16 ,
      SPEED , 25 ,
      SIGHT , 10 ,
      MOVE, 1, STRIKE, 1, DISARM, 1, EVASION, 1, GUARD, 1,
      LASSO, 1
    );
}






