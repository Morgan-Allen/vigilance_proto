

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class Item extends Element {
  
  
  public Item(Equipped kind, World world) {
    super(kind, Element.TYPE_ITEM, world);
  }
  
  
  public Item(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  public Equipped kind() {
    return (Equipped) kind;
  }
  
  
  
}
