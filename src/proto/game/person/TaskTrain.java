

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;
import proto.view.common.*;
import java.awt.Image;



public class TaskTrain extends Task {
  
  
  
  /**  Data fields, construction and save/load methods-
    */
  Ability trained;
  Trait talking;
  Person lastLevelled;
  
  
  public TaskTrain(Ability trained, Trait talking, Base base) {
    super(base, TIME_INDEF, trained, 0);
    this.trained = trained;
    this.talking = talking;
  }
  
  
  public TaskTrain(Session s) throws Exception {
    super(s);
    trained = (Ability) s.loadObject();
    talking = (Trait  ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(trained);
    s.saveObject(talking);
  }
  
  
  
  /**  Filler methods for task completion-
    */
  public boolean allowsAssignment(Person p) {
    for (Trait root : trained.roots()) {
      if (p.stats.levelFor(root) <= 0) return false;
    }
    return true;
  }
  
  
  public Place targetLocation() {
    return base;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_TRAINING;
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    
    float timeHours = base.world().timing.hoursInTick();
    for (Person p : active()) {
      final int oldLevel = p.stats.levelFor(trained);
      advanceTraining(p, timeHours);
      final int newLevel = p.stats.levelFor(trained);
      if (newLevel > oldLevel) {
        p.stats.updateStats();
        lastLevelled = p;
        presentMessage();
      }
    }
    
    return true;
  }
  
  
  /**  Training-specific methods-
    */
  void advanceTraining(Person person, float timeHours) {
    final int level = person.stats.levelFor(trained);
    float xpGain = trained.xpRequired(level), trainTime = trainingTime(person);
    xpGain *= timeHours / trainTime;
    person.stats.gainXP(trained, xpGain);
  }
  
  
  float trainBonus(Person person) {
    float trainBonus = 0;
    //
    //  If you're training in a group, then you gain a bonus for whoever has
    //  the highest skill-rating, and for having a needed conversation skill.
    int numPeers = 0, maxLevel = 0;
    for (Person p : active()) if (p != person) {
      maxLevel = Nums.max(maxLevel, p.stats.levelFor(trained));
      numPeers++;
    }
    if (numPeers > 0) {
      int ownLevel = person.stats.levelFor(trained);
      float chatChance = person.stats.levelFor(PersonStats.PERSUADE);
      chatChance = Nums.clamp(chatChance / MEDIUM_DC, 0, 1);
      maxLevel *= chatChance;
      trainBonus += (maxLevel + MEDIUM_DC) * 0.5f / (ownLevel + MEDIUM_DC);
    }
    //
    //  If you're wounded, increase the time taken-
    float woundLevel = (1 - person.health.healthLevel()) * 2;
    return trainBonus - Nums.clamp(woundLevel, 0, 1);
  }
  
  
  public int trainingTime(Person person) {
    float trainBonus = trainBonus(person);
    if (trainBonus <= -1) return -1;
    int defaultTime = GameSettings.DEF_ABILITY_TRAIN_TIME;
    int level = person.stats.levelFor(trained);
    return (int) (defaultTime * (1 + level) / (1 + trainBonus));
  }
  
  
  public int trainingTimeLeft(Person person) {
    float xp = person.stats.xpLevelFor(trained);
    return (int) (trainingTime(person) * (1 - xp));
  }
  
  
  public Ability trained() {
    return trained;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    //  TODO:  Get a better variety of these.
    return trained.icon();
  }
  
  
  public String choiceInfo(Person p) {
    int level = p.stats.levelFor(trained) + 1;
    return "Train "+trained+" ("+trained.levelDesc(level)+")";
  }
  
  
  public String activeInfo() {
    return "Learning technique: "+trained;
  }
  
  
  public String helpInfo() {
    return trained.description;
  }
  
  
  public String testInfo() {
    return "";
  }
  
  
  public TaskView createView(MainView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    return view;
  }
  
  
  protected void presentMessage() {
    final World world = base.world();
    StringBuffer s = new StringBuffer();
    
    final Person p = lastLevelled;
    if (p != null) {
      int level = p.stats.levelFor(trained);
      s.append("  "+p+" "+trained+": "+trained.levelDesc(level));
    }
    
    world.view().queueMessage(new MessageView(
      world.view(),
      icon(), "Task complete: "+activeInfo(),
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
}





