

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public interface Assignment extends Session.Saveable {
  
  //  NOTE:  Multiple assignments of the same priority cannot be assigned to a
  //  single agent!
  final public static int
    PRIORITY_ON_SCENE  = 4,
    PRIORITY_PLAN_STEP = 3,
    PRIORITY_LEAD      = 3,
    PRIORITY_TRAINING  = 2,
    PRIORITY_CRAFTING  = 2,
    PRIORITY_INVESTING = 2,
    PRIORITY_CASUAL    = 1,
    PRIORITY_NONE      = 0;
  
  String activeInfo();
  String helpInfo();
  Image icon();
  
  boolean allowsAssignment(Person p);
  int assignmentPriority();
  
  void setAssigned(Person p, boolean is);
  Series <Person> assigned();
  boolean complete();
  
  Place targetLocation();
}
