

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public class SceneType extends Index.Entry implements
  TileConstants, Session.Saveable
{
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <SceneType> INDEX = new Index <SceneType> ();
  
  final public static Object
    BORDERS = "Borders",
    FLOORS  = "Floors" ,
    DOOR    = "Wall"   ,
    WINDOW  = "Window" ,
    PROP    = "Prop"   ;
  
  final String name;
  
  Kind borders, door, window;
  Kind floors;
  Kind props[];
  float propWeights[];
  
  //  NOTE:  the 'prop' for a layout may be either a child scene-type or a kind
  //  of scene object.
  static class PropLayout {
    Object prop;
    boolean dirs[] = new boolean[9];
    float frequency;
  }
  
  
  SceneType childPatterns[] = new SceneType[0];
  Table <Trait, Boolean> traitQueryCache = new Table();
  
  
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    Batch <Kind> propB = new Batch();
    
    for (int i = 0; i < args.length; i += 2) {
      final Object label = args[i], val = args[i + 1];
      if (label == BORDERS) borders = (Kind) val;
      if (label == FLOORS ) floors  = (Kind) val;
      if (label == DOOR   ) door    = (Kind) val;
      if (label == WINDOW ) window  = (Kind) val;
      if (label == PROP   ) propB.add((Kind) val);
    }
    props = propB.toArray(Kind.class);
  }
  
  
  public static SceneType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public boolean hasFurnitureOfType(Trait trait) {
    Boolean cached = traitQueryCache.get(trait);
    if (cached != null) return cached;
    
    Kind all[] = { borders, door, window, floors };
    Boolean result = false;
    
    for (Kind kind : all) {
      if (kind != null && kind.baseLevel(trait) > 0) result = true;
    }
    for (Kind kind : props) {
      if (kind != null && kind.baseLevel(trait) > 0) result = true;
    }
    for (SceneType kid : childPatterns) {
      if (kid.hasFurnitureOfType(trait)) result = true;
    }
    
    traitQueryCache.put(trait, result);
    return result;
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(Place place, int size) {
    final Scene scene = new Scene(place, size);
    scene.setupScene();
    
    final Box2D area = new Box2D(2, 2, size - 4, size - 4);
    final SceneGen gen = new SceneGen(scene);
    gen.populateAsRoot(this, area);
    gen.printMarkup();
    
    return scene;
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}





