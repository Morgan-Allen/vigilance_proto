


package proto.game.event;
import proto.common.*;



public abstract class ClueType extends Kind {
  
  
  final public static int
    TYPE_FORENSIC = 0,
    TYPE_MESSAGE  = 1,
    TYPE_OBSERVED = 2,
    TYPE_MEMORY   = 3
  ;
  
  final int clueType;
  
  
  public ClueType(
    String name, String uniqueID, String info, int clueType
  ) {
    super(name, uniqueID, info, Kind.TYPE_CLUE);
    this.clueType = clueType;
  }
}







