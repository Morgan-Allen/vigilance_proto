

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
  final Person trains;
  Ability trainNow, trainGoal;
  
  
  public TaskTrain(Person trains, Base base) {
    super(base, TIME_INDEF);
    this.trains = trains;
  }
  
  
  public TaskTrain(Session s) throws Exception {
    super(s);
    trains    = (Person ) s.loadObject();
    trainNow  = (Ability) s.loadObject();
    trainGoal = (Ability) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(trains   );
    s.saveObject(trainNow );
    s.saveObject(trainGoal);
  }
  
  
  
  /**  Filler methods for task completion-
    */
  public boolean allowsAssignment(Person p) {
    for (Trait root : trainNow.roots()) {
      if (p.stats.levelFor(root) <= 0) return false;
    }
    return true;
  }
  
  
  public Place targetLocation(Person p) {
    return base;
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_TRAINING;
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    
    float timeHours = base.world().timing.hoursInTick();
    for (Person p : active()) {
      final int oldLevel = p.stats.levelFor(trainNow);
      advanceTraining(p, timeHours);
      final int newLevel = p.stats.levelFor(trainNow);
      if (newLevel > oldLevel) {
        p.stats.updateStats();
        presentTrainingMessage(p);
      }
    }
    
    return true;
  }
  
  
  /**  Training-specific methods-
    */
  public void assignTraining(Ability goal) {
    Series <Ability> path = TaskTrain.trainingPath(goal, trains);
    this.trainGoal = goal;
    this.trainNow  = path.first();
  }
  
  
  void advanceTraining(Person person, float timeHours) {
    if (trainNow == null) return;
    final int level = person.stats.levelFor(trainNow);
    float xpGain = trainNow.xpRequired(level);
    float trainTime = trainingTime(person, trainNow);
    xpGain *= timeHours / trainTime;
    person.stats.gainXP(trainNow, xpGain);
    
    final int newLevel = person.stats.levelFor(trainNow);
    if (level != newLevel) assignTraining(trainGoal);
  }
  
  
  public Ability trainingNow() {
    return trainNow;
  }
  
  
  public Ability trainingGoal() {
    return trainGoal;
  }
  
  
  public Person trains() {
    return trains;
  }
  
  
  static Series <Person> activeTraining(Ability trained, Base base) {
    Batch <Person> active = new Batch();
    for (Person p : base.roster()) if (p.topAssignment() instanceof TaskTrain) {
      TaskTrain t = (TaskTrain) p.topAssignment();
      if (t.trainingNow() != trained) continue;
      active.add(p);
    }
    return active;
  }
  
  
  static float trainBonus(Person person, Ability trained) {
    float trainBonus = 0;
    //
    //  If you're training in a group, then you gain a bonus for whoever has
    //  the highest skill-rating, and for having a needed conversation skill.
    int numPeers = 0, maxLevel = 0;
    for (Person p : activeTraining(trained, person.base())) if (p != person) {
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
  
  
  public static int trainingTime(Person person, Ability trained) {
    float trainBonus = trainBonus(person, trained);
    if (trainBonus <= -1) return -1;
    int defaultTime = GameSettings.DEF_ABILITY_TRAIN_TIME;
    int level = person.stats.levelFor(trained);
    return (int) (defaultTime * (1 + level) / (1 + trainBonus));
  }
  
  
  public static int trainingTimeLeft(Person person, Ability trained) {
    float xp = person.stats.xpLevelFor(trained);
    return (int) (trainingTime(person, trained) * (1 - xp));
  }
  
  
  public static Series <Ability> trainingPath(Ability goal, Person person) {
    final Stack <Ability> path = new Stack();
    final Stack <Ability> fore = new Stack();
    fore.add(goal);
    
    while (! fore.empty()) {
      for (Ability a : fore) {
        path.addFirst(a);
        fore.remove(a);
        if (a.canLearn(person)) continue;
        for (Trait t : a.roots()) if (t instanceof Ability) {
          fore.include((Ability) t);
        }
      }
    }
    return path;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    //  TODO:  Get a better variety of these.
    return trainNow.icon();
  }
  
  
  public String choiceInfo(Person p) {
    int level = p.stats.levelFor(trainNow) + 1;
    return "Train "+trainNow+" ("+trainNow.levelDesc(level)+")";
  }
  
  
  public String activeInfo() {
    return "Learning technique: "+trainNow;
  }
  
  
  public String helpInfo() {
    return trainNow.description;
  }
  
  
  public String testInfo() {
    return "";
  }
  
  
  public TaskView createView(MainView parent) {
    TaskView view = super.createView(parent);
    view.showIcon = false;
    return view;
  }
  
  
  protected void presentTrainingMessage(final Person p) {
    final World world = base.world();
    StringBuffer s = new StringBuffer();
    final Task trainTask = this;
    
    int level = p.stats.levelFor(trainNow);
    s.append("  "+p+" "+trainNow+": "+trainNow.levelDesc(level));
    
    final MainView view = world.view();
    view.queueMessage(new MessageView(
      view,
      icon(), "Task complete: "+activeInfo(),
      s.toString(),
      "Continue Training",
      "New Assignment"
    ) {
      protected void whenClicked(String option, int optionID) {
        if (optionID == 0) {
          view.dismissMessage(this);
        }
        if (optionID == 1) {
          p.removeAssignment(trainTask);
          view.switchToTab(view.trainView);
          view.dismissMessage(this);
        }
      }
    });
  }
}








