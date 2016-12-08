

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;



public abstract class EventType extends Index.Entry implements
  Session.Saveable
{
  
  
  final static Index <EventType> INDEX = new Index();
  final String name;
  final Image icon;
  
  
  protected EventType(String name, String ID, String imgPath) {
    super(INDEX, ID);
    this.name = name;
    this.icon = Kind.loadImage(imgPath);
  }
  
  
  public static EventType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Generating clues and tipoffs-
    */
  public boolean isDangerous(Event event) {
    return false;
  }
  
  
  
  /**  Rendering, interface and debug methods-
    */
  protected abstract String nameFor(Event event);
  protected abstract String infoFor(Event event);
  
  public String toString() { return name; }
  public Image iconFor(Event event) { return icon; }
}





