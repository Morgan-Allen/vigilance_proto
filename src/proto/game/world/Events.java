


package proto.game.world;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;



public class Events {
  
  
  /**  Data fields, constructors, setup and save/load methods-
    */
  final World world;
  List <EventType> eventTypes = new List();
  List <Investigation> coming = new List();
  List <Investigation> active = new List();
  
  
  Events(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(eventTypes);
    s.loadObjects(coming);
    s.loadObjects(active);
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(eventTypes);
    s.saveObjects(coming);
    s.saveObjects(active);
  }
  
  
  void addType(EventType type) {
    eventTypes.include(type);
  }
  
  
  
  /**  General public access/query methods-
    */
  public Series <Investigation> active() { return active; }
  public Series <Investigation> coming() { return coming; }
  
  
  
  /**  Regular updates and event-generation-
    */
  void updateEvents() {
    int MAX_EVENTS = 2;
    
    if (coming.size() < MAX_EVENTS) {
      Pick <Investigation> pickEvent = new Pick();
      for (EventType type : eventTypes) {
        Investigation e = type.createRandomEvent(world);
        pickEvent.compare(e, type.eventChance(e) * Rand.num());
      }
      Investigation event = pickEvent.result();
      if (event != null) coming.add(event);
    }
    
    for (Investigation event : coming) {
      if (event.timeBegins() <= world.currentTime()) {
        coming.remove(event);
        active.add(event);
      }
    }
    for (Investigation event : active) {
      if (event.timeEnds() <= world.currentTime()) {
        active.remove(event);
      }
    }
  }
  
  
}









