

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
  
  
  public TaskTrain trainingFor(Ability ability) {
    for (TaskTrain task : tasks) if (task.trained() == ability) return task;
    
    TaskTrain newTask = new TaskTrain(ability, PersonStats.PERSUADE, base);
    tasks.add(newTask);
    return newTask;
  }
  
  
  void updateTraining() {
    for (TaskTrain task : tasks) {
      task.updateAssignment();
    }
  }
}







