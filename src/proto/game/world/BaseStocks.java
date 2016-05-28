

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public class BaseStocks {
  
  
  final Base base;
  Tally <Equipped> stocks = new Tally();
  
  
  BaseStocks(Base base) {
    this.base = base;
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveTally(stocks);
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadTally(stocks);
  }
  
  
  
  public Series <Equipped> availableItems(Person person, int slotID) {
    Batch <Equipped> available = new Batch();
    
    for (Equipped item : stocks.keys()) {
      if (item.slotID != slotID || ! item.availableFor(person, base)) continue;
      available.add(item);
    }
    return available;
  }
  
  
  public boolean incStock(Equipped item, int amount) {
    if (item == null) return false;
    stocks.add(amount, item);
    return true;
  }
  
  
  public void removeFromStore(Equipped item) {
    incStock(item, -1);
  }
  
  
  public int numStored(Equipped item) {
    return (int) stocks.valueFor(item);
  }
  
}




