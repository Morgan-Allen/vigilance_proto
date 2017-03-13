

package proto.content.events;
import proto.util.*;
import proto.game.event.*;



public class CrimeTypes {
  
  
  private static Batch <PlotType> types = new Batch();
  private static PlotType register(PlotType type) {
    types.add(type);
    return type;
  }
  
  
  final public static PlotType
    /*
    HEIST    = register(new TypeHeist()),
    MURDER   = register(new TypeMurder()),
    KIDNAP   = register(new TypeKidnap()),
    COERCE   = register(new TypeCoerce()),
    BRIBE    = register(new TypeBribe()),
    MAKE     = register(new TypeMake()),
    RESEARCH = register(new TypeResearch()),
    //*/
    ALL_TYPES[] = types.toArray(PlotType.class)
  ;
  
}