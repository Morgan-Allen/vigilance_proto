

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public interface Assignment extends Session.Saveable {
  
  String name();
  Image icon();
  
  boolean allowsAssignment(Person p);
  void setAssigned(Person p, boolean is);
  Series <Person> assigned();
}
