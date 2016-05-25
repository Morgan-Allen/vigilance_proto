

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class PersonStats {
  
  
  final public static Skill
    INTELLECT  = new Skill(
      "Intellect", "stat_intellect",
      "Abstract logic, knowledge and planning ability."
    ),
    REFLEX     = new Skill(
      "Reflex", "stat_reflex",
      "Agility, sensory acuity and motor coordination."
    ),
    SOCIAL     = new Skill(
      "Social", "stat_social",
      "Charm, presence, and emotional awareness."
    ),
    STRENGTH   = new Skill(
      "Strength", "stat_strength",
      "Brute force, stature and muscle development."
    ),
    BASE_STATS[] = { INTELLECT, REFLEX, SOCIAL, STRENGTH },
    
    HIT_POINTS = new Skill("Hit Points", "stat_hit_points", ""),
    WILLPOWER  = new Skill("Willpower" , "stat_willpower" , ""),
    PHYS_STATS[] = { HIT_POINTS, WILLPOWER },
    
    ENGINEERING   = new Skill(
      "Engineering", "skill_eng",
      "Engineering skill allows an agent to construct and repair gadgets, "+
      "vehicles, and base facilities.",
      INTELLECT
    ),
    INFORMATICS   = new Skill(
      "Informatics", "skill_inf",
      "Informatics covers software engineering, data mining and encryption- "+
      "necessary for certain forms of research and advanced gadgetry.",
      INTELLECT
    ),
    PHARMACY      = new Skill(
      "Pharmacy", "skill_pha",
      "A knowledge of pharmacy allows a character to concoct vaccines and "+
      "medicines- or chemical weapons for their own use.",
      INTELLECT
    ),
    ANATOMY       = new Skill(
      "Anatomy", "skill_ant",
      "A knowledge of anatomy is essential to treatment of serious injury- "+
      "and can let you inflict crushing blows.",
      INTELLECT
    ),
    LAW_N_FINANCE = new Skill(
      "Law & Finance", "skill_law",
      "A knowledge of loopholes, regulations and wheels to grease helps to "+
      "navigate the corporate world and judicial process.",
      INTELLECT
    ),
    THE_OCCULT    = new Skill(
      "The Occult", "skill_occ",
      "There are some things man was not meant to know.",
      INTELLECT
    ),
    
    LANGUAGES     = new Skill(
      "Languages", "skill_lng",
      "A knowledge of spoken and written languages, both ancient and modern. "+
      "Often useful for research, travel, questioning or impersonation.",
      INTELLECT, SOCIAL
    ),
    QUESTION      = new Skill(
      "Question", "skill_que",
      "Used to obtain information from friendly or neutral persons, and spot "+
      "inconsistencies or gaps in the account.",
      INTELLECT, SOCIAL
    ),
    DISGUISE      = new Skill(
      "Disguise" , "skill_dis",
      "How to blend in or stand out through the use of wigs, props, costume "+
      "and cosmetics.  Used working undercover or leading a double life.",
      SOCIAL
    ),
    SUASION       = new Skill(
      "Suasion"  , "skill_sua",
      "Allows an agent to beg favours, bargain or advocate convincingly, "+
      "without resorting to force.",
      SOCIAL
    ),
    
    STEALTH       = new Skill(
      "Stealth", "skill_ste",
      "Allows an agent to slip past guards and surveillance systems unnoticed,"+
      "particularly after dark or with some cover.",
      REFLEX
    ),
    SURVEILLANCE  = new Skill(
      "Surveillance", "skill_sur",
      "Lets agents keep an eye out for suspicious activity, trail a suspect "+
      "or see through a ruse.",
      REFLEX
    ),
    VEHICLES      = new Skill(
      "Vehicles", "skill_veh",
      "Allows piloting of bikes, automobiles, or even jets & planes.",
      REFLEX
    ),
    MARKSMAN      = new Skill(
      "Marksman", "skill_mrk",
      "Permits the accurate and forceful use of projectile weapons- bows, "+
      "throwing stars, darts and guns.",
      REFLEX
    ),
    
    INTIMIDATE    = new Skill(
      "Intimidate", "skill_int",
      "A combination of physical brutality and menacing implications might "+
      "coax cooperation from stubborn suspects.",
      STRENGTH, SOCIAL
    ),
    GYMNASTICS    = new Skill(
      "Gymnastics", "skill_gym",
      "They might not dodge bullets, but a good gymnast can vault obstacles "+
      "to reach cover, escape injury, or reach inaccessible places.",
      STRENGTH, REFLEX
    ),
    CLOSE_COMBAT  = new Skill(
      "Close Combat", "skill_ccm",
      "An agent with close combat skills can deliver knockout blows, and "+
      "stands a better chance of disarming or cuffing a perp.",
      STRENGTH, REFLEX
    ),
    STAMINA       = new Skill(
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
    ALL_STATS[] = (Skill[]) Visit.compose(
      Skill.class, BASE_STATS, PHYS_STATS, ALL_SKILLS
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
  
  
  public float xpLevelFor(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) return 0;
    return l.practice / (l.level + 1f);
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
    
    for (Skill s : BASE_STATS) {
      updateStat(s, -1, true);
    }
    for (Skill s : PHYS_STATS) {
      updateStat(s, 0, false);
    }
    for (Skill s : ALL_SKILLS) {
      updateStat(s, -1, true);
    }
    
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
  
  
  protected boolean updateStat(Skill stat, float base, boolean mental) {
    Level l = levels.get(stat);
    if (l == null) return false;
    
    float rootBonus = 0;
    for (Skill root : stat.roots) {
      rootBonus += levelFor(root) / (3f * stat.roots.length);
    }
    
    l.bonus   = rootBonus;
    l.learned = mental   ;
    if (base > 0) l.level = base;
    return true;
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
  
  
  public void gainXP(Skill stat, float XP) {
    final Level l = getLevel(stat);
    l.practice += XP;
    while (l.practice >= l.level + 1) {
      l.practice -= l.level + 1;
      l.level++;
    }
    for (Skill root : stat.roots) {
      gainXP(root, XP / (3f * stat.roots.length));
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














