

package proto;
import util.*;
import static proto.Ability.*;
import static proto.Person.*;
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
      
      public void applyEffect(Action use) {
        Scene s = use.acting.currentScene();
        if (s.isExitPoint(use.target, use.acting)) s.removePerson(use.acting);
      }
      
      float animDuration() {
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
      
      public void applyEffect(Action use) {
        final Person struck = (Person) use.target;
        float power = use.acting.levelFor(MUSCLE);
        power += use.acting.equipmentBonus(SLOT_WEAPON, Equipped.IS_MELEE);
        float damage = (Rand.index(6) + 2) * power / 10f;
        struck.takeDamage(damage / 2, damage / 2);
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      Image missileSprite() { return missile; }
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
      
      public void applyEffect(Action use) {
        final Person struck = (Person) use.target;
        float power = use.acting.levelFor(MUSCLE);
        float damage = (Rand.index(2) + 1) * power / 10f;
        struck.takeDamage(0, damage);
        
        float disarmChance = damage * 2 / struck.maxHealth();
        if (Rand.num() < disarmChance && struck.hasEquipped(SLOT_WEAPON)) {
          I.say(struck+" dropped their weapon!");
          struck.emptyEquipSlot(SLOT_WEAPON);
        }
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      Image missileSprite() { return missile; }
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
      
      public void applyEffect(Action use) {
        final Person struck = (Person) use.target;
        float damage = Rand.index(4) + 1;
        struck.takeDamage(damage, 0);
        
        float resistance = struck.levelFor(MUSCLE) / 100f;
        float disarmChance = 0.66f - resistance;
        if (Rand.num() < disarmChance && struck.hasEquipped(SLOT_WEAPON)) {
          I.say(struck+" dropped their weapon!");
          struck.emptyEquipSlot(Person.SLOT_WEAPON);
        }
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_batarang.png");
      Image missileSprite() { return missile; }
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
      
      
      public void applyEffect(Action use) {
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
      
      
      public void applyEffect(Action use) {
        Scene scene = use.acting.currentScene();
        final Person struck = (Person) use.target;
        
        float resistance = 0.5f + (struck.levelFor(MUSCLE) / 100f);
        resistance -= use.acting.levelFor(MUSCLE) / 100f;
        
        if (Rand.num() >= resistance) {
          Tile close = closestTo(struck, use.acting);
          struck.setExactPosition(close.x, close.y, 0, scene);
          STRIKE.applyEffect(use);
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
      Image missileSprite() { return missile; }
    };
  
  
  final static Kind
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














