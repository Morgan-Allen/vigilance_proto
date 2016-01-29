

package proto.game.content;
import proto.game.scene.*;
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
          return ! ((Prop) target).kind().blockPath();
        }
        return target instanceof Tile;
      }
      
      public void applyOnActionEnd(Action use) {
        Scene s = use.scene();
        if (s.isExitPoint(use.target, use.acting)) s.removePerson(use.acting);
      }
      
      public float animDuration() {
        return 0;
      }
    },
    
    //  TODO:  Add Sneak!
    
    //  TODO:  Add Dash!
    
    STRIKE = new Ability(
      "Strike", "ability_strike",
      "Strike a melee target.  (Base damage scales with strength and weapon "+
      "bonus, 50% stun damage.)",
      NONE, 1, REAL_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return target instanceof Person;
      }
      
      protected Volley createVolley(Action use) {
        Scene  scene  = use.scene();
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
      
      
      protected Volley createVolley(Action use) {
        Scene  scene  = use.acting.currentScene();
        Person struck = (Person) use.target;
        Volley volley = new Volley();
        volley.setupVolley(use.acting, struck, false, scene);
        volley.stunPercent = 100;
        volley.damagePercent = 50;
        return volley;
      }
      
      
      public void applyOnActionEnd(Action use) {
        Person struck       = use.volley().targAsPerson();
        int    damage       = use.volley().damageMargin;
        float  disarmChance = damage * 2 / struck.maxHealth();
        
        if (Rand.num() < disarmChance && struck.hasEquipped(SLOT_WEAPON)) {
          I.say(struck+" dropped their weapon!");
          struck.emptyEquipSlot(SLOT_WEAPON);
        }
      }
      
      final Image missile = Kind.loadImage(IMG_DIR+"sprite_punch.png");
      public Image missileSprite() { return missile; }
    },
    
    //  TODO:  Add Finish!
    
    EVASION = new Ability(
      "Evasion", "ability_evasion",
      "Reserve AP to increase chance to dodge enemy attacks by at least 20%. "+
      "Ends turn.",
      IS_DELAYED | TRIGGER_ON_DEFEND, 1, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return acting instanceof Person;
      }
      
      public void applyOnDefendStart(Volley volley) {
        Person self = volley.targAsPerson();
        Person hits = volley.origAsPerson();
        dodgePosition(self, hits, 0.33f);
        volley.hitsDefence += 20;
      }
      
      public void applyOnDefendEnd(Volley volley) {
        Person self = volley.targAsPerson();
        Tile at = self.location();
        self.setExactPosition(at.x, at.y, 0, at.scene);
      }
    },
    
    GUARD = new Ability(
      "Guard", "ability_guard",
      "Reserve AP to reduce incoming damage and grant chance to counter-"+
      "attack in melee.  Ends turn.",
      IS_DELAYED | TRIGGER_ON_DEFEND, 1, NO_HARM, MINOR_POWER
    ) {
      
      public boolean allowsTarget(Object target, Scene scene, Person acting) {
        return acting instanceof Person;
      }
      
      
      public void applyOnDefendStart(Volley volley) {
        Person self = volley.targAsPerson();
        Person hits = volley.origAsPerson();
        dodgePosition(self, hits, -0.33f);
        volley.hitsArmour += 2;
        volley.hitsArmour += self.baseArmour() / 2f;
      }
      
      
      public void applyOnDefendEnd(Volley volley) {
        Person self  = volley.targAsPerson();
        Person hits  = volley.origAsPerson();
        Scene  scene = self  .currentScene();
        Tile at = self.location();
        self.setExactPosition(at.x, at.y, 0, at.scene);
        float counterChance = 0.5f;
        boolean opening = volley.melee() && ! volley.didConnect;
        if (Rand.num() < counterChance && opening) {
          STRIKE.takeFreeAction(self, hits.location(), hits, scene);
        }
      }
    }
    
    //  TODO:  Add Overwatch!
  ;
  
  
  final public static Equipped
    UNARMED = new Equipped(
      "Unarmed", "item_unarmed",
      "Bare fists and moxy.",
      SLOT_WEAPON, 0,
      Equipped.IS_WEAPON | Equipped.IS_MELEE, 0
    ),
    UNARMOURED = new Equipped(
      "Unarmoured", "item_unarmoured",
      "Bare fists and moxy.",
      SLOT_ARMOUR, 0,
      Equipped.IS_ARMOUR, 0
    );
  
}














