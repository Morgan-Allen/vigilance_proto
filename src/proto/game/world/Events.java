


package proto.game.world;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;



public class Events {
  
  
  final World world;
  List <Investigation> allEvents = new List();
  
  
  Events(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(allEvents);
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(allEvents);
  }
  
  
  
  
  
  
}
