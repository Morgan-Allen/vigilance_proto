


package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.util.*;



public class Events {
  
  
  /**  Data fields, constructors, setup and save/load methods-
    */
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
    /*
    int MAX_EVENTS = 2;
    if (coming.size() < MAX_EVENTS) {
      Pick <Event> pickEvent = new Pick();
      for (EventType type : eventTypes) {
        Event e = type.createRandomEvent(world);
        pickEvent.compare(e, type.eventChance(e) * Rand.num());
      }
      Event event = pickEvent.result();
      if (event != null) coming.add(event);
    }
    //*/
    
    for (Event event : coming) {
      if (event.timeBegins() <= world.timeDays()) {
        coming.remove(event);
        active.add(event);
        world.pauseMonitoring();
      }
    }
    
    for (Event event : active) {
      if (event.timeEnds() <= world.timeDays() || event.complete()) {
        closeEvent(event);
      }
      else {
        event.updateEvent();
      }
    }
  }
  
  
  public void closeEvent(Event event) {
    coming.remove(event);
    active.remove(event);
  }
  
  
  
  /**  Logging background events-
    */
  public void logAssignment(Assignment action) {
    this.currentAction = action;
  }
  
  
  public void log(String info, int priority) {
    if (currentAction == null) return;
    final LogEntry entry = new LogEntry();
    entry.action   = currentAction;
    entry.info     = info;
    entry.priority = priority;
    actionLog.add(entry);
  }
  
  
  public void log(String info) {
    log(info, Event.EVENT_NORMAL);
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
    return extractLogInfo(action, Event.EVENT_NORMAL);
  }
}





