

package proto.game.person;
import proto.game.world.*;
import proto.game.scene.*;
import proto.view.*;
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
  
  final public AbilityFX FX = new AbilityFX(this);
  
  
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
  
  
  
  /**  Methods specific to scenes and actions-
    */
  public int costAP(Action derived) {
    return 1;
  }
  
  
  public int maxRange() {
    return -1;
  }
  
  
  public boolean allowsTarget(Object target, Scene scene, Person acting) {
    return false;
  }
  
  
  public Action configAction(
    Person acting, Tile dest, Object target,
    Scene scene, Tile pathToTake[]
  ) {
    if (acting == null || ! allowsTarget(target, scene, acting)) return null;
    if (requiresSight() && ! acting.hasSight(dest)) return null;
    
    final float range = scene.distance(acting.location, dest);
    final int maxRange = maxRange();
    if (maxRange > 0 && range > maxRange ) return null;
    if (range > (acting.sightRange() + 2)) return null;
    
    Tile path[] = null;
    if (pathToTake != null) {
      path = pathToTake;
    }
    else if (ranged() || delayed()) {
      path = new Tile[] { acting.location };
    }
    else {
      MoveSearch search = new MoveSearch(acting, acting.location, dest);
      search.doSearch();
      if (! search.success()) return null;
      else path = search.fullPath(Tile.class);
    }
    
    Action newAction = new Action(this, acting, target);
    newAction.attachPath(path, scene.time());
    newAction.attachVolley(createVolley(newAction, target, scene));
    
    if (costAP(newAction) > acting.currentAP()) return null;
    return newAction;
  }
  
  
  public Action takeFreeAction(
    Person acting, Tile dest, Object target, Scene scene
  ) {
    Action newAction = new Action(this, acting, target);
    newAction.attachPath(new Tile[] { acting.location }, scene.time());
    newAction.attachVolley(createVolley(newAction, target, scene));
    
    applyOnActionStart(newAction);
    checkForTriggers(newAction, true, false);
    
    newAction.setProgress(1);
    
    checkForTriggers(newAction, false, true);
    applyOnActionEnd(newAction);
    
    return newAction;
  }
  
  
  protected Volley createVolley(Action use, Object target, Scene scene) {
    return null;
  }
  
  
  protected void checkForTriggers(Action use, boolean start, boolean end) {
    Scene  scene  = use.acting.currentScene();
    Volley volley = use.volley();
    
    if (volley != null) {
      Person self = volley.origAsPerson();
      Person hits = volley.targAsPerson();
      if (start) volley.beginVolley   ();
      if (end  ) volley.completeVolley();
      
      if (self != null && self.nextAction() != null) {
        Ability a = self.nextAction().used;
        if (a.triggerOnAttack() && a.allowsTarget(self, scene, self)) {
          if (start) a.applyOnAttackStart(volley);
          if (end  ) a.applyOnAttackEnd  (volley);
        }
      }
      
      if (self != null) for (Ability a : self.stats.listAbilities()) {
        if (! a.passive()) continue;
        if (a.triggerOnAttack() && a.allowsTarget(self, scene, self)) {
          if (start) a.applyOnAttackStart(volley);
          if (end  ) a.applyOnAttackEnd  (volley);
        }
      }
      
      if (hits != null && hits.nextAction() != null) {
        Ability a = hits.nextAction().used;
        if (a.triggerOnDefend() && a.allowsTarget(hits, scene, hits)) {
          if (start) a.applyOnDefendStart(volley);
          if (end  ) a.applyOnDefendEnd  (volley);
        }
      }
      
      if (hits != null) for (Ability a : hits.stats.listAbilities()) {
        if (! a.passive()) continue;
        if (a.triggerOnDefend() && a.allowsTarget(hits, scene, hits)) {
          if (start) a.applyOnDefendStart(volley);
          if (end  ) a.applyOnDefendEnd  (volley);
        }
      }
      
      if (end && hits != null) hits.receiveAttack(volley);
    }
  }
  
  
  public void applyOnActionStart(Action use) {
    return;
  }
  
  
  public void applyOnActionEnd(Action use) {
    return;
  }
  
  
  public void applyOnAttackStart(Volley volley) {
    return;
  }
  
  
  public void applyOnAttackEnd(Volley volley) {
    return;
  }
  
  
  public void applyOnDefendStart(Volley volley) {
    return;
  }
  
  
  public void applyOnDefendEnd(Volley volley) {
    return;
  }
  
  
  //  TODO:  Move these to an AI class.
  
  public Action bestMotionToward(Object point, Person acting, Scene scene) {
    Tile at = scene.tileUnder(point);
    if (at == null) return null;
    if (point instanceof Person && ! acting.canNotice(point)) return null;
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
      if (relation < 0 && ! other.conscious()) relation = 0;
    }
    
    rating = harmLevel * relation * -1 * powerLevel;
    Tile at = acts.currentScene().tileUnder(use.target);
    rating *= 10f / (10 + at.scene.distance(acts.location, at));
    
    //  TODO:  Include a rating for hit-chance, assuming that a Volley is
    //  involved?  (Maybe base on brains?)
    
    return rating;
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
  
  
  public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
    return;
  }
}













