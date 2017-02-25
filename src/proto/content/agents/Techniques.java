

package proto.content.agents;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;

import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;
import static proto.game.person.PersonStats.*;

import proto.view.base.*;

import java.awt.Graphics2D;
import java.awt.Image;



public class Techniques {
  
  final static String
    ICONS_DIR  = "media assets/ability icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  final public static Ability
    
    BRAWLER = new Ability(
      "Brawler", "ability_brawler",
      ICONS_DIR+"icon_brawler.png",
      "Deal 1 extra damage in melee (+0.5 per experience grade), once armour "+
      "is penetrated.",
      Ability.IS_PASSIVE | Ability.IS_MELEE | Ability.TRIGGER_ON_ATTACK,
      1, Ability.MINOR_HARM, Ability.MINOR_POWER
    ) {
      public void applyOnAttackEnd(Volley volley) {
        Person self = volley.origAsPerson();
        Person mark = volley.targAsPerson();
        int level = self.stats.levelFor(this) - 1;
        
        if (volley.melee() && mark != null && volley.didDamage()) {
          mark.health.receiveInjury(1 + (level * 0.5f));
        }
        return;
      }
    },
    
    PUNISHER = new Ability(
      "Punisher", "ability_punisher",
      ICONS_DIR+"icon_punisher.png",
      "Deals additional melee damage with increased risk of lethality and "+
      "chance to stun.",
      Ability.IS_MELEE, 1, Ability.REAL_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (target instanceof Person) {
          final Person other = (Person) target;
          return other.isEnemy(acting);
        }
        return false;
      }
      
      //  TODO:  Modify this to create a persistent 'punishment' effect after
      //  melee attacks, to persistently hamper enemies.
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        int level = use.acting.stats.levelFor(this);
        Volley volley = new Volley();
        volley.setupMeleeVolley(use.acting, (Person) target, scene);
        volley.selfDamageRange.inc(1 + level, this);
        volley.stunPercent    .set(20       , this);
        //  TODO:  Include actual stun effects!
        return volley;
      }
      
      final Image missile = Kind.loadImage(SPRITE_DIR+"sprite_punch.png");
      
      public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
        FX.renderMissile(action, scene, missile, 0.5f, g);
      }
    },
    
    DISARM = new Ability(
      "Disarm", "ability_disarm",
      ICONS_DIR+"icon_disarm.png",
      "Deals nonlethal melee damage with a chance to remove the target's "+
      "weapon.",
      Ability.IS_MELEE, 2, Ability.REAL_HARM, Ability.MINOR_POWER
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
        volley.stunPercent.set(100, this);
        return volley;
      }
      
      public void applyOnActionEnd(Action use) {
        Person struck       = use.volley().targAsPerson();
        int    damage       = use.volley().damageMargin.value();
        float  disarmChance = damage * 2f / struck.health.maxHealth();
        
        if (Rand.num() < disarmChance && struck.gear.hasEquipped(SLOT_WEAPON)) {
          I.say(struck+" dropped their weapon!");
          struck.gear.dropItem(SLOT_WEAPON);
        }
      }
      
      final Image missile = Kind.loadImage(SPRITE_DIR+"sprite_punch.png");
      
      public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
        FX.renderMissile(action, scene, missile, 0.5f, g);
      }
    },
    
    SPRINTER = new Ability(
      "Sprinter", "ability_sprinter",
      ICONS_DIR+"icon_sprinter.png",
      "Increases movement speed by 1 tile per experience grade and provides "+
      "a small bonus to defence.",
      Ability.IS_PASSIVE, 0, Ability.NO_HARM, Ability.MINOR_POWER
    ) {
      public float passiveModifierFor(Person person, Trait trait) {
        int level = person.stats.levelFor(this);
        if (trait == MOVE_SPEED) return level * 1;
        if (trait == DEFENCE   ) return Nums.max(0, (level * 2) - 1);
        return 0;
      }
    },
    
    EVASION = new Ability(
      "Evasion", "ability_evasion",
      ICONS_DIR+"icon_evasion.png",
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
        int dodgeBonus = 20 + (self.actions.currentAP() * 5) + (level * 5);
        volley.hitsDefence.inc(dodgeBonus, this);
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
      ICONS_DIR+"icon_endurance.png",
      "Grants 2 bonus health per experience grade.",
      Ability.IS_PASSIVE, 1,
      Ability.NO_HARM, Ability.MINOR_POWER
    ) {
      public float passiveModifierFor(Person person, Trait trait) {
        int level = person.stats.levelFor(this);
        if (trait == HEALTH) return level * 2;
        return 0;
      }
    },
    
    STEADY_AIM = new Ability(
      "Steady Aim", "ability_steady_aim",
      ICONS_DIR+"icon_steady_aim.png",
      "Grants +25 accuracy to your ranged attacks (+5 per experience grade) "+
      "for one turn.",
      Ability.IS_SELF_ONLY | Ability.IS_CONDITION | Ability.TRIGGER_ON_ATTACK,
      2, Ability.MINOR_HELP, Ability.MINOR_POWER
    ) {
      
      public void applyOnActionAssigned(Action use) {
        use.acting.stats.applyCondition(this, use.acting, 1);
      }
      
      public void modifyAttackVolley(Volley volley) {
        Person using = volley.origAsPerson();
        int level = using.stats.levelFor(this) - 1;
        if (volley.ranged()) volley.selfAccuracy.inc(25 + (level * 5), this);
      }
    },
    
    OVERWATCH = new Ability(
      "Overwatch", "ability_overwatch",
      ICONS_DIR+"icon_overwatch.png",
      "Readies a ranged weapon for use when an enemy comes in view, at a -15 "+
      "accuracy penality (+5 per experience grade.)",
      Ability.IS_DELAYED | Ability.TRIGGER_ON_NOTICE, 1,
      Ability.MINOR_HELP, Ability.MINOR_POWER
    ) {
      
      public boolean triggerOnNoticing(Person acts, Person seen, Action noted) {
        if (! noted.inMotion()) return false;
        return seen.isEnemy(acts);
      }
      
      public void applyOnNoticing(Person acts, Person seen, Action noted) {
        Scene scene = acts.currentScene();
        int level = acts.stats.levelFor(this);
        
        //  TODO:  Include an added bonus for reserving AP!
        
        final Action fire = Common.FIRE.takeFreeAction(
          acts, seen.currentTile(), seen, scene
        );
        fire.volley().selfAccuracy.inc((level * 5) - 20, this);
        
        acts.actions.cancelAction();
      }
    },
    
    //  TODO:  This needs to be either ranged or melee, depending on the type
    //  of the user's weapon!
    FLESH_WOUND = new Ability(
      "Flesh Wound", "ability_flesh_wound",
      ICONS_DIR+"icon_flesh_wound.png",
      "Reduces accuracy and crit chance to reduce lethality with precision "+
      "firearms.",
      Ability.IS_RANGED, 1, Ability.REAL_HARM, Ability.MINOR_POWER
    ) {
      
      public boolean allowsUse(Person acting, StringBuffer failLog) {
        final ItemType weapon = acting.gear.weaponType();
        int subtype = weapon.subtype();
        if (subtype == Kind.SUBTYPE_BLADE      ) return true;
        if (subtype == Kind.SUBTYPE_PRECISE_GUN) return true;
        return false;
      }
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        if (target instanceof Person) {
          final Person other = (Person) target;
          return other.isEnemy(acting);
        }
        return false;
      }
      
      protected Volley createVolley(Action use, Object target, Scene scene) {
        Volley volley = new Volley();
        int level = use.acting.stats.levelFor(this) - 1;
        volley.setupVolley(use.acting, (Person) target, true, scene);
        int miss = -15 + (level * 5 );
        int soft =  40 + (level * 10);
        int crit = -10;
        volley.selfAccuracy.inc(miss, this);
        volley.stunPercent .inc(soft, this);
        volley.critPercent .inc(crit, this);
        return volley;
      }
      
      public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
        final ItemType weapon = action.acting.gear.weaponType();
        if (weapon.melee()) Common.STRIKE.renderUsageFX(action, scene, g);
        else                Common.FIRE  .renderUsageFX(action, scene, g);
      }
    };
  
  final public static AbilityPalette CORE_TECHNIQUES;
  static {
    AbilityPalette p = CORE_TECHNIQUES = new AbilityPalette(3, 4);
    p.attachAbility(ENDURANCE  , 0, 0);
    p.attachAbility(SPRINTER   , 0, 1);
    p.attachAbility(EVASION    , 0, 2);
    SPRINTER.attachRoots(ENDURANCE);
    EVASION .attachRoots(SPRINTER );
    
    p.attachAbility(BRAWLER    , 1, 0);
    p.attachAbility(DISARM     , 1, 1);
    p.attachAbility(PUNISHER   , 1, 2);
    DISARM  .attachRoots(BRAWLER);
    PUNISHER.attachRoots(DISARM );
    
    p.attachAbility(STEADY_AIM , 2, 0);
    p.attachAbility(OVERWATCH  , 2, 1);
    p.attachAbility(FLESH_WOUND, 2, 2);
    OVERWATCH  .attachRoots(STEADY_AIM);
    FLESH_WOUND.attachRoots(OVERWATCH );
    
  }
}











