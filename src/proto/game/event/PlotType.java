

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
  
  
  public abstract Plot initPlot(Base base);
}