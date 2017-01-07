

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;



public class BaseStocks {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final Base base;
  Tally <ItemType> stocks = new Tally();
  List <TaskCraft> tasks = new List();
  
  
  BaseStocks(Base base) {
    this.base = base;
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveTally(stocks);
    s.saveObjects(tasks);
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadTally(stocks);
    s.loadObjects(tasks);
  }
  
  
  
  
  /**  Adding, removing and listing items:
    */
  public Series <ItemType> availableItems(Person person, int slotID) {
    Batch <ItemType> available = new Batch();
    
    for (ItemType item : stocks.keys()) {
      if (item.slotID != slotID || ! item.availableFor(person, base)) continue;
      available.add(item);
    }
    
    if (slotID == PersonGear.SLOT_ARMOUR) available.include(Common.UNARMOURED);
    if (slotID == PersonGear.SLOT_WEAPON) available.include(Common.UNARMED   );
    return available;
  }
  
  
  public boolean incStock(ItemType item, int amount) {
    if (item == null) return false;
    stocks.add(amount, item);
    return true;
  }
  
  
  public void removeFromStore(ItemType item) {
    incStock(item, -1);
  }
  
  
  public int numStored(ItemType item) {
    return (int) stocks.valueFor(item);
  }
  
  
  
  /**  Generating and updating manufacturing tasks-
    */
  public Series <TaskCraft> craftingTasksFor(Person person) {
    if (! tasks.empty()) return tasks;
    
    for (Object tech : base.knownTech) {
      if (tech instanceof ItemType) {
        ItemType type = (ItemType) tech;
        tasks.add(new TaskCraft(type, base));
      }
    }
    
    return tasks;
  }
  
  
  void updateCrafting() {
    for (TaskCraft task : tasks) {
      task.updateAssignment();
    }
  }
  
}


















