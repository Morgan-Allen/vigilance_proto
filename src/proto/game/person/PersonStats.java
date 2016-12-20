

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class PersonStats {
  
  final static String
    ICON_PATH = "media assets/stat icons/"
  ;
  
  final public static Trait
    MUSCLE = new Trait(
      "Muscle", "stat_strength", ICON_PATH+"icon_strength.png",
      "Brute force, stature and strength development."
    ),
    REFLEXES = new Trait(
      "Reflexes", "stat_reflex", ICON_PATH+"icon_reflex.png",
      "Agility, sensory acuity and motor coordination."
    ),
    WILL = new Trait(
      "Will", "stat_will", ICON_PATH+"icon_social.png",
      "Sheer bloody-minded persistence."
    ),
    BRAINS = new Trait(
      "Brains", "stat_brains", ICON_PATH+"icon_intellect.png",
      "Abstract logic, knowledge and planning ability."
    ),
    BASE_STATS[] = { MUSCLE, REFLEXES, WILL, BRAINS },
    
    ARMOUR = new Trait(
      "Armour", "stat_armour", null, ""
    ),
    HEALTH = new Trait(
      "Health", "stat_health", null, ""
    ),
    MIN_DAMAGE = new Trait(
      "Damage Base", "stat_min_damage", null, ""
    ),
    RNG_DAMAGE = new Trait(
      "Damage Range", "stat_rng_damage", null, ""
    ),
    ACCURACY = new Trait(
      "Accuracy", "stat_accuracy", null, ""
    ),
    DEFENCE = new Trait(
      "Defence", "stat_defence", null, ""
    ),
    SIGHT_RANGE = new Trait(
      "Sight Range", "stat_sight_range", null, ""
    ),
    STEALTH = new Trait(
      "Stealth", "stat_stealth", null, ""
    ),
    MOVE_SPEED = new Trait(
      "Move Speed", "stat_move_speed", null, ""
    ),
    ACT_SPEED = new Trait(
      "Act Speed", "stat_act_speed", null, ""
    ),
    COMBAT_STATS[] = {
      ARMOUR, HEALTH, MIN_DAMAGE, RNG_DAMAGE,
      ACCURACY, DEFENCE, SIGHT_RANGE, STEALTH, MOVE_SPEED, ACT_SPEED
    },
    
    ENGINEERING = new Skill(
      "Engineering", "skill_engineering", ICON_PATH+"icon_engineering.png",
      "Engineering skill allows an agent to construct and repair gadgets, "+
      "vehicles, and base facilities.",
      BRAINS
    ),
    MEDICINE = new Skill(
      "Medicine", "skill_medicine", ICON_PATH+"icon_pharmacy.png",
      "A knowledge of pharmacy allows a character to concoct vaccines and "+
      "medicines- or chemical weapons for their own use.",
      BRAINS
    ),
    QUESTION      = new Skill(
      "Question", "skill_question", ICON_PATH+"icon_social.png",
      "Used to obtain information from friendly or neutral persons, and spot "+
      "inconsistencies or gaps in the account.",
      WILL
    ),
    PERSUADE = new Skill(
      "Persuade", "skill_persuade", ICON_PATH+"icon_social.png",
      "Allows an agent to beg favours, bargain or advocate convincingly, "+
      "without resorting to force.",
      WILL
    ),
    SKILL_STATS[] = { ENGINEERING, MEDICINE, QUESTION, PERSUADE },
    
    ALL_STATS[] = (Trait[]) Visit.compose(
      Trait.class, BASE_STATS, COMBAT_STATS, SKILL_STATS
    );
  
  
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
    for (ItemType e : person.gear.equipSlots) if (e != null) {
      for (Ability a : e.abilities) all.add(a);
    }
    return all;
  }
  
  
  public float powerLevel() {
    //  TODO:  Refine this!
    if (person.isHero    ()) return 4;
    if (person.isCriminal()) return 1;
    return 0;
  }
  
  
  
  /**  Regular updates-
    */
  void initStats() {
    for (Trait s : person.kind.baseTraits()) {
      float base = person.kind().baseLevel(s);
      setLevel(s, base, true);
    }
    for (Ability a : Common.BASIC_ABILITIES) {
      setLevel(a, 1, true);
    }
    updateStats();
  }
  
  
  void updateStats() {
    
    //I.say("Updating stats for: "+person);
    for (Trait t : COMBAT_STATS) {
      updateStat(t, 0, false);
    }
    for (Trait s : BASE_STATS) {
      updateStat(s, -1, true);
    }
    for (Trait s : SKILL_STATS) {
      updateStat(s, -1, true);
    }
    
    Series <Ability> abilities = listAbilities();
    for (Ability a : abilities) if (a.passive()) {
      a.applyPassiveStatsBonus(person);
    }
    for (ItemType i : person.gear.equipment()) {
      i.applyPassiveStatsBonus(person);
    }
  }
  
  
  protected Level getLevel(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) levels.put(trait, l = new Level());
    return l;
  }
  
  
  protected boolean updateStat(Trait stat, float base, boolean mental) {
    Level l = levels.get(stat);
    if (l == null) return false;
    
    float rootBonus = 0;
    for (Trait root : stat.roots()) {
      rootBonus += levelFor(root) / (3f * stat.roots().length);
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
  
  
  public void gainXP(Trait stat, float XP) {
    final Level l = getLevel(stat);
    l.practice += XP;
    
    while (l.practice >= l.level + 1) {
      l.practice -= l.level + 1;
      l.level++;
      person.world().events.log(person+" reached level "+l.level+" in "+stat);
    }
    for (Trait root : stat.roots()) {
      gainXP(root, XP / (3f * stat.roots().length));
    }
  }
  
  
  public void toggleItemAbilities(ItemType item, boolean active) {
    if (item == null) return;
    for (Ability a : item.abilities) {
      if (! a.equipped()) continue;
      setLevel(a, active ? 1 : 0, false);
    }
  }
}














