

package proto;
import util.*;
import static proto.Ability.*;
import static proto.Person.*;
import java.awt.Image;



public class Common {
  


  final static String IMG_DIR = "media assets/hero sprites/";
  
  final static Ability
    
    MOVE = new Ability(
      "Move", "Move to the chosen point.",
      NONE, 1, NO_HARM, MINOR_POWER
    ) {
      
      boolean allowsTarget(Object target) {
        if (target instanceof Prop) {
          return ! ((Prop) target).kind.blockPath;
        }
        return target instanceof Tile;
      }
      
      void applyEffect(Action use) {
        return;
      }
      
      float animDuration() {
        return 0;
      }
    },
    
    STRIKE = new Ability(
      "Strike",
      "Strike a melee target.  (Deals base of 2-7 damage, scaling with "+
      "strength.)",
      NONE, 1, MINOR_HARM, MINOR_POWER
    ) {
      boolean allowsTarget(Object target) {
        return target instanceof Person;
      }
      
      void applyEffect(Action use) {
        final Person struck = (Person) use.target;
        float power = use.acting.levelFor(Person.MUSCLE);
        float damage = (Rand.index(6) + 2) * power / 10f;
        struck.takeDamage(damage / 2, damage / 2);
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      Image missileSprite() { return missile; }
    },
    
    BATARANG = new Ability(
      "Batarang",
      "Throw a Batarang for light damage and chance to disarm.",
      IS_RANGED, 1, MINOR_HARM, MINOR_POWER
    ) {
      
      boolean allowsTarget(Object target) {
        return target instanceof Person;
      }
      
      void applyEffect(Action use) {
        final Person struck = (Person) use.target;
        float damage = Rand.index(4) + 1;
        struck.takeDamage(damage, 0);
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_batarang.png");
      Image missileSprite() { return missile; }
    };
  
  
  final static Kind
    NOCTURNE = Kind.ofPerson(
      "Nocturne", IMG_DIR+"sprite_batman.png",
      Kind.TYPE_HERO,
      HEALTH, 30 ,
      ARMOUR, 2  ,
      MUSCLE, 20 ,
      BRAIN , 20 ,
      SPEED , 20 ,
      SIGHT , 10 ,
      MOVE, 1, STRIKE, 1,
      BATARANG, 1
    ),
    KESTREL  = Kind.ofPerson(
      "Kestrel", IMG_DIR+"sprite_robin.png",
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
      "Corona", IMG_DIR+"sprite_superman.png",
      Kind.TYPE_HERO,
      HEALTH, 250,
      ARMOUR, 8  ,
      MUSCLE, 100,
      BRAIN , 16 ,
      SPEED , 16 ,
      SIGHT , 16 ,
      MOVE, 1, STRIKE, 1
    ),
    GALATEA  = Kind.ofPerson(
      "Galatea", IMG_DIR+"sprite_wonder_woman.png",
      Kind.TYPE_HERO,
      HEALTH, 180,
      ARMOUR, 2  ,
      MUSCLE, 45 ,
      BRAIN , 16 ,
      SPEED , 25 ,
      SIGHT , 10 ,
      MOVE, 1, STRIKE, 1
    );
  
}














