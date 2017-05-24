

package proto.game.event;
import proto.game.person.Person;
import proto.game.world.*;
import proto.common.*;
import proto.util.*;



public class BasePlots {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  PlotType plotTypes[] = new PlotType[0];
  Plot rootPlot;
  
  
  
  public BasePlots(Base base) {
    this.base = base;
  }
  
  
  public void loadState(Session s) throws Exception {
    plotTypes = (PlotType[]) s.loadObjectArray(PlotType.class);
    rootPlot  = (Plot) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(plotTypes);
    s.saveObject(rootPlot);
  }
  
  
  public void assignPlotTypes(PlotType... types) {
    this.plotTypes = types;
  }
  
  
  
  
  public Plot generateNextPlot() {
    
    Person leader = base.leader();
    if (leader == null || leader.isCaptive() || ! leader.health.conscious()) {
      return null;
    }
    
    final boolean report = GameSettings.eventsVerbose;
    if (report) I.say("\nPicking new plot for "+base);
    Pick <Plot> pick = new Pick(0);
    
    for (PlotType type : plotTypes) {
      Plot sample = type.initPlot(base);
      sample.fillAndExpand();
      float rating = sample.ratePlotFor(base.leader());
      if (report) I.say("  "+sample+": "+rating);
      pick.compare(sample, rating * Rand.avgNums(2));
    }
    if (report) I.say("  Picked: "+pick.result());
    
    return pick.result();
  }
  
  
  public void assignRootPlot(Plot newPlot, int delayHours) {
    if (rootPlot != null) {
      base.world().events.closeEvent(rootPlot);
    }
    if (newPlot != null) {
      this.rootPlot = newPlot;
      base.world().events.scheduleEvent(rootPlot, delayHours);
    }
  }
  
  
  public void updatePlanning() {
    if (Visit.empty(plotTypes)) {
      return;
    }
    if (rootPlot == null || rootPlot.complete()) {
      Plot newPlot = generateNextPlot();
      int delay = (int) Rand.range(
        GameSettings.MIN_PLOT_THINKING_TIME,
        GameSettings.MAX_PLOT_THINKING_TIME
      );
      assignRootPlot(newPlot, delay);
    }
  }
  
}






