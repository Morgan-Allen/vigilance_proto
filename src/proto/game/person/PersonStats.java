

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class PersonStats {
  
  
  public static class Stat extends Trait {
    
    final public Stat roots[];
    
    public Stat(String name, String ID, String description, Stat... roots) {
      super(name, ID, description);
      this.roots = roots;
    }
  }
  
  final public static Stat
    INTELLECT  = new Stat(
      "Intellect", "stat_intellect",
      "Abstract logic, knowledge and planning ability."
    ),
    REFLEX     = new Stat(
      "Reflex", "stat_reflex",
      "Agility, sensory acuity and motor coordination."
    ),
    SOCIAL     = new Stat(
      "Social", "stat_social",
      "Charm, presence, and emotional awareness."
    ),
    STRENGTH   = new Stat(
      "Strength", "stat_strength",
      "Brute force, stature and muscle development."
    ),
    BASE_STATS[] = { INTELLECT, REFLEX, SOCIAL, STRENGTH },
    
    HIT_POINTS = new Stat("Hit Points", "stat_hit_points", ""),
    WILLPOWER  = new Stat("Willpower" , "stat_willpower" , ""),
    PHYS_STATS[] = { HIT_POINTS, WILLPOWER },
    
    ENGINEERING   = new Stat(
      "Engineering", "skill_eng",
      "Engineering skill allows an agent to construct and repair gadgets, "+
      "vehicles, and base facilities.",
      INTELLECT
    ),
    INFORMATICS   = new Stat(
      "Informatics", "skill_inf",
      "Informatics covers software engineering, data mining and encryption- "+
      "necessary for certain forms of research and advanced gadgetry.",
      INTELLECT
    ),
    PHARMACY      = new Stat(
      "Pharmacy", "skill_pha",
      "A knowledge of pharmacy allows a character to concoct vaccines and "+
      "medicines- or chemical weapons for their own use.",
      INTELLECT
    ),
    ANATOMY       = new Stat(
      "Anatomy", "skill_ant",
      "A knowledge of anatomy is essential to treatment of serious injury- "+
      "and can let you inflict crushing blows.",
      INTELLECT
    ),
    LAW_N_FINANCE = new Stat(
      "Law & Finance", "skill_law",
      "A knowledge of loopholes, regulations and wheels to grease helps to "+
      "navigate the corporate world and judicial process.",
      INTELLECT
    ),
    THE_OCCULT    = new Stat(
      "The Occult", "skill_occ",
      "There are some things man was not meant to know.",
      INTELLECT
    ),
    
    LANGUAGES     = new Stat(
      "Languages", "skill_lng",
      "A knowledge of spoken and written languages, both ancient and modern. "+
      "Often useful for research, travel, questioning or impersonation.",
      INTELLECT, SOCIAL
    ),
    QUESTION      = new Stat(
      "Question", "skill_que",
      "Used to obtain information from friendly or neutral persons, and spot "+
      "inconsistencies or gaps in the account.",
      INTELLECT, SOCIAL
    ),
    DISGUISE      = new Stat(
      "Disguise" , "skill_dis",
      "How to blend in or stand out through the use of wigs, props, costume "+
      "and cosmetics.  Used working undercover or leading a double life.",
      SOCIAL
    ),
    SUASION       = new Stat(
      "Suasion"  , "skill_sua",
      "Allows an agent to beg favours, bargain or advocate convincingly, "+
      "without resorting to force.",
      SOCIAL
    ),
    
    STEALTH       = new Stat(
      "Stealth", "skill_ste",
      "Allows an agent to slip past guards and surveillance systems unnoticed,"+
      "particularly after dark or with some cover.",
      REFLEX
    ),
    SURVEILLANCE  = new Stat(
      "Surveillance", "skill_sur",
      "Lets agents keep an eye out for suspicious activity, trail a suspect "+
      "or see through a ruse.",
      REFLEX
    ),
    VEHICLES      = new Stat(
      "Vehicles", "skill_veh",
      "Allows piloting of bikes, automobiles, or even jets & planes.",
      REFLEX
    ),
    MARKSMAN      = new Stat(
      "Marksman", "skill_mrk",
      "Permits the accurate and forceful use of projectile weapons- bows, "+
      "throwing stars, darts and guns.",
      REFLEX
    ),
    
    INTIMIDATE    = new Stat(
      "Intimidate", "skill_int",
      "A combination of physical brutality and menacing implications might "+
      "coax cooperation from stubborn suspects.",
      STRENGTH, SOCIAL
    ),
    GYMNASTICS    = new Stat(
      "Gymnastics", "skill_gym",
      "They might not dodge bullets, but a good gymnast can vault obstacles "+
      "to reach cover, escape injury, or reach inaccessible places.",
      STRENGTH, REFLEX
    ),
    CLOSE_COMBAT  = new Stat(
      "Close Combat", "skill_ccm",
      "An agent with close combat skills can deliver knockout blows, and "+
      "stands a better chance of disarming or cuffing a perp.",
      STRENGTH, REFLEX
    ),
    STAMINA       = new Stat(
      "Stamina", "skill_sta",
      "Sheer physical endurance may be the only way to weather certain "+
      "trials, work through pain, or survive an injury.",
      STRENGTH
    ),
    
    ALL_SKILLS[] = {
      ENGINEERING, INFORMATICS, PHARMACY, ANATOMY, LAW_N_FINANCE, THE_OCCULT,
      LANGUAGES , QUESTION    , DISGUISE    , SUASION ,
      STEALTH   , SURVEILLANCE, VEHICLES    , MARKSMAN,
      INTIMIDATE, GYMNASTICS  , CLOSE_COMBAT, STAMINA
    },
    ALL_STATS[] = (Stat[]) Visit.compose(
      Stat.class, BASE_STATS, PHYS_STATS, ALL_SKILLS
    )
  ;
  
  
  final Person person;
  
  List <Ability> abilities = new List();
  class Level {
    float level, practice, bonus;
    boolean learned;
  }
  Table <Trait, Level> levels = new Table();
  
  
  PersonStats(Person p) {
    person = p;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(abilities);
    for (int n = s.loadInt(); n-- > 0;) {
      Trait key = (Trait) s.loadObject();
      Level l = new Level();
      l.level    = s.loadFloat();
      l.practice = s.loadFloat();
      l.bonus    = s.loadFloat();
      l.learned  = s.loadBool ();
      levels.put(key, l);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(abilities);
    s.saveInt(levels.size());
    for (Trait a : levels.keySet()) {
      Level l = levels.get(a);
      s.saveObject(a);
      s.saveFloat(l.level   );
      s.saveFloat(l.practice);
      s.saveFloat(l.bonus   );
      s.saveBool (l.learned );
    }
  }
  
  
  
  /**  General statistical queries-
    */
  public Series <Ability> learnedAbilities() {
    return abilities;
  }
  
  
  public int levelFor(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) return 0;
    return (int) (l.level + l.bonus);
  }
  
  
  public int xpFor(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) return 0;
    return (int) l.practice;
  }
  
  
  public int bonusFor(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) return 0;
    return (int) l.bonus;
  }
  
  
  public Series <Ability> listAbilities() {
    //  TODO:  I'm not certain, at the moment, if this shouldn't be considered
    //  a UI method.  Check.
    
    Batch <Ability> all = new Batch();
    for (Ability a : abilities) all.add(a);
    for (Equipped e : person.equipSlots) if (e != null) {
      for (Ability a : e.abilities) all.add(a);
    }
    return all;
  }
  
  
  
  /**  Regular updates-
    */
  void initStats() {
    for (Trait s : person.kind.baseTraits()) {
      float base = person.kind().baseLevel(s);
      setLevel(s, base, true);
    }
    updateStats();
  }
  
  
  void updateStats() {
    
    
    /*
    float    muscle = levelFor(MUSCLE);
    float    reflex = levelFor(REFLEX);
    float    brain  = levelFor(BRAIN );
    float    will   = levelFor(WILL  );
    Equipped armour = person.currentArmour();
    Equipped weapon = person.currentWeapon();
    
    float maxHP      = 10 + muscle;
    float maxEnergy  = (muscle + will) / 2f;
    float baseArmour = armour.bonus;
    float baseRegen  = muscle / 100f;
    float baseDamage = weapon.bonus / 2f;
    float rollDamage = weapon.bonus / 2f;
    
    setStatBase(HIT_POINTS, maxHP     , false);
    setStatBase(ENERGY    , maxEnergy , false);
    setStatBase(ARMOUR    , baseArmour, false);
    setStatBase(REGEN     , baseRegen , false);
    setStatBase(MIN_DAMAGE, baseDamage, false);
    setStatBase(RNG_DAMAGE, rollDamage, false);
    
    float lightLevel = (reflex + 10) / 2;
    setStatBase(SIGHT    , lightLevel, true);
    setStatBase(SPEED_MOV, lightLevel, true);
    setStatBase(SPEED_ACT, lightLevel, true);
    setStatBase(PRECISION, lightLevel, true);
    setStatBase(RESTRAINT, lightLevel, true);
    setStatBase(DODGE    , lightLevel, true);
    setStatBase(PARRY    , lightLevel, true);
    setStatBase(STEALTH  , lightLevel, true);
    
    float levelQuestion = (will + brain      ) / 2;
    float levelSuasion  = (10 + will + reflex) / 2;
    setStatBase(QUESTION, levelQuestion, true);
    setStatBase(SUASION , levelSuasion , true);
    //*/
    
    Series <Ability> abilities = listAbilities();
    for (Ability a : abilities) if (a.passive()) {
      a.applyPassiveStatsBonus(person);
    }
  }
  
  
  protected Level getLevel(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) levels.put(trait, l = new Level());
    return l;
  }
  
  
  protected void setStatBase(Stat stat, float level, boolean mental) {
    final Level l = getLevel(stat);
    final int minVal = person.kind().baseLevel(stat);
    if (level < minVal) level = minVal;
    
    l.learned = mental;
    l.level   = level ;
    l.bonus   = 0     ;
  }
  
  
  protected void incBonus(Trait trait, float bonusMod) {
    final Level l = getLevel(trait);
    l.bonus += bonusMod;
  }
  
  
  protected void applyStatEffects() {
    /*
    final float regen = levelFor(REGEN);
    if      (person.stun  () > 0) person.liftStun  (regen * 2);
    else if (person.injury() > 0) person.liftInjury(regen * 1);
    //*/
  }
  
  
  
  /**  Assigning experience and abilities-
    */
  public void setLevel(Trait a, float level, boolean learned) {
    final boolean keep = learned && a instanceof Ability;
    
    if (level > 0) {
      final Level l = getLevel(a);
      l.level    = level;
      l.practice = 0;
      l.learned  = learned;
      if (keep) abilities.include((Ability) a);
    }
    else {
      levels.remove(a);
      if (keep) abilities.remove((Ability) a);
    }
  }
  
  
  public void gainXP(Stat stat, int XP) {
    final Level l = getLevel(stat);
    l.practice += XP;
    while (l.practice >= l.level + 1) {
      l.practice -= l.level + 1;
      l.level++;
    }
  }
  
  
  public void toggleItemAbilities(Equipped item, boolean active) {
    if (item == null) return;
    for (Ability a : item.abilities) {
      if (! a.equipped()) continue;
      setLevel(a, active ? 1 : 0, false);
    }
  }
}














