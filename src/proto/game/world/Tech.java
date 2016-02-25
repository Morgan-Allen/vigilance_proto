

package proto.game.world;
import proto.common.*;
import proto.util.*;


public class Tech extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Tech> INDEX = new Index <Tech> ();
  
  
  
  final String name;
  
  
  public Tech(String name, String ID) {
    super(INDEX, ID);
    this.name = name;
  }
  
  
  public static Tech loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
}