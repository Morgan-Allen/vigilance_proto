

package proto.game.event;
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
    Pick <Plot> pick = new Pick(0);
    
    for (PlotType type : plotTypes) {
      Plot sample = type.initPlot(base);
      sample.fillAndExpand();
      float rating = sample.ratePlotFor(base.leader());
      pick.compare(sample, rating * (0.5f + Rand.num()));
    }
    
    return pick.result();
  }
  
  
  public void assignRootPlot(Plot master) {
    this.rootPlot = master;
  }
  
  
  public void updatePlanning() {
    if (rootPlot == null || rootPlot.complete()) {
      rootPlot = generateNextPlot();
      int delay = (int) Rand.range(
        GameSettings.MIN_PLOT_THINKING_TIME,
        GameSettings.MAX_PLOT_THINKING_TIME
      );
      rootPlot.setBeginTime(base.world().timing.totalHours() + delay);
    }
    if (rootPlot != null && ! rootPlot.scheduled()) {
      base.world().events.scheduleEvent(rootPlot);
    }
  }
  
}






