

package proto.game.scene;
import proto.game.world.*;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.*;



public abstract class Task implements Assignment {
  
  
  final public static int
    RESOLVE_AVG = 0,
    RESOLVE_SEQ = 1,
    RESOLVE_ALL = 2,
    
    TIME_SHORT  = 1,
    TIME_MEDIUM = World.HOURS_PER_SHIFT,
    TIME_LONG   = World.HOURS_PER_DAY * World.DAYS_PER_WEEK
  ;
  
  final String name;
  final String info;
  
  World   world;
  Trait   tested [];
  int     testDCs[];
  int     testMod[];
  boolean results[];
  
  List <Person> assigned = new List();
  int timeTaken, initTime;
  boolean complete, success;
  
  
  
  protected Task(
    String name, String info, int timeHours, World world, Object... args
  ) {
    this.name = name;
    this.info = info;
    
    this.timeTaken = timeHours * World.MINUTES_PER_HOUR;
    this.initTime  = -1;
    this.world     = world;
    
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
  
  
  public Task(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
    info = s.loadString();
    
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
    world     = (World) s.loadObject();
    timeTaken = s.loadInt ();
    initTime  = s.loadInt ();
    complete  = s.loadBool();
    success   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveString(name);
    s.saveString(info);
    
    s.saveInt(tested.length);
    for (int n = 0; n < tested.length; n++) {
      s.saveObject(tested [n]);
      s.saveInt   (testDCs[n]);
      s.saveInt   (testMod[n]);
      s.saveBool  (results[n]);
    }
    
    s.saveObjects(assigned);
    s.saveObject(world);
    s.saveInt (timeTaken);
    s.saveInt (initTime );
    s.saveBool(complete );
    s.saveBool(success  );
  }
  
  
  
  /**  Roster assignments-
    */
  public void setAssigned(Person p, boolean is) {
    assigned.toggleMember(p, is);
    p.setAssignment(is ? this : null);
  }
  
  
  public boolean setModifier(Trait skill, int mod) {
    int index = Visit.indexOf(skill, tested);
    if (index == -1) return false;
    testMod[index] = mod;
    return true;
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
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
  
  
  
  /**  Task performance and completion-
    */
  public void updateAssignment() {
    if (assigned.empty() || complete) return;
    
    final int time = world.totalMinutes();
    if (initTime == -1) initTime = time;
    if ((time - initTime) > timeTaken) attemptTask();
  }
  
  
  public boolean attemptTask() {
    success = performTest();
    if (success) {
      onSuccess();
    }
    else {
      onFailure();
    }
    complete = true;
    onCompletion();
    return success;
  }
  
  
  protected void onCompletion() {
    presentMessage(world);
    for (Person p : assigned) setAssigned(p, false);
  }
  
  
  protected boolean performTest() {
    boolean okay = true;
    
    for (int n = tested.length; n-- > 0;) {
      Trait stat = tested [n];
      int   mod  = testMod[n];
      int   DC   = testDCs[n] + Nums.max(0, -mod);
      
      float maxLevel = 0, sumLevels = 0;
      for (Person p : assigned) {
        float level = p.stats.levelFor(stat) + Nums.max(0, mod);
        maxLevel  = Nums.max(level, maxLevel);
        sumLevels += level;
      }
      
      float check = maxLevel + ((sumLevels - maxLevel) / 2);
      check += Rand.index(5);
      okay &= results[n] = check >= DC;
    }
    
    return okay;
  }
  
  
  protected void resetTask() {
    complete = success = false;
    initTime = -1;
  }
  
  
  protected abstract void onSuccess();
  protected abstract void onFailure();
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected abstract void presentMessage(World world);
  
  
  public String name() {
    return name;
  }
  
  
  public String info() {
    return info;
  }
  
  
  public String testInfo() {
    StringBuffer s = new StringBuffer("Requires: ");
    for (int n = 0; n < tested.length;) {
      s.append(tested[n].name+" "+testDCs[n]);
      if (++n < tested.length) s.append(", ");
    }
    return s.toString();
  }
  
  
  public String toString() {
    return name;
  }
}














