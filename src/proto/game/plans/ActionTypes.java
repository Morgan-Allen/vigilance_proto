

package proto.game.plans;
import proto.util.*;



class ActionTypes {
  
  
  private static Batch <ActionType> types = new Batch();
  private static ActionType register(ActionType type) {
    types.add(type);
    return type;
  }
  
  final static ActionType
    HEIST  = register(new TypeHeist ()),
    COERCE = register(new TypeCoerce()),
    ALL_TYPES[] = types.toArray(ActionType.class)
  ;
  
}