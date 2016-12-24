

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.util.*;



public class Element implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods.
    */
  final public Kind kind;
  final public int type;
  
  World world;
  Element attachedTo;
  List <Element> attached = new List();
  
  
  protected Element(Kind kind, World world) {
    this.kind  = kind;
    this.type  = kind.type();
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
    final Element oldP = other.attachedTo;
    attached.toggleMember(other, is);
    if (is && oldP != null && oldP != this) oldP.setAttached(other, false);
    other.attachedTo = is ? this : null;
  }
  
  
  public Series <Element> attached() {
    return attached;
  }
  
  
  public Element attachedTo() {
    return attachedTo;
  }
  
  
  public Element parentOfType(int type) {
    Element e = this;
    while (e != null) {
      if (e.type == type) return e;
      e = e.attachedTo;
    }
    return null;
  }
  
  
  public Place place() {
    return (Place) parentOfType(Kind.TYPE_PLACE);
  }
  
  
  public Region region() {
    return (Region) parentOfType(Kind.TYPE_REGION);
  }
  
  
  public boolean isPerson() { return type == Kind.TYPE_PERSON; }
  public boolean isPlace () { return type == Kind.TYPE_PLACE ; }
  public boolean isRegion() { return type == Kind.TYPE_REGION; }
  public boolean isItem  () { return type == Kind.TYPE_ITEM  ; }
  public boolean isProp  () { return type == Kind.TYPE_PROP  ; }
  public boolean isClue  () { return type == Kind.TYPE_CLUE  ; }
  
  
  
  /**  AI-support methods-
    */
  public static enum Access {
    GRANTED, POSSIBLE, SECRET
  };
  
  public Access accessLevel(Base base) {
    return Access.POSSIBLE;
  }
  
  
  
  /**  Scene support methods-
    */
  public boolean blockPath() {
    return kind.blockPath();
  }
  
  
  public boolean blockSight() {
    return kind.blockSight();
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




