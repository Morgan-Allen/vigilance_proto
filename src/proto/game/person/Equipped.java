

package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;
import java.awt.*;



public class Equipped extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Equipped> INDEX = new Index <Equipped> ();
  
  final public static int
    NONE        = 0     ,
    
    IS_CONSUMED = 1 << 0,
    IS_WEAPON   = 1 << 1,
    IS_MELEE    = 1 << 2,
    IS_ARMOUR   = 1 << 3,
    IS_RANGED   = 1 << 4,
    
    IS_KINETIC  = 1 << 5,
    IS_BEAM     = 1 << 6,
    IS_PLASMA   = 1 << 7,
    IS_NUCLEAR  = 1 << 8,
    IS_AREA_FX  = 1 << 9;
  
  final public String name, description;
  final Object media;
  
  final public int slotID;
  final public int buildCost;
  final public int bonus;
  
  final public int properties;
  final Ability abilities[];
  
  
  public Equipped(
    String name, String ID, String description,
    Object media,
    int slotID, int buildCost,
    int properties, int bonus,
    Ability... abilities
  ) {
    super(INDEX, ID);
    this.name        = name;
    this.description = description;
    this.media       = media;
    
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
  
  
  public boolean isBeam() {
    return hasProperty(IS_BEAM);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image missileSprite() {
    if (media instanceof Image) return (Image) media;
    return null;
  }
  
  
  public Color beamColor() {
    if (media instanceof Color) return (Color) media;
    return null;
  }
  
  
  public void renderUsage(Action a, Scene s, Graphics2D g) {
    if (isWeapon()) {
      Ability used = a.used;
      if (isBeam()) {
        used.renderBeam(a, s, beamColor(), Color.WHITE, 1, g);
      }
      else {
        used.renderMissile(a, s, missileSprite(), g);
      }
    }
  }
  
  
  
}















