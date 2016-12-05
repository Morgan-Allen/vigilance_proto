

package proto.content.events;
import proto.game.event.StepType;
import proto.util.*;



public class StepTypes {
  
  
  private static Batch <StepType> types = new Batch();
  private static StepType register(StepType type) {
    types.add(type);
    return type;
  }
  
  final public static StepType
    HEIST    = register(new TypeHeist()),
    MURDER   = register(new TypeAssassination()),
    KIDNAP   = register(new TypeKidnapping()),
    COERCE   = register(new TypeCoerce()),
    BRIBE    = register(new TypeBribe()),
    MAKE     = register(new TypeMake()),
    RESEARCH = register(new TypeResearch()),
    ALL_TYPES[] = types.toArray(StepType.class)
  ;
  
}