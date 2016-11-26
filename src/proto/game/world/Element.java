

package proto.game.world;
import proto.common.*;
import proto.game.plans.*;
import proto.util.*;



public class Element implements Session.Saveable {
  
  
  final public static int
    TYPE_INIT   = -1,
    TYPE_WORLD  =  0,
    TYPE_REGION =  1,
    TYPE_PLACE  =  2,
    TYPE_PERSON =  3,
    TYPE_ITEM   =  5,
    TYPE_CLUE   =  5
  ;
  
  final public Kind kind;
  final public int type;
  World world;
  Place location;
  List <Clue> attached = new List();
  
  
  
  protected Element(Kind kind, int type, World world) {
    this.kind  = kind ;
    this.type  = type ;
    this.world = world;
  }
  
  
  public Element(Session s) throws Exception {
    s.cacheInstance(this);
    kind     = (Kind ) s.loadObject();
    type     = s.loadInt();
    world    = (World) s.loadObject();
    location = (Place) s.loadObject();
    s.loadObjects(attached);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind);
    s.saveInt(type);
    s.saveObject(world);
    s.saveObject(location);
    s.saveObjects(attached);
  }
  
  
  public World world() {
    return world;
  }
}




