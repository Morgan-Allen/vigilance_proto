

package proto.game.person;
import proto.game.world.*;
import proto.game.person.PersonStats.Condition;
import proto.game.scene.*;
import proto.util.*;
import proto.view.scene.AbilityFX;

import java.awt.Graphics2D;
import java.awt.Image;



public abstract class Ability extends Trait {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    NONE              = 0      ,
    IS_MELEE          = 1 << 0 ,
    IS_RANGED         = 1 << 1 ,
    IS_ACTIVE         = 1 << 2 ,
    IS_PASSIVE        = 1 << 3 ,
    IS_DELAYED        = 1 << 4 ,
    NO_NEED_FOG       = 1 << 5 ,
    NO_NEED_LOS       = 1 << 6 ,
    IS_CONDITION      = 1 << 7 ,
    IS_AREA_EFFECT    = 1 << 8 ,
    IS_STUNNING       = 1 << 8 ,
    IS_BASIC          = 1 << 10,
    IS_NATURAL        = 1 << 11,
    IS_EQUIPPED       = 1 << 12,
    TRIGGER_ON_ATTACK = 1 << 13,
    TRIGGER_ON_DEFEND = 1 << 14,
    TRIGGER_ON_ACTION = 1 << 15;
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
  
  
  public boolean requiresFog() {
    return ! hasProperty(NO_NEED_FOG);
  }
  
  
  public boolean requiresSight() {
    return ! hasProperty(NO_NEED_LOS);
  }
  
  
  public boolean ranged() {
    return hasProperty(IS_RANGED);
  }
  
  
  public boolean melee() {
    return hasProperty(IS_MELEE);
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
  
  
  public boolean canLearn(Person learns) {
    for (Trait t : roots()) {
      if (learns.stats.levelFor(t) < 1) return false;
    }
    return true;
  }
  
  
  
  /**  Passive effects on stats and allowing actions, etc.-
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
  
  
  protected void applyConditionStatsBonus(Person person) {
    for (Trait trait : PersonStats.ALL_STATS) {
      final float mod = conditionModifierFor(person, trait);
      if (mod != 0) person.stats.incBonus(trait, mod);
    }
  }
  
  
  public float conditionModifierFor(Person person, Trait trait) {
    return 0;
  }
  
  
  public void applyConditionOnTurn(Person person, Person source) {
    return;
  }
  
  
  public boolean allowsAssignment(Person p, Assignment a) {
    return true;
  }
  
  
  public boolean conditionAllowsAbility(Ability a) {
    return true;
  }
  
  
  public boolean conditionAllowsAction(Action a) {
    return true;
  }
  
  
  
  /**  Methods specific to scenes and actions-
    */
  public int minCostAP() {
    return costAP;
  }
  
  
  public int maxRange() {
    return -1;
  }
  
  
  public int motionCostAP(int pathLength, Person person) {
    if (pathLength <= 1) return 0;
    float tilesPerAP = person.stats.tilesMotionPerAP();
    return (int) ((pathLength - 1) / tilesPerAP);
  }
  
  
  public int costAP(Action use) {
    return minCostAP() + motionCostAP(use.path().length, use.acting);
  }
  
  
  public boolean allowsTarget(Object target, Scene scene, Person acting) {
    return false;
  }
  
  
  public Action configAction(
    Person acting, Tile dest, Object target,
    Scene scene, Tile pathToTake[], StringBuffer failLog
  ) {
    if (acting == null || ! allowsTarget(target, scene, acting)) {
      return failResult("Invalid Target", failLog);
    }
    if (requiresFog() && ! acting.actions.hasSight(dest)) {
      return failResult("Hidden In Fog", failLog);
    }
    
    final float range = scene.distance(acting.location, dest);
    final int maxRange = maxRange();
    
    if (maxRange > 0 && range > maxRange) {
      return failResult("Outside Max. Range", failLog);
    }
    if (maxRange > 0 && requiresSight()) {
      if (range > (acting.stats.sightRange() + 1)) {
        return failResult("Outside Sight Range", failLog);
      }
      if (scene.vision.degreeOfSight(acting, dest, false) <= 0) {
        return failResult("No Sight Line", failLog);
      }
    }
    
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
      if (! search.success()) {
        return failResult("No Path", failLog);
      }
      else {
        path = search.fullPath(Tile.class);
      }
    }
    
    Action newAction = new Action(this, acting, target);
    newAction.attachPath(path, scene.time());
    newAction.attachVolley(createVolley(newAction, target, scene));
    final int costAP = costAP(newAction);
    
    if (costAP > acting.actions.currentAP()) {
      return failResult("Not enough AP", failLog);
    }
    for (Condition c : acting.stats.conditions) {
      if (! c.basis.conditionAllowsAction(newAction)) {
        return failResult(c.basis+" prevents use", failLog);
      }
    }
    
    return newAction;
  }
  
  
  private Action failResult(String s, StringBuffer log) {
    if (log != null) log.append(s);
    return null;
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
      
      if (self != null) for (Item item : self.gear.equipped()) {
        if (! item.kind().triggerOnAttack(volley)) continue;
        if (start) item.kind().applyOnAttackStart(volley);
        if (end  ) item.kind().applyOnAttackEnd  (volley);
      }
      
      if (self != null && self.actions.nextAction() != null) {
        Ability a = self.actions.nextAction().used;
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
      
      if (hits != null) for (Item item : hits.gear.equipped()) {
        if (! item.kind().triggerOnDefend(volley)) continue;
        if (start) item.kind().applyOnDefendStart(volley);
        if (end  ) item.kind().applyOnDefendEnd  (volley);
      }
      
      if (hits != null && hits.actions.nextAction() != null) {
        Ability a = hits.actions.nextAction().used;
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
      
      if (end && hits != null) hits.health.receiveAttack(volley);
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
  
  
  
  /**  Dealing with AoE effects-
    */
  public int maxEffectRange() {
    return -1;
  }
  
  
  protected boolean affectsTargetInRange(
    Element affects, Scene scene, Person acting
  ) {
    return true;
  }
  
  
  protected Series <Element> elementsInRange(Action use, Tile centre) {
    final Batch <Element> all = new Batch();
    int range = maxEffectRange();
    if (range <= 0) return all;
    
    Scene scene = use.scene();
    int minX = centre.x - range, minY = centre.y - range;
    
    for (Coord c : Visit.grid(minX, minY, range * 2, range * 2, 1)) {
      Tile t = scene.tileAt(c.x, c.y);
      
      if (t != null) for (Element e : t.inside()) {
        float dist = scene.distance(t, scene.tileUnder(e));
        if (dist > range) continue;
        
        if (affectsTargetInRange(e, scene, use.acting)) {
          all.include(e);
        }
      }
    }
    return all;
  }
  
  
  
  
  /**  Supplementary methods for AI decision-making.
    */
  //  TODO:  Move these to an AI class.
  
  public Action bestMotionToward(Object point, Person acting, Scene scene) {
    Tile at = scene.tileUnder(point);
    if (at == null) return null;
    
    if (point instanceof Person && ! acting.actions.canNotice(point)) {
      return null;
    }
    MoveSearch search = new MoveSearch(acting, acting.location, at);
    search.doSearch();
    if (! search.success()) return null;
    
    Tile path[] = search.fullPath(Tile.class);
    for (int n = path.length; n-- > 0;) {
      Tile shortPath[] = new Tile[n + 1], t = path[n];
      System.arraycopy(path, 0, shortPath, 0, n + 1);
      Action use = configAction(acting, t, t, scene, shortPath, null);
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
      if (relation < 0 && ! other.health.conscious()) relation = 0;
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
  
  
  public void renderUsageFX(Action action, Scene scene, Graphics2D g) {
    return;
  }
}













