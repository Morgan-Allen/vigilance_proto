

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;



public class BaseStocks {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final Base base;
  List <Item> stocks = new List();
  List <TaskCraft> tasks = new List();
  
  
  BaseStocks(Base base) {
    this.base = base;
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(stocks);
    s.saveObjects(tasks);
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(stocks);
    s.loadObjects(tasks);
  }
  
  
  
  /**  Adding, removing and listing items:
    */
  public Series <ItemType> availableItemTypes(Person person, int slotType) {
    Batch <ItemType> available = new Batch();
    
    for (Item item : stocks) {
      final ItemType type = item.kind();
      if (  type.slotType != slotType      ) continue;
      if (! type.availableFor(person, base)) continue;
      available.include(type);
    }
    if (slotType == PersonGear.SLOT_TYPE_ARMOUR) {
      available.add(Common.UNARMOURED);
    }
    if (slotType == PersonGear.SLOT_TYPE_WEAPON) {
      available.add(Common.UNARMED);
    }
    return available;
  }
  
  
  public boolean incStock(ItemType type, int amount) {
    while (amount-- > 0) {
      stocks.add(new Item(type, base.world));
    }
    return true;
  }
  
  
  public void addItem(Item item) {
    if (item.kind() == Common.UNARMED   ) return;
    if (item.kind() == Common.UNARMOURED) return;
    stocks.add(item);
  }
  
  
  public void removeItem(Item item) {
    stocks.remove(item);
  }
  
  
  public int numStored(ItemType type) {
    int total = 0;
    for (Item i : stocks) if (i.kind() == type) total++;
    return total;
  }
  
  
  public Item nextOfType(ItemType type) {
    if (type == Common.UNARMED   ) return null;
    if (type == Common.UNARMOURED) return null;
    for (Item i : stocks) if (i.kind() == type) return i;
    return null;
  }
  
  
  
  /**  Generating and updating manufacturing tasks-
    */
  public Series <TaskCraft> craftingTasks() {
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
      if (task.complete()) task.resetTask();
      task.updateAssignment();
    }
  }
}







