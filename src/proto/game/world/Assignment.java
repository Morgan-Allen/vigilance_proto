

package proto.game.world;

import proto.common.Session;
import proto.common.Session.Saveable;
import proto.game.person.Person;

public interface Assignment extends Session.Saveable {
  
  String name();
  boolean allowsAssignment(Person p);
}
