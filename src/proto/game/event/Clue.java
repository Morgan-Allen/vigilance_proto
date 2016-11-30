

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;




public class Clue extends Element {
  
  
  Session.Saveable revealed;
  Trait traitKey;
  float expireCount = Task.TIME_MEDIUM * World.MINUTES_PER_HOUR;
  
  
  public Clue(ClueType kind, World world, Session.Saveable revealed) {
    super(kind, Element.TYPE_CLUE, world);
    this.revealed = revealed;
  }
  
  
  public Clue(Session s) throws Exception {
    super(s);
    revealed    = s.loadObject();
    traitKey    = (Trait) s.loadObject();
    expireCount = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(revealed);
    s.saveObject(traitKey);
    s.saveFloat(expireCount);
  }
  
  
  public ClueType kind() {
    return (ClueType) kind;
  }
  
  
}



