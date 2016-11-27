

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;


public abstract class EventType extends Index.Entry implements
  Session.Saveable
{
  
  
  final static Index <EventType> INDEX = new Index();
  final String name;
  
  
  protected EventType(String name, String ID) {
    super(INDEX, ID);
    this.name = name;
  }
  
  
  public static EventType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  //public abstract Event createRandomEvent(World world);
  //public abstract float eventChance(Event event);
  
  
  /**  Rendering, interface and debug methods-
    */
  protected abstract String nameFor(Event event);
  protected abstract String infoFor(Event event);
}





