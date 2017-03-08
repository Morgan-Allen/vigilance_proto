

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;



public abstract class CrimeType extends EventType {
  
  
  /**  Construction-
    */
  protected CrimeType(
    String name, String ID, String iconPath
  ) {
    super(name, ID, iconPath);
  }
  
  
  protected abstract Crime initCrime(Base base);
  

  
  /**  Rendering and interface:
    */
  protected String nameFor(Event event) {
    Crime c = (Crime) event;
    return name+": "+c.elementWithRole(Crime.ROLE_TARGET);
  }
  
  
  protected String infoFor(Event event) {
    //  TODO:  Fill this in.  (A lot of these probably aren't needed anyway...)
    return null;
  }
}









