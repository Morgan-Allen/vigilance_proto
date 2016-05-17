

package proto.game.scene;
import proto.game.world.*;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public abstract class Task implements Assignment {
  
  
  final static int
    RESOLVE_AVG = 0,
    RESOLVE_SEQ = 1,
    RESOLVE_ALL = 2;
  
  
  final String name;
  final String info;
  
  Trait   tested [];
  int     testDCs[];
  boolean results[];
  List <Person> assigned = new List();
  
  
  
  public Task(String name, String info, Object... args) {
    this.name = name;
    this.info = info;
    
    final int numT = args.length / 2;
    tested  = new Trait  [numT];
    testDCs = new int    [numT];
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
    results = new boolean[numT];
    for (int n = 0; n < numT; n++) {
      tested [n] = (Trait) s.loadObject();
      testDCs[n] = s.loadInt ();
      results[n] = s.loadBool();
    }
    
    s.loadObjects(assigned);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveString(name);
    s.saveString(info);
    
    s.saveInt(tested.length);
    for (int n = 0; n < tested.length; n++) {
      s.saveObject(tested [n]);
      s.saveInt   (testDCs[n]);
      s.saveBool  (results[n]);
    }
    
    s.saveObjects(assigned);
  }
  
  
  
  /**  Roster assignments-
    */
  public void setAssigned(Person p, boolean is) {
    assigned.toggleMember(p, is);
    p.setAssignment(is ? this : null);
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
  }
  
  
  public Series <Person> assigned() {
    return assigned;
  }
  
  
  
  /**  Task performance and completion-
    */
  public boolean attemptTask() {
    final boolean success = performTest();
    if (success) onSuccess();
    else onFailure();
    return success;
  }
  
  
  protected boolean performTest() {
    boolean okay = true;
    
    for (int n = tested.length; n-- > 0;) {
      Trait stat = tested [n];
      int   DC   = testDCs[n];
      
      float maxLevel = 0, sumLevels = 0;
      for (Person p : assigned) {
        float level = p.stats.levelFor(stat);
        maxLevel  = Nums.max(level, maxLevel);
        sumLevels += level;
      }
      
      float check = maxLevel + ((sumLevels - maxLevel) / 2);
      check += Rand.index(5);
      okay &= results[n] = check >= DC;
    }
    
    return okay;
  }
  
  
  protected abstract void onSuccess();
  protected abstract void onFailure();
  
  
  
  /**  Rendering, debug and interface methods-
    */
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














