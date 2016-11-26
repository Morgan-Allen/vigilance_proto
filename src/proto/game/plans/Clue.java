

package proto.game.plans;
import proto.game.world.*;
import proto.util.*;




public abstract class Clue {
  
  final static int
    TYPE_FORENSIC = 0,
    TYPE_MESSAGE  = 1,
    TYPE_MEMORY   = 2
  ;
  
  int type;
  float chanceLeft;
  Element attached;
  
  Element revealed;
  String propKey;
  Object propVal;
  
}

