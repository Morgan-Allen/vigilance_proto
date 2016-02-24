


package proto.game.content;
import proto.common.Kind;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;

import static proto.game.person.Ability.*;
import static proto.game.person.Common.*;
import static proto.game.person.Person.*;
import static proto.game.person.PersonStats.*;
import java.awt.Image;



//  TODO:  Let Corona smash through walls!

public class Corona {

  final static String IMG_DIR = "media assets/hero sprites/";
  
  
  final public static Ability
    
    INTEGRITY = new Ability(
      "Integrity", "ability_integrity",
      "Grants additional Hit Points and Armour and halves kinetic damage.",
      IS_PASSIVE | TRIGGER_ON_DEFEND, 0, NO_HARM, MINOR_POWER
    ) {
      
      public Trait[] passiveTraitsModified() {
        return new Trait[] { HIT_POINTS, ARMOUR };
      }
      
      public float passiveModifierFor(Person person, Trait trait) {
        float level = person.stats.levelFor(this);
        float muscle = person.stats.levelFor(MUSCLE);
        if (trait == HIT_POINTS) return 15 + (level * 25) + (muscle * 1);
        if (trait == ARMOUR    ) return 4  + (level * 4);
        return 0;
      }
      
      public void applyOnDefendEnd(Volley volley) {
        if (! volley.didConnect) return;
        if (! volley.hasDamageType(Equipped.IS_KINETIC)) return;
        volley.damageMargin *= 0.5f;
      }
    },
    
    HYPERCEPTION = new Ability(
      "Hyperception", "ability_hyperception",
      "Increases sight range and grants a small bonus to engineering and "+
      "biology research.",
      IS_PASSIVE, 0, NO_HARM, MINOR_POWER
    ) {
      
      public Trait[] passiveTraitsModified() {
        return new Trait[] { SIGHT };
      }
      
      public float passiveModifierFor(Person person, Trait trait) {
        final float level = person.stats.levelFor(this);
        if (trait == SIGHT) return 10 + (level * 5);
        return 0;
      }
    }
  ;
  
  
  final public static Kind
    CORONA   = Kind.ofPerson(
      "Corona", "hero_kind_corona", IMG_DIR+"sprite_superman.png",
      Kind.TYPE_HERO,
      MUSCLE, 100,
      REFLEX, 10 ,
      BRAIN , 15 ,
      WILL  , 16 ,
      MOVE, 1, STRIKE, 1, DISARM, 1, GUARD, 1,
      INTEGRITY, 1, HYPERCEPTION, 1
    );
  
  
}


