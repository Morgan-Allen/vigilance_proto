

package proto.game.event;
import proto.game.world.*;
import proto.common.*;
import proto.util.*;



public class BasePlans {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  PlotType plotTypes[] = new PlotType[0];
  Plot rootPlot;
  
  
  
  public BasePlans(Base base) {
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
    return null;
  }
  
  
  public void assignRootPlot(Plot master) {
    this.rootPlot = master;
  }
  
  
  public void updatePlanning() {
    if (rootPlot != null) {
      //  TODO:  Add a delay here!
      
      if (! rootPlot.scheduled()) {
        base.world().events.scheduleEvent(rootPlot);
      }
    }
  }
  
}



