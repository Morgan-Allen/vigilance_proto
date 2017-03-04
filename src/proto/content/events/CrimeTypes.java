

package proto.content.events;
import proto.util.*;
import proto.game.event.*;



public class CrimeTypes {
  
  
  private static Batch <CrimeType> types = new Batch();
  private static CrimeType register(CrimeType type) {
    types.add(type);
    return type;
  }
  
  
  final public static CrimeType
    /*
    HEIST    = register(new TypeHeist()),
    MURDER   = register(new TypeMurder()),
    KIDNAP   = register(new TypeKidnap()),
    COERCE   = register(new TypeCoerce()),
    BRIBE    = register(new TypeBribe()),
    MAKE     = register(new TypeMake()),
    RESEARCH = register(new TypeResearch()),
    //*/
    ALL_TYPES[] = types.toArray(CrimeType.class)
  ;
  
}