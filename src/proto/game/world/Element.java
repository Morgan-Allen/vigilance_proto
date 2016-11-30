

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.util.*;



public class Element implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods.
    */
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
  Element attachedTo;
  List <Element> attached = new List();
  
  
  
  protected Element(Kind kind, int type, World world) {
    this.kind  = kind ;
    this.type  = type ;
    this.world = world;
  }
  
  
  public Element(Session s) throws Exception {
    s.cacheInstance(this);
    kind       = (Kind   ) s.loadObject();
    type       = s.loadInt();
    world      = (World  ) s.loadObject();
    attachedTo = (Element) s.loadObject();
    s.loadObjects(attached);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(kind);
    s.saveInt(type);
    s.saveObject(world);
    s.saveObject(attachedTo);
    s.saveObjects(attached);
  }
  
  
  
  /**  Generic no-brainer getters and setters.
    */
  public World world() {
    return world;
  }
  
  
  public Kind kind() {
    return kind;
  }
  
  
  public void setAttached(Element other, boolean is) {
    if (other.attachedTo != null) other.attachedTo.setAttached(other, false);
    attached.toggleMember(other, is);
    other.attachedTo = is ? this : null;
  }
  
  
  public Series <Element> attached() {
    return attached;
  }
  
  
  public Element attachedTo() {
    return attachedTo;
  }
  
  
  public Place location() {
    Element e = attachedTo;
    while (e != null) {
      if (e.type == TYPE_PLACE) return (Place) e;
      e = e.attachedTo;
    }
    return null;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String name() {
    return kind.name();
  }
  
  
  public String toString() {
    return name();
  }
}




