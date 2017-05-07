


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
  public Event nextComing() {
    Pick <Event> pick = new Pick();
    for (Event e : coming) pick.compare(e, 0 - e.timeBegins());
    return pick.result();
  }
  
  
  public Plot latestPlot() {
    Pick <Event> pick = new Pick();
    for (Event e : coming) if (e.isPlot()) pick.compare(e, e.timeBegins());
    for (Event e : active) if (e.isPlot()) pick.compare(e, e.timeBegins());
    return (Plot) pick.result();
  }
  
  
  public Trial latestTrial() {
    Pick <Event> pick = new Pick();
    for (Event e : coming) if (e.isTrial()) pick.compare(e, e.timeBegins());
    for (Event e : active) if (e.isTrial()) pick.compare(e, e.timeBegins());
    return (Trial) pick.result();
  }
}





