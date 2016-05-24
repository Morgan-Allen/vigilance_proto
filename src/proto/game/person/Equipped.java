

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import java.awt.*;



public class Equipped extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Equipped> INDEX = new Index <Equipped> ();
  
  final public static int
    NONE        = 0      ,
    IS_COMMON   = 1 << 0 ,
    IS_CUSTOM   = 1 << 1 ,
    
    IS_CONSUMED = 1 << 2 ,
    IS_WEAPON   = 1 << 3 ,
    IS_MELEE    = 1 << 4 ,
    IS_ARMOUR   = 1 << 5 ,
    IS_RANGED   = 1 << 6 ,
    
    IS_KINETIC  = 1 << 7 ,
    IS_BEAM     = 1 << 8 ,
    IS_PLASMA   = 1 << 9 ,
    IS_NUCLEAR  = 1 << 10,
    IS_AREA_FX  = 1 << 11
  ;
  
  final public String name, description;
  final Object media;
  
  final public int slotID;
  final public int buildCost;
  final public Object craftArgs[];
  final public int bonus;
  
  final public int properties;
  final Ability abilities[];
  
  private Kind inventor;
  private Tech required[];
  
  
  public Equipped(
    String name, String ID, String description,
    Object media,
    int slotID, int buildCost, Object craftArgs[],
    int properties, int bonus,
    Ability... abilities
  ) {
    super(INDEX, ID);
    this.name        = name;
    this.description = description;
    this.media       = media;
    
    this.slotID     = slotID;
    this.buildCost  = buildCost;
    this.craftArgs  = craftArgs;
    this.properties = properties;
    this.bonus      = bonus;
    this.abilities  = abilities;
  }
  
  
  public Equipped setRequirements(Kind inventor, Tech... required) {
    this.inventor = inventor;
    this.required = required;
    return this;
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
  
  
  public boolean isCommon() {
    return hasProperty(IS_COMMON);
  }
  
  
  public boolean isCustom() {
    return hasProperty(IS_CUSTOM);
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
  
  
  public boolean isBeam() {
    return hasProperty(IS_BEAM);
  }
  
  
  public boolean availableFor(Person user, Base base) {
    if (isCommon()) return true;
    if (inventor != null && user.kind() != inventor) return false;
    if (required != null) for (Tech req : required) {
      if (! base.hasTech(req)) return false;
    }
    return true;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
  
  
  public String name() {
    return name;
  }
}















