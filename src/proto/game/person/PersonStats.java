

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class PersonStats {
  
  
  static class Stat extends Trait {
    
    final Stat roots[];
    
    public Stat(String name, String ID, String description, Stat... roots) {
      super(name, ID, description);
      this.roots = roots;
    }
  }
  
  final public static Stat
    INTELLECT  = new Stat("Intellect" , "stat_intellect", ""),
    REFLEX     = new Stat("Reflex"    , "stat_reflex"   , ""),
    SOCIAL     = new Stat("Social"    , "stat_social"   , ""),
    STRENGTH   = new Stat("Strength"  , "stat_strength" , ""),
    BASE_STATS[] = { INTELLECT, REFLEX, SOCIAL, STRENGTH },
    
    HIT_POINTS = new Stat("Hit Points", "stat_hit_points", ""),
    WILLPOWER  = new Stat("Willpower" , "stat_willpower" , ""),
    PHYS_STATS[] = { HIT_POINTS, WILLPOWER },
    
    ENGINEERING   = new Stat("Engineering"  , "skill_eng", "", INTELLECT),
    INFORMATICS   = new Stat("Informatics"  , "skill_inf", "", INTELLECT),
    PHARMACY      = new Stat("Pharmacy"     , "skill_pha", "", INTELLECT),
    ANATOMY       = new Stat("Anatomy"      , "skill_ant", "", INTELLECT),
    LAW_N_FINANCE = new Stat("Law & Finance", "skill_law", "", INTELLECT),
    THE_OCCULT    = new Stat("The Occult"   , "skill_occ", "", INTELLECT),
    
    LANGUAGES     = new Stat("Languages", "skill_lng", "", INTELLECT, SOCIAL),
    QUESTION      = new Stat("Question" , "skill_que", "", INTELLECT, SOCIAL),
    DISGUISE      = new Stat("Disguise" , "skill_dis", "", SOCIAL),
    SUASION       = new Stat("Suasion"  , "skill_sua", "", SOCIAL),
    
    STEALTH       = new Stat("Stealth"     , "skill_ste", "", REFLEX),
    SURVEILLANCE  = new Stat("Surveillance", "skill_sur", "", REFLEX),
    VEHICLES      = new Stat("Vehicles"    , "skill_veh", "", REFLEX),
    MARKSMAN      = new Stat("Marksman"    , "skill_mrk", "", REFLEX),
    
    INTIMIDATE    = new Stat("Intimidate"  , "skill_int", "", STRENGTH, SOCIAL),
    GYMNASTICS    = new Stat("Gymnastics"  , "skill_gym", "", STRENGTH, REFLEX),
    CLOSE_COMBAT  = new Stat("Close Combat", "skill_ccm", "", STRENGTH, REFLEX),
    STAMINA       = new Stat("Stamina"     , "skill_sta", "", STRENGTH),
    
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
  
  int totalXP = 0;
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
    totalXP = s.loadInt();
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
    s.saveInt(totalXP);
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
  
  
  protected void setStatBase(Stat stat, float level, boolean mental) {
    Level l = levels.get(stat);
    if (l == null) levels.put(stat, l = new Level());
    
    final int minVal = person.kind().baseLevel(stat);
    if (level < minVal) level = minVal;
    
    l.learned = mental;
    l.level   = level ;
    l.bonus   = 0     ;
  }
  
  
  protected void incBonus(Trait trait, float bonusMod) {
    Level l = levels.get(trait);
    if (l == null) levels.put(trait, l = new Level());
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
      Level l = levels.get(a);
      if (l == null) levels.put(a, l = new Level());
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
  
  
  public void gainXP(int XP) {
    totalXP += XP;
  }
  
  
  public void toggleItemAbilities(Equipped item, boolean active) {
    if (item == null) return;
    for (Ability a : item.abilities) {
      if (! a.equipped()) continue;
      setLevel(a, active ? 1 : 0, false);
    }
  }
}














