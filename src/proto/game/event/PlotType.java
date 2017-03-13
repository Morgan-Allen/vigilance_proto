

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;



public abstract class PlotType extends EventType {
  
  
  /**  Construction-
    */
  protected PlotType(
    String name, String ID, String iconPath
  ) {
    super(name, ID, iconPath);
  }
  
  
  protected abstract Plot initPlot(Base base);
  

  
  /**  Rendering and interface:
    */
  protected String nameFor(Event event) {
    Plot c = (Plot) event;
    return name+": "+c.elementWithRole(Plot.ROLE_TARGET);
  }
  
  
  protected String infoFor(Event event) {
    //  TODO:  Fill this in.  (A lot of these probably aren't needed anyway...)
    return null;
  }
}









