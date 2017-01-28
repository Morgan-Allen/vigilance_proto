

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;



public class PersonGear {
  
  
  final public static int
    SLOT_TYPE_WEAPON = 0,
    SLOT_TYPE_ARMOUR = 1,
    SLOT_TYPE_ITEM   = 2,
    
    SLOT_WEAPON = 0,
    SLOT_ARMOUR = 1,
    SLOT_ITEM_1 = 2,
    SLOT_ITEM_2 = 3,
    SLOT_ITEM_3 = 4,
    SLOT_ITEM_4 = 5,
    
    SLOT_IDS[] = {
      0, 1, 2, 3, 4, 5
    },
    SLOT_TYPES[] = {
      0, 1, 2, 2, 2, 2
    },
    DEFAULT_MAX_SLOTS = 4;
  final public static String
    SLOT_TYPE_NAMES[] = { "Weapon", "Armour", "Item" };
  
  final Person person;
  List <Item> equipped = new List();
  Element carried = null;
  
  
  
  PersonGear(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(equipped);
    carried = (Element) s.loadObject();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(equipped);
    s.saveObject(carried);
  }
  
  
  
  /**  Assigning equipment loadout-
    */
  public Item itemInSlot(int slotID) {
    for (Item i : equipped) if (i.slotID == slotID) return i;
    return null;
  }
  
  
  public int slotForItem(Item item) {
    if (! equipped.includes(item)) return -1;
    return item.slotID;
  }
  
  
  public int maxSlots() {
    //  TODO:  Allow certain abilities to modify this.
    return DEFAULT_MAX_SLOTS;
  }
  
  
  public int nextFreeSlotID(int slotType) {
    int maxSlots = maxSlots();
    for (int ID : SLOT_IDS) if (SLOT_TYPES[ID] == slotType) {
      if (ID >= maxSlots) break;
      if (itemInSlot(ID) == null) return ID;
    }
    return -1;
  }
  
  
  public boolean hasEquipped(int slotID) {
    return itemInSlot(slotID) != null;
  }
  
  
  public void equipItem(ItemType kind, int slotID) {
    equipItem(new Item(kind, person.world()), slotID);
  }
  

  public void equipItem(Item item, int slotID) {
    equipItem(item, slotID, null);
  }
  
  
  public void equipItem(Item item, int slotID, Base from) {
    int maxSlots = maxSlots();
    if (slotID >= maxSlots) {
      I.complain("Cannot add item to slot: "+slotID);
      return;
    }
    
    final Item oldItem = itemInSlot(slotID);
    if (oldItem != null) {
      equipped.remove(oldItem);
      oldItem.setCarries(null, -1);
      if (from != null) from.stocks.addItem(oldItem);
    }
    if (item != null) {
      if (from != null) from.stocks.removeItem(item);
      item.setCarries(person, slotID);
      equipped.add(item);
    }
    person.stats.updateStats();
  }
  
  
  public void dropItem(int slotID) {
    if (person.currentTile() == null) {
      I.complain("NOT IN SCENE: "+person);
      return;
    }
    
    Item item = itemInSlot(slotID);
    dequipSlot(slotID, null);
    person.currentTile().setInside(item, true);
  }
  
  
  public void dequipSlot(int slotID, Base from) {
    equipItem(null, slotID, from);
  }
  
  
  public boolean hasEquipped(ItemType item) {
    for (Item i : equipped) if (i.kind() == item) return true;
    return false;
  }
  
  
  public Series <Item> equipped() {
    return equipped;
  }
  
  
  public boolean useCharge(ItemType type, float amount) {
    for (Item i : equipped) if (i.kind() == type && i.charges > 0) {
      i.charges -= amount;
      return true;
    }
    return false;
  }
  
  
  public void refreshCharges() {
    for (Item i : equipped) {
      i.charges = Nums.max(i.charges, 1);
    }
  }
  
  
  
  /**  Specific queries related to weapons and armour-
    */
  public ItemType weaponType() {
    Item weapon = itemInSlot(SLOT_WEAPON);
    return weapon == null ? Common.UNARMED : weapon.kind();
  }
  
  
  public ItemType armourType() {
    Item armour = itemInSlot(SLOT_ARMOUR);
    return armour == null ? Common.UNARMOURED : armour.kind();
  }
}







