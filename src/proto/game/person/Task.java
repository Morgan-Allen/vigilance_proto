

package proto.game.person;
import proto.game.world.*;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.*;
import proto.view.common.*;
import static proto.game.person.PersonStats.*;



//  TODO:  Consider merging Tasks with Events, or having one extend the other?


public abstract class Task implements Assignment {
  
  
  final public static int
    RESOLVE_AVG = 0,
    RESOLVE_SEQ = 1,
    RESOLVE_ALL = 2,
    
    TIME_SHORT  = 1,
    TIME_MEDIUM = World.HOURS_PER_SHIFT,
    TIME_LONG   = World.HOURS_PER_DAY * 2,
    TIME_INDEF  = -1,
    
    TRIVIAL_DC = 1,
    LOW_DC     = 3,
    MEDIUM_DC  = 5,
    HIGH_DC    = 7,
    EPIC_DC    = 9
  ;
  
  final public Base base;
  Trait   tested [];
  int     testDCs[];
  int     testMod[];
  boolean results[];
  
  List <Person> assigned = new List();
  int timeTaken, initTime;
  boolean complete, success;
  
  
  
  protected Task(
    Base base, int timeHours, Object... args
  ) {
    this.timeTaken = timeHours <= 0 ? -1 : timeHours;
    this.initTime  = -1;
    this.base      = base;
    assignTestArgs(args);
  }
  
  
  public Task(Session s) throws Exception {
    s.cacheInstance(this);
    
    final int numT = s.loadInt();
    tested  = new Trait  [numT];
    testDCs = new int    [numT];
    testMod = new int    [numT];
    results = new boolean[numT];
    for (int n = 0; n < numT; n++) {
      tested [n] = (Trait) s.loadObject();
      testDCs[n] = s.loadInt ();
      testMod[n] = s.loadInt ();
      results[n] = s.loadBool();
    }
    
    s.loadObjects(assigned);
    base      = (Base) s.loadObject();
    timeTaken = s.loadInt ();
    initTime  = s.loadInt ();
    complete  = s.loadBool();
    success   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveInt(tested.length);
    for (int n = 0; n < tested.length; n++) {
      s.saveObject(tested [n]);
      s.saveInt   (testDCs[n]);
      s.saveInt   (testMod[n]);
      s.saveBool  (results[n]);
    }
    
    s.saveObjects(assigned);
    s.saveObject(base);
    s.saveInt (timeTaken);
    s.saveInt (initTime );
    s.saveBool(complete );
    s.saveBool(success  );
  }
  
  
  protected void assignTestArgs(Object... args) {
    final int numT = args.length / 2;
    tested  = new Trait  [numT];
    testDCs = new int    [numT];
    testMod = new int    [numT];
    results = new boolean[numT];
    
    for (int n = 0; n < numT; n++) {
      tested [n] = (Trait  ) args[ n * 2     ];
      testDCs[n] = (Integer) args[(n * 2) + 1];
    }
  }
  
  
  
  /**  Roster assignments and progress reporting-
    */
  public void setAssigned(Person p, boolean is) {
    assigned.toggleMember(p, is);
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_CASUAL;
  }
  
  
  public Series <Person> assigned() {
    return assigned;
  }
  
  
  public boolean complete() {
    return complete;
  }
  
  
  public boolean success() {
    return success;
  }
  
  
  public boolean failed() {
    return complete && ! success;
  }
  
  
  public float hoursSoFar() {
    if (initTime == -1) return 0;
    return base.world().timing.totalHours() - initTime;
  }
  
  
  public float hoursLeft() {
    return timeTaken - Nums.max(0, initTime);
  }
  
  
  public abstract Place targetLocation();
  
  
  
  /**  Skill and type requirements-
    */
  public boolean setModifier(Trait skill, int mod) {
    int index = Visit.indexOf(skill, tested);
    if (index == -1) return false;
    testMod[index] = mod;
    return true;
  }
  
  
  public boolean physical() {
    for (Trait t : tested) for (Trait s : t.roots()) {
      if (s == REFLEXES || s == MUSCLE) return true;
    }
    return false;
  }
  
  
  public boolean mental() {
    for (Trait t : tested) for (Trait s : t.roots()) {
      if (s == WILL || s == BRAINS) return true;
    }
    return false;
  }
  
  
  public Trait[] tested() {
    return tested;
  }
  
  
  public boolean needsSkill(Trait s) {
    return Visit.arrayIncludes(tested, s);
  }
  
  
  
  /**  Task performance and completion-
    */
  public boolean updateAssignment() {
    if (active().empty() || complete()) return false;
    
    if (timeTaken > TIME_INDEF) {
      final int time = base.world().timing.totalHours();
      if (initTime == -1) initTime = time;
      if ((time - initTime) > timeTaken) attemptTask();
    }
    return true;
  }
  
  
  public Series <Person> active() {
    final Batch <Person> active = new Batch();
    for (Person p : assigned) {
      if (p.topAssignment() == this) active.add(p);
    }
    return active;
  }
  
  
  public boolean attemptTask() {
    setCompleted(performTest(active()));
    return success;
  }
  
  
  public boolean setCompleted(boolean success) {
    this.complete = true;
    this.success = success;
    if (success) {
      onSuccess();
    }
    else {
      onFailure();
    }
    onCompletion();
    return true;
  }
  
  
  protected void onCompletion() {
    presentMessage();
    for (Person p : assigned) p.removeAssignment(this);
  }
  
  
  public float testChance(int testIndex) {
    Trait stat = tested [testIndex];
    int   mod  = testMod[testIndex];
    int   DC   = testDCs[testIndex] + Nums.max(0, -mod);
    
    float maxLevel = 0, sumLevels = 0;
    for (Person p : active()) {
      float level = p.stats.levelFor(stat) + Nums.max(0, mod);
      maxLevel  = Nums.max(level, maxLevel);
      sumLevels += level;
    }
    
    float checkLevel = maxLevel + ((sumLevels - maxLevel) / 2);
    float winChance = Nums.clamp((checkLevel - (DC - 5)) / 10f, 0, 1);
    return winChance;
  }
  
  
  public float testChance() {
    float chance = 1.0f;
    for (int n = tested.length; n-- > 0;) {
      chance *= testChance(n);
    }
    return chance;
  }
  
  
  protected boolean performTest(Series <Person> active) {
    boolean okay = true;
    float xpRate = timeTaken * 1f / World.MINUTES_PER_HOUR;
    xpRate = Nums.sqrt(xpRate);
    
    for (int n = tested.length; n-- > 0;) {
      Trait stat = tested[n];
      float winChance = testChance(n);
      okay &= results[n] = (Rand.num() < winChance);
      
      for (Person p : active) {
        p.stats.gainXP(stat, (1 - winChance) * 2 * xpRate);
      }
    }
    
    return okay;
  }
  
  
  protected boolean performTest(Trait stat, Person p, int DC) {
    //  TODO:  Unify with the above method/s.
    
    int checkLevel = p.stats.levelFor(stat);
    float winChance = Nums.clamp((checkLevel - (DC - 5)) / 10f, 0, 1);
    p.stats.gainXP(stat, (1 - winChance) * 2);
    
    return Rand.num() < winChance;
  }
  
  
  protected void resetTask() {
    complete = success = false;
    initTime = -1;
  }
  
  
  protected void onSuccess() {
    return;
  }
  
  
  protected void onFailure() {
    return;
  }
  
  
  public void onSceneExit(Scene scene, EventReport report) {
    return;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected abstract void presentMessage();
  public abstract String choiceInfo(Person p);
  
  
  public String testInfo() {
    StringBuffer s = new StringBuffer("Requires: ");
    for (int n = 0; n < tested.length;) {
      s.append(tested[n].name+" "+testDCs[n]);
      
      float chance = testChance(n);
      s.append(" ("+((int) (chance * 100))+"%)");
      
      if (++n < tested.length) s.append(", ");
    }
    return s.toString();
  }
  
  
  public TaskView createView(UINode parent) {
    return new TaskView(this, parent);
  }
  
  
  public String toString() {
    return activeInfo();
  }
}














