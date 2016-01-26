

package proto;
import util.*;
import java.awt.Graphics2D;
import java.awt.Image;



public abstract class Ability extends Index.Entry implements Session.Saveable {

  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Ability> INDEX = new Index <Ability> ();
  
  final static int
    NONE          = 0     ,
    IS_RANGED     = 1 << 0,
    IS_PASSIVE    = 1 << 1,
    NO_NEED_SIGHT = 1 << 2,
    IS_EQUIPPED   = 1 << 3;
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
  
  final public String name;
  final public String description;
  
  final int properties, costAP;
  final float harmLevel, powerLevel;
  
  
  Ability(
    String name, String uniqueID, String description,
    int properties, int costAP,
    float harmLevel, float powerLevel
  ) {
    super(INDEX, uniqueID);
    this.name        = name;
    this.description = description;
    this.properties  = properties;
    this.costAP      = costAP;
    this.harmLevel   = harmLevel;
    this.powerLevel  = powerLevel;
  }
  
  
  public static Ability loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
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
  
  
  public boolean passive() {
    return hasProperty(IS_PASSIVE);
  }
  
  
  public int minCostAP() {
    return costAP;
  }
  
  
  public int motionCostAP(int pathLength) {
    return (pathLength - 1) / 2;
  }
  
  
  public int costAP(Action use) {
    return minCostAP() + motionCostAP(use.path.length);
  }
  
  
  public boolean allowsTarget(Object target, Scene scene, Person acting) {
    return false;
  }
  
  
  public int maxRange() {
    return -1;
  }
  
  
  
  /**  Configuring actions-
    */
  public Action configAction(
    Person acting, Tile dest, Object target,
    Scene scene, Tile pathToTake[]
  ) {
    if (acting == null || ! allowsTarget(target, scene, acting)) return null;
    if (requiresSight() && ! acting.canSee(dest)) return null;
    
    final float range = scene.distance(acting.location, dest);
    final int maxRange = maxRange();
    if (maxRange > 0 && range > maxRange) return null;
    
    Tile path[] = null;
    if (pathToTake != null) {
      path = pathToTake;
    }
    else if (ranged()) {
      path = new Tile[] { acting.location };
    }
    else {
      MoveSearch search = new MoveSearch(acting, acting.location, dest);
      search.doSearch();
      if (! search.success()) return null;
      else path = search.fullPath(Tile.class);
    }
    
    Action newAction = new Action(this);
    newAction.acting    = acting;
    newAction.path      = path;
    newAction.target    = target;
    newAction.timeStart = scene.time;
    newAction.progress  = -1;
    
    if (costAP(newAction) > acting.currentAP()) return null;
    return newAction;
  }
  
  
  public Action bestMotionToward(Object point, Person acting, Scene scene) {
    Tile at = scene.tileUnder(point);
    if (at == null || ! acting.canSee(at)) return null;
    MoveSearch search = new MoveSearch(acting, acting.location, at);
    search.doSearch();
    if (! search.success()) return null;
    
    Tile path[] = search.fullPath(Tile.class);
    for (int n = path.length; n-- > 0;) {
      Tile shortPath[] = new Tile[n + 1], t = path[n];
      System.arraycopy(path, 0, shortPath, 0, n + 1);
      Action use = configAction(acting, t, t, scene, shortPath);
      if (use != null) return use;
    }
    return null;
  }
  
  
  public float rateUsage(Action use) {
    Person acts = use.acting;
    float rating = 1, relation = 0;
    
    if (use.target instanceof Person) {
      Person other = (Person) use.target;
      if (other.isAlly (acts)) relation =  1;
      if (other.isEnemy(acts)) relation = -1;
    }
    rating = harmLevel * relation * -1 * powerLevel;
    
    Tile at = acts.currentScene().tileUnder(use.target);
    rating *= 10f / (10 + at.scene.distance(acts.location, at));
    
    return rating;
  }
  
  
  public void applyEffect(Action use) {
    return;
  }
  
  
  
  /**  Rendering, interface and debug methods-
    */
  public String name() {
    return name;
  }
  
  
  public String toString() {
    return name;
  }
  
  
  float animDuration() {
    return 0.25f;
  }
  
  
  Image missileSprite() {
    return null;
  }
  
  
  public void renderMissile(Action action, Scene s, Graphics2D g) {
    Image sprite = missileSprite();
    if (sprite == null) return;
    
    Person using = action.acting;
    float progress = action.progress;
    Tile target = s.tileUnder(action.target);
    
    float pX = using.posX * (1 - progress);
    float pY = using.posY * (1 - progress);
    pX += target.x * progress;
    pY += target.y * progress;
    
    s.view.renderAt(pX, pY, 0.5f, 0.5f, sprite, null, g);
  }
}







