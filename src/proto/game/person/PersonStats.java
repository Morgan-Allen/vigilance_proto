

package proto.game.person;
import proto.game.scene.*;
import proto.game.world.*;
import proto.common.*;
import proto.util.*;



public class PersonStats {
  
  final static String
    ICON_PATH = "media assets/stat icons/"
  ;
  
  final public static Trait
    MUSCLE = new Trait(
      "Muscle", "stat_strength", null,
      "Brute force, stature and strength development."
    ),
    REFLEXES = new Trait(
      "Reflexes", "stat_reflex", null,
      "Agility, sensory acuity and motor coordination."
    ),
    WILL = new Trait(
      "Will", "stat_will", null,
      "Sheer bloody-minded persistence."
    ),
    BRAINS = new Trait(
      "Brains", "stat_brains", null,
      "Abstract logic, knowledge and planning ability."
    ),
    BASE_STATS[] = { MUSCLE, REFLEXES, WILL, BRAINS },
    
    //  TODO:  Also add Regeneration and Energy?
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
    HIDE_RANGE = new Trait(
      "Hide Range", "stat_stealth", null, ""
    ),
    MOVE_SPEED = new Trait(
      "Move Speed", "stat_move_speed", null, ""
    ),
    ACT_POINTS = new Trait(
      "Action Points", "stat_act_speed", null, ""
    ),
    COMBAT_STATS[] = {
      ARMOUR, HEALTH, MIN_DAMAGE, RNG_DAMAGE,
      ACCURACY, DEFENCE, SIGHT_RANGE, HIDE_RANGE, MOVE_SPEED, ACT_POINTS
    },
    
    ENGINEERING = new Trait(
      "Engineering", "skill_engineering", ICON_PATH+"icon_engineering.png",
      "Engineering skill allows an agent to construct and repair gadgets, "+
      "vehicles, and base facilities.",
      BRAINS
    ),
    MEDICINE = new Trait(
      "Medicine", "skill_medicine", ICON_PATH+"icon_pharmacy.png",
      "A knowledge of pharmacy allows a character to concoct vaccines and "+
      "medicines- or chemical weapons for their own use.",
      BRAINS
    ),
    QUESTION = new Trait(
      "Question", "skill_question", ICON_PATH+"icon_social.png",
      "Used to obtain information from friendly or neutral persons, and spot "+
      "inconsistencies or gaps in the account.",
      WILL
    ),
    PERSUADE = new Trait(
      "Persuade", "skill_persuade", ICON_PATH+"icon_social.png",
      "Allows an agent to beg favours, bargain or advocate convincingly, "+
      "without resorting to force.",
      WILL
    ),
    INVESTMENT = new Trait(
      "Investment", "skill_investment", ICON_PATH+"icon_invest.png",
      "",
      BRAINS
    ),
    MYTH_AND_RELIGION = new Trait(
      "Myth and Religion", "skill_myth_and_religion", ICON_PATH+"icon_myth.png",
      "",
      BRAINS
    ),
    SKILL_STATS[] = {
      ENGINEERING, MEDICINE, QUESTION, PERSUADE, INVESTMENT, MYTH_AND_RELIGION
    },
    ALL_STATS[] = (Trait[]) Visit.compose(
      Trait.class, BASE_STATS, COMBAT_STATS, SKILL_STATS
    );
  
  
  final Person person;
  List <Ability> abilities = new List();
  
  static class Level {
    float level, xpGained, bonus;
    boolean learned;
  }
  Table <Trait, Level> levels = new Table();
  
  static class Condition {
    //  TODO:  Just refer to the source action instead.
    Ability basis;
    Person casts;
    int countdown;
    
    public String toString() { return basis+" ("+countdown+" turns)"; }
  }
  List <Condition> conditions = new List();
  
  
  PersonStats(Person p) {
    person = p;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(abilities);
    for (int n = s.loadInt(); n-- > 0;) {
      Trait key = (Trait) s.loadObject();
      Level l = new Level();
      l.level    = s.loadFloat();
      l.xpGained = s.loadFloat();
      l.bonus    = s.loadFloat();
      l.learned  = s.loadBool ();
      levels.put(key, l);
    }
    for (int n = s.loadInt(); n-- > 0;) {
      Condition c = new Condition();
      c.basis = (Ability) s.loadObject();
      c.casts = (Person ) s.loadObject();
      c.countdown = s.loadInt();
      conditions.add(c);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(abilities);
    s.saveInt(levels.size());
    for (Trait a : levels.keySet()) {
      Level l = levels.get(a);
      s.saveObject(a);
      s.saveFloat(l.level   );
      s.saveFloat(l.xpGained);
      s.saveFloat(l.bonus   );
      s.saveBool (l.learned );
    }
    s.saveInt(conditions.size());
    for (Condition c : conditions) {
      s.saveObject(c.basis);
      s.saveObject(c.casts);
      s.saveInt(c.countdown);
    }
  }
  
  
  void initStats() {
    for (Trait s : person.kind.baseTraits()) {
      float base = person.kind().baseLevel(s);
      setLevel(s, base, true);
    }
    updateStats(0);
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
    return l.xpGained / trait.xpRequired((int) l.level);
  }
  
  
  public int bonusFor(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) return 0;
    return (int) l.bonus;
  }
  
  
  
  /**  Specific numeric calculations:
    */
  public float powerLevel() {
    //  TODO:  Refine this!
    if (person.isHero    ()) return 4;
    if (person.isCriminal()) return 1;
    return 0;
  }
  
  
  public float sightRange() {
    return levelFor(SIGHT_RANGE);
  }
  
  
  public float hidingRange() {
    return levelFor(HIDE_RANGE);
  }
  
  
  public int maxActionPoints() {
    return (int) levelFor(ACT_POINTS);
  }
  
  
  public float tilesMotionPerAP() {
    return levelFor(MOVE_SPEED) * 1f / maxActionPoints();
  }
  
  
  
  /**  Regular updates-
    */
  void updateStats(float numWeeks) {
    
    //  These are all assumed to have averages of 5, ranging from 0 to 10.
    updateStat(MUSCLE  , -1, false);
    updateStat(REFLEXES, -1, false);
    updateStat(BRAINS  , -1, true );
    updateStat(WILL    , -1, true );
    float muscle   = levelFor(MUSCLE  );
    float reflexes = levelFor(REFLEXES);
    
    //  Average hit point total is 9.  Damage and armour start at 0.
    float maxHP = 4 + (muscle * 1);
    //  TODO:  Make this more tweakable later...
    if (GameSettings.EASY_MODE && person.isHero()) maxHP *= 1.6f;
    
    updateStat(HEALTH    , maxHP, false);
    //  TODO:  Incorporate these.
    //float maxEnergy  = (muscle + will) / 2f;
    //float baseRegen  = muscle / 100f;
    updateStat(ARMOUR    , 0, false);
    updateStat(MIN_DAMAGE, 0, false);
    updateStat(RNG_DAMAGE, 0, false);
    
    //  Average sight range is 5.  Average hide range is 2.5.
    float sightRange = (reflexes + 5) / 2;
    float hideRange  = sightRange / 2;
    updateStat(SIGHT_RANGE, sightRange, true);
    updateStat(HIDE_RANGE , hideRange , true);
    
    //  Average action points is 4.  Average move speed is 11.
    float actPoints = 2 + 2;
    float moveSpeed = 6 + (reflexes / 1);
    updateStat(ACT_POINTS , actPoints, true);
    updateStat(MOVE_SPEED , moveSpeed, true);
    
    //  Average accuracy is 50%.  Average defence is 15%.
    float accLevel = (5 + reflexes) * 5;
    float defLevel = 5 * (int) (reflexes / 2);
    updateStat(ACCURACY   , accLevel, true);
    updateStat(DEFENCE    , defLevel, true);
    
    //  Skill stats are initialised independently.
    for (Trait skill : SKILL_STATS) updateStat(skill, -1, true);
    
    Series <Ability> abilities = person.actions.listAbilities();
    for (Ability a : abilities) if (a.passive()) {
      a.applyPassiveStatsBonus(person);
    }
    for (Item i : person.gear.equipped()) {
      if (i.kind().slotType == PersonGear.SLOT_TYPE_WEAPON) {
        if (i != person.gear.weaponPicked) continue;
      }
      i.kind().applyPassiveStatsBonus(person);
    }
    for (Condition c : conditions) {
      c.basis.applyConditionStatsBonus(person);
    }
  }
  
  
  protected Level getLevel(Trait trait) {
    Level l = levels.get(trait);
    if (l == null) levels.put(trait, l = new Level());
    return l;
  }
  
  
  protected boolean updateStat(Trait stat, float base, boolean mental) {
    Level l = getLevel(stat);
    
    float rootBonus = 0;
    for (Trait root : stat.roots()) {
      rootBonus += levelFor(root) / (3f * stat.roots().length);
    }
    
    l.bonus   = rootBonus;
    l.learned = mental   ;
    if (base >= 0) l.level = base;
    return true;
  }
  
  
  protected void incBonus(Trait trait, float bonusMod) {
    final Level l = getLevel(trait);
    l.bonus += bonusMod;
  }
  
  
  public void refreshCooldowns() {
    //  TODO:  Implement this once cooldowns are available...
  }
  
  
  boolean onTurnStart() {
    for (Condition c : conditions) {
      c.countdown--;
      if (c.countdown <= 0) conditions.remove(c);
      else c.basis.applyConditionOnTurn(person, c.casts);
    }
    //  TODO:  Restore these.
    /*
    final float regen = levelFor(REGEN);
    if      (person.stun  () > 0) person.liftStun  (regen * 2);
    else if (person.injury() > 0) person.liftInjury(regen * 1);
    //*/
    return true;
  }
  
  
  boolean onTurnEnd() {
    return true;
  }
  
  
  
  /**  Assigning conditions-
    */
  public void applyCondition(Ability basis, Person source, int duration) {
    Condition match = conditionMatching(basis, source);
    if (match == null) conditions.add(match = new Condition());
    match.basis = basis;
    match.casts = source;
    match.countdown = Nums.max(duration, match.countdown);
  }
  
  
  public void removeCondition(Ability basis, Person source) {
    Condition match = conditionMatching(basis, source);
    if (match != null) conditions.remove(match);
  }
  
  
  Condition conditionMatching(Ability basis, Person source) {
    for (Condition c : conditions) {
      if (c.basis == basis && c.casts == source) return c;
    }
    return null;
  }
  
  
  public Series <Trait> allConditions() {
    Batch <Trait> all = new Batch();
    for (Condition c : conditions) {
      all.add(c.basis);
    }
    Action taken = person.actions.nextAction();
    if (taken != null && (taken.used.delayed() || ! taken.complete())) {
      all.add(taken.used);
    }
    if (person.health.bleeding()) {
      all.add(Common.BLEEDING);
    }
    return all;
  }
  
  
  
  /**  Assigning experience and abilities-
    */
  public void setLevel(Trait a, float level, boolean learned) {
    final boolean keep = learned && a instanceof Ability;
    
    if (level > 0) {
      final Level l = getLevel(a);
      l.level    = level;
      l.xpGained = 0;
      l.learned  = learned;
      if (keep) abilities.include((Ability) a);
    }
    else {
      levels.remove(a);
      if (keep) abilities.remove((Ability) a);
    }
  }
  
  
  public void addTrait(Trait t) {
    if (levelFor(t) > 0) return;
    setLevel(t, 1, false);
  }
  
  
  public void gainXP(Trait stat, float XP) {
    final Level l = getLevel(stat);
    l.xpGained += XP;
    
    while (true) {
      final float xpNeed = stat.xpRequired((int) l.level);
      if (l.xpGained < xpNeed) break;
      l.xpGained -= xpNeed;
      setLevel(stat, l.level + 1, true);
      person.world().events.log(person+" reached level "+l.level+" in "+stat);
    }
  }
}














