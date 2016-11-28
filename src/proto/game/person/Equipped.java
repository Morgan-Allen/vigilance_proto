

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import java.awt.*;



public class Equipped extends Kind {
  
  
  /**  Data fields, construction and save/load methods-
    */
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
  
  final Image icon;
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
    String iconImgPath,
    int slotID, int buildCost, Object craftArgs[],
    int properties, int bonus,
    Ability... abilities
  ) {
    super(name, ID, description);
    this.icon  = Kind.loadImage(iconImgPath);
    this.media = null;
    
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
  
  
  public float craftDC(Trait skill) {
    final int index = Visit.indexOf(skill, craftArgs);
    if (index == -1 || index >= craftArgs.length -1) return 100;
    return (Integer) craftArgs[index + 1];
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
  
  
  
  /**  Stat modifications-
    */
  protected void applyPassiveStatsBonus(Person person) {
    for (Trait trait : PersonStats.ALL_STATS) {
      final float mod = passiveModifierFor(person, trait);
      if (mod != 0) person.stats.incBonus(trait, mod);
    }
    return;
  }
  
  
  public float passiveModifierFor(Person person, Trait trait) {
    return 0;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String describeStats(Person person) {
    final StringBuffer s = new StringBuffer();
    for (Trait t : PersonStats.ALL_STATS) {
      final float mod = passiveModifierFor(person, t);
      if (mod != 0) s.append(t+""+I.signNum((int) mod)+" ");
    }
    return s.toString();
  }
  
  
  public Image icon() {
    return icon;
  }
}















