


package proto.game.world;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;



public class Events {
  
  
  final World world;
  List <EventType> eventTypes = new List();
  List <Investigation> upcoming = new List();
  List <Investigation> active   = new List();
  
  
  Events(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(eventTypes);
    s.loadObjects(upcoming);
    s.loadObjects(active  );
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(eventTypes);
    s.saveObjects(upcoming);
    s.saveObjects(active  );
  }
  
  
  
  void addType(EventType type) {
    eventTypes.include(type);
  }
  
  
  void updateEvents() {
    int MAX_EVENTS = 3;
    
    if (upcoming.size() < MAX_EVENTS) {
      Pick <Investigation> pickEvent = new Pick();
      for (EventType type : eventTypes) {
        Investigation e = type.createRandomEvent(world);
        pickEvent.compare(e, type.eventChance(e) * Rand.num());
      }
      Investigation event = pickEvent.result();
      if (event != null) upcoming.add(event);
    }
    
    for (Investigation event : upcoming) {
      if (event.timeBegins() >= world.currentTime()) {
        upcoming.remove(event);
        active.add(event);
      }
    }
    for (Investigation event : active) {
      if (event.timeEnds() >= world.currentTime()) {
        active.remove(event);
      }
    }
  }
  
  
}









