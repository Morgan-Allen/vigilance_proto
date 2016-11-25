

package proto.game.plans;
import proto.util.*;



public class MotiveTrace {
  
  
  float valueSum = 0;
  //List <Motive> motives = new List();
  List <String> report = new List();
  
  
  
  void performTrace(Plan plan, Thing evaluator) {
    //  TODO:  Fill this out.
    
    Motive motives[] = new Motive[0];// evaluator.motives();
    valueSum = 0;
    report.clear();
    
    for (PlanStep step : plan.steps) {
      for (Thing given : step.gives()) {
        for (Motive motive : motives) {
          float value = motive.valueFor(given);
          if (value == 0) continue;
          valueSum += value;
          report.add(motive+": "+value);
        }
      }
    }
  }
  
  
  
}












