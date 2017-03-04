

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;



public abstract class CrimeType extends EventType {
  
  
  protected CrimeType(
    String name, String ID, String iconPath
  ) {
    super(name, ID, iconPath);
  }
  
  
  protected abstract Crime initCrime(Base base);
  
}
