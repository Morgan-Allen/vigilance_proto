

package proto.game.plans;
import proto.util.*;




abstract class Clue {
  
  final static int
    TYPE_FORENSIC = 0,
    TYPE_MESSAGE  = 1,
    TYPE_MEMORY   = 2
  ;
  
  int type;
  float chanceLeft;
  Thing attached;
  
  Thing revealed;
  String propKey;
  Object propVal;
  
  
  
  //abstract Action[] analyses();
  //abstract Fact[] revealed();
}

