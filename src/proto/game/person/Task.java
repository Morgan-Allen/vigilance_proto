

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
  
  List <Person> assigned = new List();
  int timeTaken, initTime;
  protected Attempt attempt;
  boolean complete, success;
  
  
  
  protected Task(Base base, int timeHours) {
    this.timeTaken = timeHours <= 0 ? -1 : timeHours;
    this.initTime  = -1;
    this.base      = base;
  }
  
  
  public Task(Session s) throws Exception {
    s.cacheInstance(this);
    
    s.loadObjects(assigned);
    base      = (Base) s.loadObject();
    timeTaken = s.loadInt ();
    initTime  = s.loadInt ();
    attempt   = (Attempt) s.loadObject();
    complete  = s.loadBool();
    success   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjects(assigned);
    s.saveObject(base);
    s.saveInt   (timeTaken);
    s.saveInt   (initTime );
    s.saveObject(attempt  );
    s.saveBool  (complete );
    s.saveBool  (success  );
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
    if (attempt == null || attempt.complete()) {
      attempt = configAttempt(active());
    }
    setCompleted(attempt.performAttempt(1) == 1);
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
    for (Person p : assigned) p.removeAssignment(this);
  }
  
  
  public void resetTask() {
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
  
  
  protected abstract Attempt configAttempt(Series <Person> attempting);
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String testInfo(Person p) {
    StringBuffer s = new StringBuffer("Requires: ");
    
    Batch <Person> active = new Batch();
    Visit.appendTo(active, active());
    active.include(p);
    
    Attempt sample = configAttempt(active);
    float chance = sample.testChance();
    for (Attempt.Test t : sample.tests) {
      s.append("\n  "+t.tested+" "+t.testTotal);
      s.append(" vs. Obstacle "+t.testDC+" / "+t.testRange);
    }
    s.append("\n  Overall Chance: "+((int) (chance * 100))+"%");
    
    return s.toString();
  }
  
  
  public abstract String choiceInfo(Person p);
  
  
  public String toString() {
    return activeInfo();
  }
}





