

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;



public class BaseTraining {
  
  
  final Base base;
  List <TaskTrain> tasks = new List();
  
  
  
  BaseTraining(Base base) {
    this.base = base;
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(tasks);
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(tasks);
  }
  
  

  public Series <TaskTrain> trainingTasksFor(Person person) {
    if (! tasks.empty()) return tasks;
    
    //  TODO:  Also include any abilities that are unlocked from whatever that
    //  person currently knows!  (This will require caching tasks per agent.)
    
    for (Object tech : base.knownTech) {
      if (tech instanceof Ability) {
        Ability type = (Ability) tech;
        tasks.add(new TaskTrain(type, PersonStats.PERSUADE, base));
      }
    }
    
    return tasks;
  }
  
  
  void updateTraining() {
    for (TaskTrain task : tasks) {
      task.updateAssignment();
    }
  }
}







