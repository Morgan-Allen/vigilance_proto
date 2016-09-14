

package proto.game.plans;
import proto.util.*;



class StepTypes {
  
  
  private static Batch <StepType> types = new Batch();
  private static StepType register(StepType type) {
    types.add(type);
    return type;
  }
  
  final static StepType
    HEIST  = register(new TypeHeist ()),
    COERCE = register(new TypeCoerce()),
    MAKE   = register(new TypeMake  ()),
    ALL_TYPES[] = types.toArray(StepType.class)
  ;
  
}