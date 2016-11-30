

package proto.game.person;
import proto.game.scene.*;
import proto.game.event.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;

import proto.common.Session;



public class PersonMind {

  final public static int
    STATE_INIT    = -1,
    STATE_AS_PC   =  0,
    STATE_UNAWARE =  1,
    STATE_ACTIVE  =  2,
    STATE_RETREAT =  3;
  
  final Person person;
  
  int   AIstate    = STATE_INIT;
  float confidence = 1.0f;
  float wariness   = 0.0f;
  
  
  PersonMind(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    AIstate      = s.loadInt   ();
    confidence   = s.loadFloat ();
    wariness     = s.loadFloat ();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt   (AIstate     );
    s.saveFloat (confidence  );
    s.saveFloat (wariness    );
  }
  
  
  
  /**  General state queries-
    */
  public float confidence() {
    return confidence;
  }
  
  
  public float wariness() {
    return wariness;
  }
  
  
  public boolean isDoing(int AIstate) {
    return this.AIstate == AIstate;
  }
  
  
  public boolean retreating() {
    return isDoing(STATE_RETREAT);
  }
  
  
  
  
  
  public Action selectActionAsAI() {
    Scene scene = person.currentScene();
    if (scene == null || person.actions.canTakeAction()) return null;
    boolean report = I.talkAbout == this;
    if (report) I.say("\nGetting next AI action for "+this);
    
    Pick <Action> pick = new Pick(0);
    Series <Ability> abilities = person.stats.listAbilities();
    
    if (confidence < 1) {
      AIstate = STATE_RETREAT;
    }
    else {
      AIstate = STATE_ACTIVE;
      for (Person p : scene.persons()) {
        if (! person.actions.canNotice(p)) continue;
        for (Ability a : abilities) {
          Action use = a.configAction(person, p.location, p, scene, null);
          if (use == null) continue;
          float rating = a.rateUsage(use) * Rand.avgNums(2);
          if (report) I.say("  Rating for "+a+" is "+rating);
          pick.compare(use, rating);
        }
      }
    }
    if (pick.empty()) {
      Series <Person> foes = scene.playerTeam();
      Action motion = retreating() ?
        pickRetreatAction(foes) :
        pickAdvanceAction(foes)
      ;
      pick.compare(motion, 1);
    }
    
    return pick.result();
  }
  
  
  Action pickAdvanceAction(Series <Person> foes) {
    Scene scene = person.currentScene();
    for (Person p : foes) {
      Action motion = Common.MOVE.bestMotionToward(p, person, scene);
      if (motion != null) return motion;
    }
    
    int range = Nums.ceil(person.actions.sightRange() / 2);
    final Tile location = person.currentTile();
    Tile pick = scene.tileAt(
      location.x + (Rand.index(range + 1) * (Rand.yes() ? 1 : -1)),
      location.y + (Rand.index(range + 1) * (Rand.yes() ? 1 : -1))
    );
    
    I.say(this+" picked random tile to approach: "+pick+" (at "+location+")");
    
    return Common.MOVE.bestMotionToward(pick, person, scene);
  }
  
  
  Action pickRetreatAction(Series <Person> foes) {
    Scene scene = person.currentScene();
    int hS = scene.size() / 2, sD = scene.size() - 1;
    Tile exits[] = {
      scene.tileAt(0 , hS),
      scene.tileAt(hS, 0 ),
      scene.tileAt(sD, hS),
      scene.tileAt(hS, sD)
    };
    
    final Pick <Tile> pick = new Pick();
    final Tile location = person.currentTile();
    for (Tile t : exits) pick.compare(t, 0 - scene.distance(t, location));
    return Common.MOVE.bestMotionToward(pick.result(), person, scene);
  }
  
  
  
  /**  Confidence assessment, temper, panic etc.-
    */
  protected void assessConfidence() {
    Scene scene = person.currentScene();
    float teamHealth = 0, teamPower = 0, enemySight = 0;
    
    I.say("Assessing confidence for "+person);
    
    for (Person p : scene.persons()) {
      if (p.isAlly(person)) {
        teamPower  += p.stats.powerLevel();
        teamHealth += p.stats.powerLevel() * p.health.healthLevel();
      }
      else if (p.isEnemy(person) && person.actions.hasSight(p.currentTile())) {
        enemySight++;
        if (person.actions.canNotice(p)) enemySight++;
      }
    }
    
    //  TODO:  Refine these, and use constants to define the math.
    
    float courage = 0.2f, minAlert = (
      person.stats.levelFor(REFLEX  ) +
      person.stats.levelFor(STRENGTH)
    ) / 100f;
    if (enemySight > 0) {
      wariness += enemySight / 4f;
    }
    else {
      wariness -= 0.25f;
    }
    wariness = Nums.clamp(wariness, minAlert, 1);
    
    if (person.isHero    ()) courage = 1.5f;
    if (person.isCriminal()) courage = 0.5f;
    
    if (teamPower <= 0) {
      confidence = 0;
    }
    else {
      confidence = teamHealth / teamPower;
      confidence = (confidence + person.health.healthLevel()) / 2;
      if (! retreating()) confidence += courage;
      
      I.say("Confidence for "+person+": "+confidence);
    }
  }
}
