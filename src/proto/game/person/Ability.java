

package proto.game.person;
import proto.game.world.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.BasicStroke;



public abstract class Ability extends Trait {
  
  
  /**  Data fields, construction and save/load methods-
    */
  
  final public static int
    NONE              = 0     ,
    IS_MELEE          = 1 << 0,
    IS_RANGED         = 1 << 1,
    IS_ACTIVE         = 1 << 2,
    IS_PASSIVE        = 1 << 3,
    IS_DELAYED        = 1 << 4,
    NO_NEED_SIGHT     = 1 << 5,
    IS_BASIC          = 1 << 6,
    IS_EQUIPPED       = 1 << 7,
    TRIGGER_ON_ATTACK = 1 << 8,
    TRIGGER_ON_DEFEND = 1 << 9;
  final public static float
    MAJOR_HELP = -2.0f,
    REAL_HELP  = -1.0f,
    MINOR_HELP = -0.5f,
    NO_HARM    =  0.0f,
    MINOR_HARM =  0.5f,
    REAL_HARM  =  1.0f,
    MAJOR_HARM =  2.0f;
  final public static int
    NO_POWER     = 0,
    MINOR_POWER  = 2,
    MEDIUM_POWER = 5,
    MAJOR_POWER  = 8;
  final public static Trait
    NO_TRAITS[] = new Trait[0],
    ALL_STATS[] = PersonStats.ALL_STATS;
  
  final int properties, costAP;
  final float harmLevel, powerLevel;
  
  
  public Ability(
    String name, String ID, String imgPath, String description,
    int properties, int costAP,
    float harmLevel, float powerLevel
  ) {
    super(name, ID, imgPath, description);
    this.properties  = properties;
    this.costAP      = costAP;
    this.harmLevel   = harmLevel;
    this.powerLevel  = powerLevel;
  }
  
  
  
  /**  Public query methods/properties-
    */
  public boolean hasProperty(int p) {
    return (properties & p) == p;
  }
  
  
  public boolean requiresSight() {
    return ! hasProperty(NO_NEED_SIGHT);
  }
  
  
  public boolean ranged() {
    return hasProperty(IS_RANGED);
  }
  
  
  public boolean delayed() {
    return hasProperty(IS_DELAYED);
  }
  
  
  public boolean passive() {
    return hasProperty(IS_PASSIVE);
  }
  
  
  public boolean active() {
    return hasProperty(IS_ACTIVE) || ! passive();
  }
  
  
  public boolean basic() {
    return hasProperty(IS_BASIC);
  }
  
  
  public boolean equipped() {
    return hasProperty(IS_EQUIPPED);
  }
  
  
  public boolean triggerOnAttack() {
    return hasProperty(TRIGGER_ON_ATTACK);
  }
  
  
  public boolean triggerOnDefend() {
    return hasProperty(TRIGGER_ON_DEFEND);
  }
  
  
  
  
  /**  Configuring actions-
    */
  protected void applyPassiveStatsBonus(Person person) {
    for (Trait trait : passiveTraitsModified()) {
      final float mod = passiveModifierFor(person, trait);
      if (mod != 0) person.stats.incBonus(trait, mod);
    }
    return;
  }
  
  
  public Trait[] passiveTraitsModified() {
    return NO_TRAITS;
  }
  
  
  public float passiveModifierFor(Person person, Trait trait) {
    return 0;
  }
  
  
  public boolean allowsAssignment(Person p, Assignment a) {
    return true;
  }
  
  
  
  /**  Rendering, interface and debug methods-
    */
  public String name() {
    return name;
  }
  
  
  public String toString() {
    return name;
  }
  
  
  public float animDuration() {
    return 0.25f;
  }
  
  
  public Image missileSprite() {
    return null;
  }
}













