

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class PersonGear {

  final public static int
    SLOT_WEAPON     = 0,
    SLOT_ARMOUR     = 1,
    SLOT_ITEMS      = 2,
    NUM_EQUIP_SLOTS = 3,
    ALL_SLOTS[] = { 0, 1, 2 };
  final public static String
    SLOT_NAMES[] = { "Weapon", "Armour", "Items" };
  
  final Person person;
  Equipped equipSlots[] = new Equipped[NUM_EQUIP_SLOTS];
  
  
  
  PersonGear(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      equipSlots[i] = (Equipped) s.loadObject();
    }
  }
  
  
  void saveState(Session s) throws Exception {
    for (int i = 0 ; i < NUM_EQUIP_SLOTS; i++) {
      s.saveObject(equipSlots[i]);
    }
  }
  

  /**  Assigning equipment loadout-
    */
  public void equipItem(Equipped item, Base from) {
    Equipped oldItem = equipSlots[item.slotID];
    equipSlots[item.slotID] = item;
    person.stats.toggleItemAbilities(oldItem, false);
    person.stats.toggleItemAbilities(item   , true );
    
    if (oldItem != null && from != null) from.stocks.incStock(oldItem,  1);
    if (item    != null && from != null) from.stocks.incStock(item   , -1);
  }
  
  
  public int equipBonus(int slotID, int properties) {
    Equipped item = equipSlots[slotID];
    if (item == null || ! item.hasProperty(properties)) return 0;
    return item.bonus;
  }
  
  
  public Equipped equippedInSlot(int slotID) {
    return equipSlots[slotID];
  }
  
  
  public boolean hasEquipped(int slotID) {
    return equipSlots[slotID] != null;
  }
  
  
  public boolean hasEquipped(Equipped item) {
    for (Equipped i : equipSlots) if (i == item) return true;
    return false;
  }
  
  
  public boolean canEquip(Equipped item) {
    for (int slotID : ALL_SLOTS) {
      if (item.slotID == slotID && equipSlots[slotID] == null) return true;
    }
    return false;
  }
  
  
  public Equipped currentWeapon() {
    Equipped weapon = equippedInSlot(SLOT_WEAPON);
    return weapon == null ? Common.UNARMED : weapon;
  }
  
  
  public Equipped currentArmour() {
    Equipped armour = equippedInSlot(SLOT_ARMOUR);
    return armour == null ? Common.UNARMOURED : armour;
  }
  
  
  public Series <Equipped> equipment() {
    Batch <Equipped> all = new Batch();
    for (Equipped e : equipSlots) if (e != null) all.add(e);
    return all;
  }
}




