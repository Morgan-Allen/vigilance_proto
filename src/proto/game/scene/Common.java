

package proto.game.scene;
import proto.util.*;
import static proto.game.scene.Ability.*;
import static proto.game.scene.Person.*;
import java.awt.Image;



public class Common {
  


  final static String IMG_DIR = "media assets/hero sprites/";
  
  final public static Ability
    
    MOVE = new Ability(
      "Move", "ability_move",
      "Move to the chosen point.",
      NONE, 1, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (target instanceof Prop) {
          return ! ((Prop) target).kind.blockPath;
        }
        return target instanceof Tile;
      }
      
      public void applyOnActionEnd(Action use) {
        Scene s = use.acting.currentScene();
        if (s.isExitPoint(use.target, use.acting)) s.removePerson(use.acting);
      }
      
      public float animDuration() {
        return 0;
      }
    },
    
    STRIKE = new Ability(
      "Strike", "ability_strike",
      "Strike a melee target.  (Deals base of 2-7 damage, scaling with "+
      "strength and weapon bonus.  Half damage is stun.)",
      NONE, 1, REAL_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return target instanceof Person;
      }
      
      Volley createVolley(Action use) {
        Scene  scene  = use.acting.currentScene();
        Person struck = (Person) use.target;
        Volley volley = new Volley();
        volley.setupVolley(use.acting, struck, false, scene);
        volley.stunPercent = 50;
        return volley;
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      public Image missileSprite() { return missile; }
    },
    
    DISARM = new Ability(
      "Disarm", "ability_disarm",
      "Attempt to disarm a melee target.  (Deals base of 1-3 stun damage, "+
      "scaling with strength.  Disarm chance is based on enemy health.)",
      NONE, 2, MINOR_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return target instanceof Person;
      }
      
      
      Volley createVolley(Action use) {
        Scene  scene  = use.acting.currentScene();
        Person struck = (Person) use.target;
        Volley volley = new Volley();
        volley.setupVolley(use.acting, struck, false, scene);
        volley.stunPercent = 100;
        volley.damagePercent = 50;
        return volley;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Person struck       = use.volley.hitsAsPerson();
        int    damage       = use.volley.damageMargin;
        float  disarmChance = damage * 2 / struck.maxHealth();
        
        if (Rand.num() < disarmChance && struck.hasEquipped(SLOT_WEAPON)) {
          I.say(struck+" dropped their weapon!");
          struck.emptyEquipSlot(SLOT_WEAPON);
        }
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      public Image missileSprite() { return missile; }
    },
    
    BATARANG = new Ability(
      "Batarang", "ability_batarang",
      "Throw a Batarang for 1-5 damage and 66% chance to disarm. (Note that "+
      "strong opponents may resist disarming.)",
      IS_RANGED, 1, MINOR_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return target instanceof Person;
      }
      
      
      Volley createVolley(Action use) {
        Scene  scene  = use.acting.currentScene();
        Person struck = (Person) use.target;
        Volley volley = new Volley();
        volley.setupVolley(use.acting, struck, false, scene);
        volley.selfDamageBase  = 2;
        volley.selfDamageRange = 2;
        return volley;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Person struck       = use.volley.hitsAsPerson();
        float  resistance   = struck.stats.levelFor(MUSCLE) / 100f;
        float  disarmChance = 0.66f - resistance;
        
        if (Rand.num() < disarmChance && struck.hasEquipped(SLOT_WEAPON)) {
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
    },
    
    LASSO = new Ability(
      "Lasso", "ability_lasso",
      "Pulls a distant enemy to within closer range and deals strike damage."+
      " (Note- strong enemies may resist, and closest tile must be free.)",
      IS_RANGED, 2, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (! (target instanceof Person)) return false;
        Tile close = closestTo((Person) target, acting);
        return ! close.blocked();
      }
      
      
      public int maxRange() {
        return 4;
      }
      
      
      Volley createVolley(Action use) {
        return STRIKE.createVolley(use);
      }
      
      
      public void applyOnActionEnd(Action use) {
        Scene  scene       = use.acting.currentScene();
        Person acting      = use.acting;
        Person struck      = use.volley.hitsAsPerson();
        float  resistance  = 0.5f + (struck.stats.levelFor(MUSCLE) / 100f);
               resistance -=         acting.stats.levelFor(MUSCLE) / 100f;
        
        if (Rand.num() >= resistance) {
          Tile close = closestTo(struck, use.acting);
          struck.setExactPosition(close.x, close.y, 0, scene);
          STRIKE.applyOnActionEnd(use);
        }
      }
      
      
      Tile closestTo(Person target, Person from) {
        Scene scene = from.currentScene();
        Pick <Tile> pick = new Pick();
        for (Tile t : from.location.tilesAdjacent()) if (t != null) {
          pick.compare(t, 0 - scene.distance(t, target.location));
        }
        return pick.result();
      }
      
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_lasso.png");
      public Image missileSprite() { return missile; }
    }
  ;
  
  final static Equipped
    UNARMED = new Equipped(
      "Unarmed", "item_unarmed",
      "Bare fists and moxy.",
      SLOT_WEAPON, 0,
      Equipped.IS_WEAPON | Equipped.IS_MELEE, 5
    ),
    UNARMOURED = new Equipped(
      "Unarmoured", "item_unarmoured",
      "Bare fists and moxy.",
      SLOT_ARMOUR, 0,
      Equipped.IS_ARMOUR, 5
    );
  
  
  final public static Kind
    NOCTURNE = Kind.ofPerson(
      "Nocturne", "hero_kind_nocturne", IMG_DIR+"sprite_batman.png",
      Kind.TYPE_HERO,
      HEALTH, 30 ,
      ARMOUR, 2  ,
      MUSCLE, 20 ,
      BRAIN , 20 ,
      SPEED , 20 ,
      SIGHT , 10 ,
      MOVE, 1, STRIKE, 1,
      BATARANG, 1, ULTRASOUND, 1
    ),
    KESTREL  = Kind.ofPerson(
      "Kestrel", "hero_kind_kestrel", IMG_DIR+"sprite_robin.png",
      Kind.TYPE_HERO, 
      HEALTH, 20 ,
      ARMOUR, 0  ,
      MUSCLE, 16 ,
      BRAIN , 15 ,
      SPEED , 18 ,
      SIGHT , 12 ,
      MOVE, 1, STRIKE, 1,
      BATARANG, 1
    ),
    CORONA   = Kind.ofPerson(
      "Corona", "hero_kind_corona", IMG_DIR+"sprite_superman.png",
      Kind.TYPE_HERO,
      HEALTH, 250,
      ARMOUR, 8  ,
      MUSCLE, 100,
      BRAIN , 16 ,
      SPEED , 16 ,
      SIGHT , 16 ,
      MOVE, 1, STRIKE, 1, DISARM, 1
    ),
    GALATEA  = Kind.ofPerson(
      "Galatea", "hero_kind_galatea", IMG_DIR+"sprite_wonder_woman.png",
      Kind.TYPE_HERO,
      HEALTH, 180,
      ARMOUR, 2  ,
      MUSCLE, 45 ,
      BRAIN , 16 ,
      SPEED , 25 ,
      SIGHT , 10 ,
      MOVE, 1, STRIKE, 1, DISARM, 1,
      LASSO, 1
    );
  
}














