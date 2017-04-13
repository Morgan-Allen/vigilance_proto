

package proto.content.items;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;

import static proto.game.person.PersonStats.*;
import static proto.game.person.ItemType.*;
import static proto.game.person.PersonGear.*;

import java.awt.Graphics2D;
import java.awt.Image;



public class Gadgets {
  
  
  final static String
    ICONS_DIR  = "media assets/item icons/",
    SPRITE_DIR = "media assets/character sprites/"
  ;
  
  
  final public static Ability BOLAS_THROW = new Ability(
    "Throw Bolas", "ability_bolas_throw",
    ICONS_DIR+"icon_bolas.png",
    "Temporarily entangles a subject, preventing most physical actions. "+
    "High-strength targets or escape artists can break free.",
    Ability.IS_ACTIVE | Ability.IS_EQUIPPED | Ability.IS_RANGED, 1,
    Ability.MINOR_HARM, Ability.MEDIUM_POWER
  ) {
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      if (! (target instanceof Person)) return false;
      return true;
    }
    
    public void applyOnActionEnd(Action use) {
      final Person target = (Person) use.target;
      I.say("Applying bolas to target: "+target);
      target.stats.applyCondition(this, use.acting, 3);
      use.acting.gear.useCharge(BOLAS, 1);
    }
    
    public void applyConditionOnTurn(Person person, Person source) {
      float strength = person.stats.levelFor(MUSCLE  ) / 20f;
      float reflex   = person.stats.levelFor(REFLEXES) / 20f;
      if      (Rand.num() < strength) {
        person.stats.removeCondition(this, source);
      }
      else if (Rand.num() < reflex  ) {
        person.stats.removeCondition(this, source);
      }
    }
    
    public boolean conditionAllowsAbility(Ability a) {
      if (a == Common.MOVE) return false;
      if (a.equipped    ()) return false;
      if (a.melee       ()) return false;
      return false;
    }
    
    
    public boolean conditionAllowsAction(Action a) {
      if (! Visit.empty(a.path())) return false;
      return true;
    }
    
    final Image MISSILE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_bolas.png");
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, MISSILE_IMG, 0.5f, g);
    }
  };
  
  final public static ItemType BOLAS = new ItemType(
    "Bolas", "item_bolas",
    "Temporarily entangles a subject, preventing most physical actions. "+
    "High-strength targets or escape artists can break free.",
    ICONS_DIR+"icon_bolas.png",
    SPRITE_DIR+"sprite_bolas.png",
    Kind.SUBTYPE_GADGET, SLOT_TYPE_ITEM,
    15, new Object[] { ENGINEERING, 2 },
    IS_CONSUMED, BOLAS_THROW
  ) {
    
  };
  
  
  final static Ability MED_KIT_HEAL = new Ability(
    "First Aid (Medkit)", "ability_med_kit_heal",
    ICONS_DIR+"icon_med_kit.png",
    "Patches up injury sustained in combat (up to 8 health), halts bleeding "+
    "and reduces recovery time.",
    Ability.IS_ACTIVE | Ability.IS_EQUIPPED | Ability.IS_MELEE, 2,
    Ability.REAL_HELP, Ability.MEDIUM_POWER
  ) {
    
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      if (! (target instanceof Person)) return false;
      return ((Person) target).health.injury() > 0;
    }
    
    public void applyOnActionEnd(Action use) {
      Person healed = (Person) use.target;
      healed.health.liftInjury(6 + Rand.index(3));
      healed.health.liftTotalHarm(2);
      healed.health.toggleBleeding(false);
      use.acting.gear.useCharge(Gadgets.MED_KIT, 0.5f);
    }
  };
  
  final public static ItemType MED_KIT = new ItemType(
    "Med Kit", "item_med_kit",
    "Med Kits can provide vital first aid to bleeding or incapacitated "+
    "subjects.",
    ICONS_DIR+"icon_med_kit.png",
    SPRITE_DIR+"sprite_treatment.png",
    Kind.SUBTYPE_GADGET, SLOT_TYPE_ITEM,
    25, new Object[] { MEDICINE, 4 },
    IS_CONSUMED, MED_KIT_HEAL
  ) {
  };
  
  
  final static Ability TEAR_GAS_ABILITY = new Ability(
    "Tear Gas", "tear_gas_condition",
    ICONS_DIR+"icon_tear_gas.png",
    "Reduces accuracy, sight range and action-points.  Lasts three turns.",
    Ability.IS_CONDITION | Ability.IS_AREA_EFFECT | Ability.IS_RANGED, 2,
    Ability.MINOR_HARM, Ability.MEDIUM_POWER
  ) {
    final Image GRENADE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_grenade.png");
    final Image BURST_IMG   = Kind.loadImage(SPRITE_DIR+"sprite_smoke.png"  );
    
    public int maxRange() {
      //  TODO:  Base this off the thrower's strength (as with other
      //  projectiles.)
      return 8;
    }
    
    public int maxEffectRange() {
      return 3;
    }
    
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      return true;
    }
    
    public void applyOnActionEnd(Action use) {
      use.acting.gear.useCharge(Gadgets.TEAR_GAS, 1);
      final Tile at = use.scene().tileUnder(use.target);
      final World world = use.scene().world();
      use.scene().view().addTempFX(BURST_IMG, 3, at.x, at.y, 0, 1);
      
      for (Tile t : tilesInRange(use, at)) {
        final PropEffect effect = new PropEffect(TEAR_GAS_EFFECT, use, world);
        effect.enterScene(at.scene, t.x, t.y, TileConstants.N);
        effect.turnsLeft = 2;
        for (Person p : t.persons()) {
          TEAR_GAS_EFFECT.onPersonEntry(p, use.scene(), effect);
        }
      }
    }
    
    public float conditionModifierFor(Person person, Trait trait) {
      if (trait == ACT_POINTS ) return -1;
      if (trait == SIGHT_RANGE) return -2;
      if (trait == ACCURACY   ) return -30;
      return 0;
    }
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, GRENADE_IMG, 0.5f, g);
    }
  };
  
  final public static PropType TEAR_GAS_EFFECT = new PropType(
    "Tear gas", "prop_tear_gas",
    SPRITE_DIR+"sprite_smoke.png",
    Kind.SUBTYPE_EFFECT, 1, 1, Kind.BLOCK_NONE, false
  ) {
    public void onPersonEntry(Person p, Scene s, Prop ofType) {
      Action source = ((PropEffect) ofType).source;
      p.stats.applyCondition(source.used, source.acting, 3);
    }
  };
  
  final public static ItemType TEAR_GAS = new ItemType(
    "Tear Gas", "item_tear_gas",
    "Tear Gas can blind and suffocate opponents long enough to finish them "+
    "with relative impunity.",
    ICONS_DIR+"icon_tear_gas.png",
    SPRITE_DIR+"sprite_grenade.png",
    Kind.SUBTYPE_GADGET, SLOT_TYPE_ITEM,
    35, new Object[] { MEDICINE, 6 },
    IS_CONSUMED, TEAR_GAS_ABILITY
  ) {
    
  };
  
  
  //  TODO:  The area revealed should last until the end of the next turn.
  final static Ability SONIC_PROBE_ABILITY = new Ability(
    "Sonic Probe", "sonic_probe_ability",
    ICONS_DIR+"sprite_sonic_probe.png",
    "Reveals an area hidden in the fog of war.  Lasts 2 turns.",
    Ability.IS_RANGED | Ability.NO_NEED_FOG, 1,
    Ability.NO_HARM, Ability.MINOR_POWER
  ) {
    public boolean allowsTarget(Object target, Scene scene, Person acting) {
      return true;
    }
    
    final Image GRENADE_IMG = Kind.loadImage(SPRITE_DIR+"sprite_grenade.png");
    final Image BURST_IMG   = Kind.loadImage(SPRITE_DIR+"sprite_sonic.png"  );
    
    public void renderUsageFX(Action use, Scene scene, Graphics2D g) {
      FX.renderMissile(use, scene, GRENADE_IMG, 0.5f, g);
    }
    
    public void applyOnActionEnd(Action use) {
      Scene scene = use.scene();
      Tile at = scene.tileUnder(use.target);
      PropEffect probe = new PropEffect(SONIC_PROBE_EFFECT, use, scene.world());
      probe.enterScene(scene, at.x, at.y, TileConstants.N);
      probe.turnsLeft = 2;
      SONIC_PROBE_EFFECT.updateFogFor(scene, probe);
      use.scene().view().addTempFX(BURST_IMG, 3, at.x, at.y, 0, 1);
      use.acting.gear.useCharge(SONIC_PROBE, 1);
    }
  };
  
  final public static PropType SONIC_PROBE_EFFECT = new PropType(
    "Sonic Probe", "prop_sonic_probe",
    SPRITE_DIR+"sprite_grenade.png",
    Kind.SUBTYPE_EFFECT, 1, 1, Kind.BLOCK_NONE, false
  ) {
    public void updateFogFor(Scene s, PropEffect ofType) {
      Action source = ofType.source;
      Tile at = ofType.origin();
      s.vision.liftFogAround(at, 4, source.acting, true, at);
      super.updateFogFor(s, ofType);
    }

    protected float spriteScale() {
      return 0.5f;
    }
  };
  
  final public static ItemType SONIC_PROBE = new ItemType(
    "Sonic Probe", "item_sonic_probe",
    "Can be thrown to reveal areas of hidden terrain.",
    ICONS_DIR+"icon_sonic_probe.png",
    SPRITE_DIR+"sprite_sonic_probe.png",
    Kind.SUBTYPE_GADGET, SLOT_TYPE_ITEM,
    65, new Object[] { ENGINEERING, 7 },
    IS_CONSUMED, SONIC_PROBE_ABILITY
  ) {
    
  };
  
  
}





//  TODO:  This won't be properly useful until you have 3-dimensional terrain
//  implemented.  Leave out for now.
/*
final public static ItemType CABLE_GUN = new ItemType(
  "Cable Gun", "item_cable_gun",
  "Launches a climbing cable over long distances, assisting infiltration "+
  "and escape.",
  ICONS_DIR+"icon_cable_gun.png",
  SPRITE_DIR+"sprite_batarang.png",
  SLOT_WEAPON, 80, new Object[] {
    ENGINEERING, 5
  },
  IS_WEAPON | IS_RANGED | IS_KINETIC, 0
) {
  public float passiveModifierFor(Person person, Trait trait) {
    //if (trait == GYMNASTICS) return 3;
    return 0;
  }
};
//*/





