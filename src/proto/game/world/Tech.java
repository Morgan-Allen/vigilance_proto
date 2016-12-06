

package proto.game.world;
import proto.common.*;
import proto.util.*;



//  TODO:  Extend Kind?


public class Tech extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Tech> INDEX = new Index <Tech> ();
  
  
  
  final String name;
  final Object granted[];
  
  
  public Tech(String name, String ID, Object... granted) {
    super(INDEX, ID);
    this.name = name;
    this.granted = granted;
  }
  
  
  public static Tech loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
}