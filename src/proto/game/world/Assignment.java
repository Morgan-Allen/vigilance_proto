

package proto.game.world;
import proto.common.*;
import proto.game.person.*;



public interface Assignment extends Session.Saveable {
  
  String name();
  boolean allowsAssignment(Person p);
  void setAssigned(Person p, boolean is);
}
