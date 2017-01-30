


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
  
  Assignment currentAction = null;
  static class LogEntry { String info; Assignment action; int priority; }
  List <LogEntry> actionLog = new List();
  
  
  Events(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(coming);
    s.loadObjects(active);
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(coming);
    s.saveObjects(active);
  }
  
  
  
  /**  General public access/query methods-
    */
  public Series <Event> active() { return active; }
  public Series <Event> coming() { return coming; }
  
  
  
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
      if (event.timeEnds() <= time || event.complete()) {
        event.completeEvent();
        closeEvent(event);
      }
      else {
        event.updateEvent();
      }
    }
  }
  
  
  public void scheduleEvent(Event event) {
    int time = world.timing.totalHours();
    if (event.timeBegins() == -1 || event.timeBegins() < time) {
      event.setBeginTime(time);
    }
    coming.include(event);
  }
  
  
  public void scheduleEvent(Event event, int delayHours) {
    event.setBeginTime(world.timing.totalHours() + delayHours);
    scheduleEvent(event);
  }
  
  
  public void closeEvent(Event event) {
    coming.remove(event);
    active.remove(event);
  }
  
  
  
  /**  Logging background events-
    */
  public void logAssignment(Assignment action) {
    //  TODO:  It's possible the assignment associated should be passed to the
    //  log method directly...
    this.currentAction = action;
  }
  
  
  public void log(String info, int priority) {
    if (currentAction == null) return;
    final LogEntry entry = new LogEntry();
    entry.action   = currentAction;
    entry.info     = info;
    entry.priority = priority;
    actionLog.add(entry);
    
    I.say("\nRECORDING IN LOG: "+info);
    I.say("  Current action: "+currentAction);
  }
  
  
  public void log(String info) {
    log(info, EVENT_NORMAL);
  }
  
  
  public Series <String> extractLogInfo(Assignment action, int minPriority) {
    final Batch <String> extracts = new Batch();
    
    for (LogEntry entry : actionLog) {
      if (entry.action == action) {
        actionLog.remove(entry);
        if (entry.priority >= minPriority) extracts.add(entry.info);
      }
    }
    return extracts;
  }
  
  
  public Series <String> extractLogInfo(Assignment action) {
    return extractLogInfo(action, EVENT_NORMAL);
  }
}


