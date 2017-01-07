

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class Item extends Element {
  
  
  float charges;
  boolean damaged;
  Element carries;
  int slotID = -1;
  
  
  public Item(ItemType kind, World world) {
    super(kind, world);
  }
  
  
  public Item(Session s) throws Exception {
    super(s);
    charges = s.loadFloat();
    damaged = s.loadBool();
    carries = (Element) s.loadObject();
    slotID  = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveFloat(charges);
    s.saveBool(damaged);
    s.saveObject(carries);
    s.saveInt(slotID);
  }
  
  
  public ItemType kind() {
    return (ItemType) kind;
  }
  
  
  void setCarries(Person carries, int slotID) {
    this.carries = carries;
    this.slotID = slotID;
  }
  
}








