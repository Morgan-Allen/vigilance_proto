
package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;


//
//  Analogous to the Volley class, but for Tasks instead of Actions.

public class Attempt implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Object
    BONUS_SPEED = "speed",
    BONUS_TRAIT = "trait";
  
  final Task source;
  
  static class Test {
    Trait tested;
    int testRange;
    int testDC;
    int testTotal, testResult = -1;
  }
  Batch <Test> tests = new Batch();
  float speedBonus = 0;
  int   testBonus  = 0;
  
  int attemptDuration;
  Batch <Person> attempting = new Batch();
  boolean complete = false;
  
  //  Note- we don't save/load this for the moment, since it's purely used for
  //  UI-display purposes.
  public static class Modifier {
    public Object bonus;
    public Object source;
    public float modValue;
    
    public String toString() {
      return bonus+" "+modValue+" ("+source+")";
    }
  }
  private Stack <Modifier> modifiers = new Stack();
  
  
  public Attempt(Task source) {
    this.source = source;
  }
  
  
  public Attempt(Session s) throws Exception {
    s.cacheInstance(this);
    source = (Task) s.loadObject();
    for (int n = s.loadInt(); n-- > 0;) {
      Test t = new Test();
      t.tested     = (Trait) s.loadObject();
      t.testRange  = s.loadInt();
      t.testDC     = s.loadInt();
      t.testTotal  = s.loadInt();
      t.testResult = s.loadInt();
      tests.add(t);
    }
    speedBonus = s.loadFloat();
    testBonus  = s.loadInt  ();
    attemptDuration = s.loadInt();
    s.loadObjects(attempting);
    complete = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(source);
    s.saveInt(tests.size());
    for (Test t : tests) {
      s.saveObject(t.tested);
      s.saveInt(t.testRange );
      s.saveInt(t.testDC    );
      s.saveInt(t.testTotal );
      s.saveInt(t.testResult);
    }
    s.saveFloat(speedBonus);
    s.saveInt  (testBonus );
    s.saveInt(attemptDuration);
    s.saveObjects(attempting);
    s.saveBool(complete);
  }
  
  
  
  /**  Initial setup and configuration-
    */
  public void setupFromArgsList(int testRange, Object... args) {
    final int numT = args.length / 2;
    for (int n = 0; n < numT; n++) {
      Trait tested = (Trait  ) args[ n * 2     ];
      int   testDC = (Integer) args[(n * 2) + 1];
      addTest(tested, testRange, testDC);
    }
  }
  
  
  public void addTest(Trait trait, int range, int DC) {
    if (trait == null) return;
    Test test = testFor(trait);
    if (test == null) tests.add(test = new Test());
    
    test.tested    = trait;
    test.testRange = range;
    test.testDC    = DC   ;
  }
  
  
  public void grantModifier(Object bonus, float inc, Object source) {
    Modifier match = null;
    for (Modifier m : modifiers) if (m.bonus == bonus && m.source == source) {
      match = m;
      break;
    }
    if (match == null) modifiers.add(match = new Modifier());
    match.bonus    = bonus ;
    match.modValue = inc   ;
    match.source   = source;
    
    if (bonus == BONUS_SPEED) this.speedBonus += inc;
    if (bonus == BONUS_TRAIT) this.testBonus  += inc;
  }
  
  
  public void setAssigned(Series <Person> assigned) {
    attempting.clear();
    Visit.appendTo(attempting, assigned);
    
    //  TODO:  Grant stat/speed bonuses from distinct active abilities.
    /*
    for (Person p : attempting) for (Ability a : p.actions.listAbilities()) {
      
    }
    //*/
  }
  
  
  public void grantFacilityModifiers(Base owns) {
    for (Region r : owns.world().regions()) {
      for (Place p : r.buildSlots()) if (p != null && p.owner() == owns) {
        PlaceType type = p.kind();
        float speed = type.speedBonus(this);
        float trait = type.traitBonus(this);
        if (speed != 0) grantModifier(BONUS_SPEED, speed, type);
        if (trait != 0) grantModifier(BONUS_TRAIT, trait, type);
      }
    }
  }
  
  
  
  /**  Other information-
    */
  Test testFor(Trait trait) {
    for (Test t : tests) if (t.tested == trait) return t;
    return null;
  }
  
  
  public Task source() {
    return source;
  }
  
  
  public boolean physical() {
    for (Test t : tests) for (Trait s : t.tested.roots()) {
      if (s == REFLEXES || s == MUSCLE) return true;
    }
    return false;
  }
  
  
  public boolean mental() {
    for (Test t : tests) for (Trait s : t.tested.roots()) {
      if (s == WILL || s == BRAINS) return true;
    }
    return false;
  }
  
  
  public boolean needsSkill(Trait s) {
    for (Test t : tests) if (t.tested == s) return true;
    return false;
  }
  
  
  public boolean complete() {
    return complete;
  }
  
  
  
  /**  Chance-assessment and test-execution-
    */
  public float testChance(Trait tested) {
    Test test = testFor(tested);
    if (test == null) return -1;
    
    float maxLevel = 0, sumLevels = 0;
    for (Person p : attempting) {
      float level = p.stats.levelFor(tested);
      maxLevel = Nums.max(level, maxLevel);
      sumLevels += level;
    }
    
    int range = test.testRange, DC = test.testDC - (range / 2);
    float checkLevel = maxLevel + ((sumLevels - maxLevel) / 2) + testBonus;
    float winChance = Nums.clamp((checkLevel - DC) / range, 0, 1);
    
    test.testTotal = (int) checkLevel;
    return winChance;
  }
  
  
  public float testChance() {
    float chance = 1.0f;
    for (Test t : tests) {
      chance *= testChance(t.tested);
    }
    return chance;
  }
  
  
  public int performAttempt(int outcomeRange) {
    int outcome = outcomeRange;
    float xpRate = attemptDuration * 1f / World.MINUTES_PER_HOUR;
    xpRate = Nums.sqrt(xpRate);
    
    for (Test t : tests) {
      float chance = testChance(t.tested);
      float rollChance = 1 - Nums.pow(1 - chance, 1f / outcomeRange);
      
      int roll = outcomeRange, testOutcome = 0;
      while (roll-- > 0) testOutcome += (Rand.num() < rollChance) ? 1 : 0;
      outcome = Nums.min(outcome, testOutcome);
      
      for (Person p : attempting) {
        p.stats.gainXP(t.tested, (1 - chance) * 2 * xpRate);
      }
    }
    
    return outcome;
  }
}










