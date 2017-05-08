


package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.util.*;



public class Events {
  
  
  /**  Data fields, constructors, setup and save/load methods-
    */
  final public static int
    EVENT_MINOR  = 0,
    EVENT_NORMAL = 1,
    EVENT_MAJOR  = 2
  ;
  
  final World world;
  List <Event> coming = new List();
  List <Event> active = new List();
  List <Event> past   = new List();
  
  
  Events(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(coming);
    s.loadObjects(active);
    s.loadObjects(past  );
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(coming);
    s.saveObjects(active);
    s.saveObjects(past  );
  }
  
  
  
  /**  General public access/query methods-
    */
  public Series <Event> active() { return active; }
  public Series <Event> coming() { return coming; }
  public Series <Event> past  () { return past  ; }
  
  
  
  /**  Regular updates and event-generation-
    */
  void updateEvents() {
    final int time = world.timing.totalHours();
    
    for (Event event : coming) {
      if (event.timeBegins() <= time) {
        coming.remove(event);
        active.add(event);
        event.beginEvent();
      }
    }
    
    for (Event event : active) {
      if (event.complete()) {
        closeEvent(event);
      }
      else {
        event.updateEvent();
      }
    }
  }
  
  
  public void scheduleEvent(Event event) {
    scheduleEvent(event, 0);
  }
  
  
  public void scheduleEvent(Event event, int delayHours) {
    
    int time = world.timing.totalHours();
    int ID   = coming.size() + 1;
    
    if (event.timeBegins() == -1 || event.timeBegins() < time) {
      event.scheduleStart(time + delayHours, ID);
    }
    else {
      event.scheduleStart(event.timeBegins(), ID);
    }
    
    coming.include(event);
  }
  
  
  public void closeEvent(Event event) {
    coming.remove(event);
    active.remove(event);
    if (! past.includes(event)) past.addFirst(event);
  }
  
  
  
  /**  Query methods for specific event-types:
    */
  final static int MODE_ANY = 0, MODE_PLOTS = 1, MODE_TRIALS = 2;
  
  private Event nextEvent(
    Pick pick, boolean checkComing, boolean checkActive, final int mode
  ) {
    if (pick == null) pick = new Pick <Event> () {
      public void compare(Event next, float rating) {
        if (mode == MODE_PLOTS  && ! next.isPlot ()) return;
        if (mode == MODE_TRIALS && ! next.isTrial()) return;
        super.compare(next, rating);
      }
    };
    if (checkComing) {
      for (Event e : coming) pick.compare(e, 0 - e.timeBegins());
    }
    if (checkActive) {
      for (Event e : active) pick.compare(e, 0 - e.timeBegins());
    }
    return (Event) pick.result();
  }
  
  
  public Event nextComing() {
    return nextEvent(null, true, false, MODE_ANY);
  }
  
  
  public Event nextActive() {
    return nextEvent(null, false, true, MODE_ANY);
  }
  
  
  public Plot nextPlot() {
    return (Plot) nextEvent(null, true, true, MODE_PLOTS);
  }
  
  
  public Plot nextActivePlot() {
    return (Plot) nextEvent(null, false, true, MODE_PLOTS);
  }
  
  
  public Trial nextTrial() {
    return (Trial) nextEvent(null, true, true, MODE_TRIALS);
  }
  
  
  public Trial nextActiveTrial() {
    return (Trial) nextEvent(null, false, true, MODE_TRIALS);
  }
}


