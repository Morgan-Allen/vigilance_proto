

package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.game.event.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



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
    AIstate    = s.loadInt  ();
    confidence = s.loadFloat();
    wariness   = s.loadFloat();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt   (AIstate   );
    s.saveFloat (confidence);
    s.saveFloat (wariness  );
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
    if (scene == null || ! person.actions.canTakeAction()) return null;
    boolean report = true;
    if (report) I.say("\nGetting next AI action for "+person);
    
    Pick <Action> pick = new Pick(0);
    Series <Ability> abilities = person.stats.listAbilities();
    
    if (confidence < 1) {
      AIstate = STATE_RETREAT;
    }
    else {
      AIstate = STATE_ACTIVE;
      
      for (Person p : scene.allPersons()) {
        if (! person.actions.checkToNotice(p)) {
          if (report) I.say("  Not currently aware of "+p);
          continue;
        }
        for (Ability a : abilities) {
          Action use = a.configAction(person, p.location, p, scene, null, null);
          if (use == null) continue;
          float rating = a.rateUsage(use) * Rand.avgNums(2);
          if (report) I.say("  Rating for "+a+" is "+rating);
          pick.compare(use, rating);
        }
      }
      
      PlanStep planStep = scene.triggerEventPlanStep();
      if (planStep != null) {
        Action special = planStep.type.specialAction(person, planStep, scene);
        float rating = special == null ? 0 : special.used.rateUsage(special);
        pick.compare(special, rating * 0.5f);
      }
    }
    if (pick.empty()) {
      Batch <Person> foes = new Batch();
      for (Person p : scene.allPersons()) {
        if (p.isEnemy(person) && person.actions.checkToNotice(p)) foes.add(p);
      }
      Action motion = retreating() ?
        AIUtils.pickRetreatAction(person, foes) :
        AIUtils.pickAdvanceAction(person, foes)
      ;
      pick.compare(motion, 1);
    }
    
    return pick.result();
  }
  
  
  
  /**  Confidence assessment, temper, panic etc.-
    */
  protected void assessConfidence() {
    Scene scene = person.currentScene();
    float teamHealth = 0, teamPower = 0, enemySight = 0;
    
    I.say("Assessing confidence for "+person);
    
    for (Person p : scene.allPersons()) {
      if (p.isAlly(person)) {
        teamPower  += p.stats.powerLevel();
        teamHealth += p.stats.powerLevel() * p.health.healthLevel();
      }
      else if (p.isEnemy(person) && person.actions.hasSight(p.currentTile())) {
        enemySight++;
        if (person.actions.checkToNotice(p)) enemySight++;
      }
    }
    
    //  TODO:  Refine these, and use constants to define the math.
    float courage = 0.2f, minAlert = (
      person.stats.levelFor(REFLEXES) +
      person.stats.levelFor(WILL    )
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
