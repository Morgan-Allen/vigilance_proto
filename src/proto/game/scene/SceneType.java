

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
  
  boolean fixedLayout;
  Kind fixedPropTypes[];
  byte fixedLayoutGrid[][];
  
  //  NOTE:  the 'prop' for a layout may be either a child scene-type or a kind
  //  of scene object.
  static class PropLayout {
    Object prop;
    boolean dirs[] = new boolean[9];
    float frequency;
  }
  
  SceneType childPatterns[] = new SceneType[0];
  
  
  public static SceneType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Constructor definition for procedural generation...
    */
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    this.fixedLayout = false;
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
  
  
  /**  ...and constructor definition for fixed scene layouts.
    */
  public SceneType(
    String name, String ID,
    Kind propTypes[], byte typeGrid[][]
  ) {
    super(INDEX, ID);
    this.name = name;
    
    this.fixedLayout = true;
    fixedPropTypes = propTypes;
    fixedLayoutGrid = typeGrid;
  }
  
  
  
  
  /**  Property queries-
    */
  Table <Trait, Boolean> traitQueryCache = new Table();
  
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
  public Scene generateScene(World world, int size, boolean forTesting) {
    
    if (fixedLayout) {
      int wide = fixedLayoutGrid[0].length, high = fixedLayoutGrid.length;
      size = Nums.max(wide, high) + 2;
      
      final Scene scene = new Scene(world, size);
      scene.setupScene(forTesting);
      
      for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
        byte index = fixedLayoutGrid[c.y][c.x];
        Kind type = index >= 0 ? fixedPropTypes[index] : null;
        if (type == null) continue;
        if (! scene.hasSpace(type, c.x + 1, c.y + 1)) continue;
        scene.addProp(type, c.x + 1, c.y + 1);
      }
      return scene;
    }
    
    else {
      final Scene scene = new Scene(world, size);
      scene.setupScene(forTesting);
      
      final Box2D area = new Box2D(2, 2, size - 4, size - 4);
      final SceneGen gen = new SceneGen(scene);
      gen.populateAsRoot(this, area);
      gen.printMarkup();
      return scene;
    }
  }
  
  
  
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}










