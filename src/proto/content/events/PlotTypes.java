

package proto.content.events;
import proto.util.*;
import proto.game.world.*;
import proto.game.event.*;



public class PlotTypes {
  
  
  private static Batch <PlotType> types = new Batch();
  private static PlotType register(PlotType type) {
    types.add(type);
    return type;
  }
  
  /*
  MURDER   = register(new TypeMurder()),
  COERCE   = register(new TypeCoerce()),
  BRIBE    = register(new TypeBribe()),
  MAKE     = register(new TypeMake()),
  RESEARCH = register(new TypeResearch()),
  //*/
  
  final public static PlotType
    TYPE_KIDNAP = register(new PlotType(
      "Kidnapping", "crime_type_kidnap", null
    ) {
      public Plot initPlot(Base base) { return new PlotKidnap(this, base); }
    }),
    TYPE_ROBBERY = register(new PlotType(
      "Robbery", "crime_type_robbery", null
    ) {
      public Plot initPlot(Base base) { return new PlotRobbery(this, base); }
    })
  ;
  
  final public static PlotType
    ALL_TYPES[] = types.toArray(PlotType.class)
  ;
  
}


