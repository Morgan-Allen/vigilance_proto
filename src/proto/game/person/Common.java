

package proto.game.person;
import proto.common.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.util.*;

import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;

import java.awt.Image;
import java.awt.Graphics2D;



public class Common {

  final static String
    ICONS_DIR  = "media assets/ability icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  
  final public static ItemType
    UNARMED = new ItemType(
      "Unarmed", "item_unarmed",
      "Bare fists and moxy.",
      ICONS_DIR+null,
      SPRITE_DIR+"sprite_punch.png",
      Kind.SUBTYPE_BLUNT, SLOT_WEAPON, 0, new Object[0],
      IS_WEAPON | IS_MELEE | IS_KINETIC
    ),
    UNARMOURED = new ItemType(
      "Unarmoured", "item_unarmoured",
      "Nothin' but the clothes on your back.",
      ICONS_DIR+null,
      SPRITE_DIR+"sprite_deflect.png",
      Kind.SUBTYPE_ARMOUR, SLOT_ARMOUR, 0, new Object[0],
      IS_ARMOUR
    );
  
  
  final public static Ability
    
    MOVE = new Ability(
      "Move", "ability_move",
      ICONS_DIR+"icon_move.png",
      "Move to the chosen point.",
      Ability.IS_BASIC | Ability.NO_NEED_LOS | Ability.NO_NEED_FOG, 1,
      Ability.NO_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        final Tile under = scene.tileUnder(target);
        if (under == null || under.blocked()) return false;
        return true;
      }
      
      public void applyOnActionEnd(Action use) {
        if (use.scene().entry.isExitPoint(use.target, use.acting)) {
          use.scene().removePerson(use.acting);
        }
      }
      
      public float animDuration() {
        return 0;
      }
    },
    
    STRIKE = new Ability(
      "Strike", "ability_strike",
      ICONS_DIR+"icon_strike.png",
      "Strike a melee target.  (Base damage scales with strength and weapon "+
      "bonus, 50% stun damage.)",
      Ability.IS_BASIC | Ability.IS_MELEE,
      1, Ability.REAL_HARM, Ability.MINOR_POWER
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
        volley.setupMeleeVolley(use.acting, (Person) target, scene);
        volley.stunPercent.inc(50, this);
        return volley;
      }
      
      final Image missile = Kind.loadImage(SPRITE_DIR+"sprite_punch.png");
      
      public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
        FX.renderMissile(action, scene, missile, 0.5f, g);
      }
    },
    
    FIRE = new Ability(
      "Fire", "ability_throw",
      ICONS_DIR+"icon_fire.png",
      "Fire a shot using ranged weaponry.  Accuracy falls off with distance.",
      Ability.IS_BASIC | Ability.IS_RANGED, 1,
      Ability.REAL_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (! acting.gear.weaponType().ranged()) return false;
        return target instanceof Person;
      }
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        Volley volley = new Volley();
        volley.setupVolley(use.acting, (Person) target, true, scene);
        return volley;
      }
      
      public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
        Person acting = action.acting;
        ItemType weapon = acting.gear.weaponType();
        if (weapon == null || ! weapon.ranged()) return;
        weapon.renderUsageFX(action, scene, g);
      }
    },
    
    GUARD = new Ability(
      "Guard", "ability_guard",
      ICONS_DIR+"icon_guard.png",
      "Reserve AP to reduce incoming damage and grant 50% chance to counter-"+
      "attack if you successfully defend.  Boosts defence by 25%, +5 per AP.  "+
      "Ends turn.",
      Ability.IS_BASIC | Ability.IS_DELAYED | Ability.TRIGGER_ON_DEFEND,
      1, Ability.NO_HARM, Ability.MINOR_POWER
    ) {
      
      public void applyOnDefendStart(Volley volley) {
        Person self = volley.targAsPerson();
        Person hits = volley.origAsPerson();
        FX.dodgePosition(self, hits, 0.33f);
        float baseArmour = self.stats.levelFor(PersonStats.ARMOUR);
        volley.hitsArmour .inc(2 + (baseArmour / 2f), this);
        volley.hitsDefence.inc(25 + (self.actions.currentAP() * 5), this);
      }
      
      public void applyOnDefendEnd(Volley volley) {
        Person self  = volley.targAsPerson();
        Person hits  = volley.origAsPerson();
        Scene  scene = self  .currentScene();
        Tile at = self.currentTile();
        self.setExactPosition(at.scene, at.x, at.y, 0);
        float counterChance = 0.5f;
        boolean opening = volley.melee() && ! volley.didConnect();
        if (Rand.num() < counterChance && opening) {
          STRIKE.takeFreeAction(self, hits.currentTile(), hits, scene);
        }
      }
    },

    //  TODO:  Have a special command for swapping weapons, then merge strike/
    //  fire into one ability, along with guard/overwatch.
    HIDE = new Ability(
      "Hide", "ability_hide",
      ICONS_DIR+"icon_hide.png",
      "Crouch down to improve hide range by 2 and defence by 20.  Ends turn.",
      Ability.IS_SELF_ONLY | Ability.IS_CONDITION, 1,
      Ability.REAL_HARM, Ability.MINOR_POWER
    ) {
      public void applyOnActionAssigned(Action use) {
        use.acting.stats.applyCondition(this, use.acting, 1);
        use.acting.onTurnEnd();
      }
      
      public float conditionModifierFor(Person person, Trait trait) {
        if (trait == PersonStats.HIDE_RANGE) return 2 ;
        if (trait == PersonStats.DEFENCE   ) return 20;
        return 0;
      }
    },
    
    BASIC_ABILITIES[] = { MOVE, STRIKE, FIRE, GUARD, HIDE };
  
  
  final public static Ability SPECIAL_ACTION = new Ability(
    "Special Action", "ability_special_action",
    null, "Perform a special action associated with a criminal plot.",
    Ability.NO_NEED_FOG | Ability.NO_NEED_LOS,
    1, Ability.NO_HARM, Ability.NO_POWER
  ) {
    
    public float rateUsage(Action use) {
      PlanStep step = use.scene().triggerEventPlanStep();
      return step == null ? 0 : step.type.rateSpecialAction(use);
    }
    
    public void applyOnActionEnd(Action use) {
      PlanStep step = use.scene().triggerEventPlanStep();
      if (step != null) step.type.onSpecialActionEnd(use);
    }
  };
  
  
  //
  //  Persons have a couple of randomly-defined basic traits.
  final static Trait HAIR_COLOURS[] = Trait.traitsWith(
    "hair", "XXX",
    "Hair: Red"   ,
    "Hair: Blonde",
    "Hair: Brown" ,
    "Hair: Black" ,
    "Hair: Grey"  ,
    "Hair: White"
  );
  final static Trait EYE_COLOURS[] = Trait.traitsWith(
    "eyes", "XXX",
    "Eyes: Grey" ,
    "Eyes: Green",
    "Eyes: Brown",
    "Eyes: Blue"
  );
  final static Trait RACES[] = Trait.traitsWith(
    "race", "XXX",
    "Race: Black"  ,
    "Race: Indian" ,
    "Race: Asian"  ,
    "Race: White"  ,
    "Race: Amerind",
    "Race: Mixed"
  );
  final static Trait BUILD[] = Trait.traitsWith(
    "build", "XXX",
    "Build: Stout" ,
    "Build: Skinny",
    "Build: Tall"  ,
    "Build: Petit"
  );
  
  //
  //  Locations have a number of trace elements that can be inferred from
  //  forensics, based on interior elements.
  //  Hot, Cold, Wet, Sunny?
  //  Treat proximities as traits?
  final static Trait PLACE_TRAITS[] = Trait.traitsWith(
    "place_trait", "XXX",
    "Rural"     ,
    "Industrial",
    "Medical"   ,
    "Slum"      ,
    "Office"    ,
    "Ritzy"     ,
    "Public"    ,
    "Academic"  ,
    "Domestic"  ,
    "Secure"
  );
  
}










