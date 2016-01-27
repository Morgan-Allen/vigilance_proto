

package proto.game.scene;
import proto.common.Session;
import proto.common.Session.Saveable;
import proto.util.*;



public class Equipped extends Index.Entry implements Session.Saveable {
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Equipped> INDEX = new Index <Equipped> ();
  
  final static int
    NONE        = 0     ,
    IS_CONSUMED = 1 << 0,
    IS_WEAPON   = 1 << 1,
    IS_MELEE    = 1 << 2,
    IS_ARMOUR   = 1 << 3,
    IS_RANGED   = 1 << 4;
  
  final public String name, description;
  final int slotID;
  final int buildCost;
  final int properties;
  final int bonus;
  final Ability abilities[];
  
  
  public Equipped(
    String name, String ID, String description,
    int slotID, int buildCost,
    int properties, int bonus,
    Ability... abilities
  ) {
    super(INDEX, ID);
    this.name = name;
    this.description = description;
    
    this.slotID     = slotID;
    this.buildCost  = buildCost;
    this.properties = properties;
    this.bonus      = bonus;
    this.abilities  = abilities;
  }
  
  
  public static Equipped loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  General property queries-
    */
  boolean hasProperty(int p) {
    return (properties & p) == p;
  }
  
  
  public boolean ranged() {
    return hasProperty(IS_RANGED);
  }
  
  
  public boolean melee() {
    return hasProperty(IS_MELEE);
  }
  
  
  public boolean consumed() {
    return hasProperty(IS_CONSUMED);
  }
  
  
  public boolean isWeapon() {
    return hasProperty(IS_WEAPON);
  }
  
  
  public boolean isArmour() {
    return hasProperty(IS_ARMOUR);
  }
  
  
  
  
  
}















