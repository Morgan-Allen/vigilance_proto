

package proto.game.world;
import proto.common.*;
import proto.util.*;



public class Faction extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Faction> INDEX = new Index <Faction> ();
  
  final public String name;
  final public boolean criminal;
  
  
  public Faction(String name, String ID, boolean criminal) {
    super(INDEX, ID);
    this.name = name;
    this.criminal = criminal;
  }
  
  
  public static Faction loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public String toString() {
    return name;
  }
}



