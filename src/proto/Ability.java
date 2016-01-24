

package proto;

import java.awt.Graphics2D;
import java.awt.Image;


public abstract class Ability {

  
  /**  Data fields, construction and save/load methods-
    */
  final static int
    NONE          = 0,
    IS_RANGED     = 1,
    IS_PASSIVE    = 2,
    NO_NEED_SIGHT = 3;
  final static float
    MAJOR_HELP = -2.0f,
    REAL_HELP  = -1.0f,
    MINOR_HELP = -0.5f,
    NO_HARM    =  0.0f,
    MINOR_HARM =  0.5f,
    REAL_HARM  =  1.0f,
    MAJOR_HARM =  2.0f;
  final static int
    MINOR_POWER  = 2,
    MEDIUM_POWER = 5,
    MAJOR_POWER  = 8;
  
  final String name;
  final String description;
  
  final int properties, costAP;
  final float harmLevel, powerLevel;
  
  
  Ability(
    String name, String description,
    int properties, int costAP,
    float harmLevel, float powerLevel
  ) {
    this.name        = name;
    this.description = description;
    this.properties  = properties;
    this.costAP      = costAP;
    this.harmLevel   = harmLevel;
    this.powerLevel  = powerLevel;
  }
  
  
  boolean hasProperty(int p) {
    return (properties & p) == p;
  }
  
  
  boolean requiresSight() {
    return ! hasProperty(NO_NEED_SIGHT);
  }
  
  
  boolean ranged() {
    return hasProperty(IS_RANGED);
  }
  
  
  boolean passive() {
    return hasProperty(IS_PASSIVE);
  }
  
  
  int minCostAP() {
    return costAP;
  }
  
  
  int costAP(Action use) {
    return minCostAP() + ((use.path.length - 1) / 2);
  }
  
  
  boolean allowsTarget(Object target) {
    return false;
  }
  
  
  void applyEffect(Action use) {
    return;
  }
  
  
  float rateUsage(Action use) {
    float rating = 1, relation = 0;
    if (use.target instanceof Person) {
      Person other = (Person) use.target;
      if (other.isAlly (use.acting)) relation =  1;
      if (other.isEnemy(use.acting)) relation = -1;
    }
    rating = harmLevel * relation * -1 * powerLevel;
    return rating;
  }
  
  
  
  /**  Rendering, interface and debug methods-
    */
  public String toString() {
    return name;
  }
  
  
  float animDuration() {
    return 0.5f;
  }
  
  
  Image missileSprite() {
    return null;
  }
  
  
  void renderMissile(Action action, Scene s, Graphics2D g) {
    Image sprite = missileSprite();
    if (sprite == null) return;
    
    Person using = action.acting;
    float progress = action.progress;
    Tile target = s.tileUnder(action.target);
    
    float pX = using.posX * (1 - progress);
    float pY = using.posY * (1 - progress);
    pX += target.x * progress;
    pY += target.y * progress;
    
    s.renderAt(pX, pY, 0.5f, 0.5f, sprite, null, g);
  }
}
