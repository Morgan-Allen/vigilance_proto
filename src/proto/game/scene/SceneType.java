

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class SceneType extends Index.Entry implements
  TileConstants, Session.Saveable
{
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <SceneType> INDEX = new Index <SceneType> ();
  
  final public static Object
    BORDERS  = "Borders"  ,
    FLOORS   = "Floors"   ,
    DOOR     = "Wall"     ,
    WINDOW   = "Window"   ,
    PROP     = "Prop"     ,
    CHILD    = "Child"    ,
    MIN_SIZE = "Min. Size",
    MAX_SIZE = "Max. Size"
  ;
  
  final String name;
  
  int minSize = -1, maxSize = -1;
  Kind borders, door, window;
  Kind floors;
  Kind props[];
  float propWeights[];
  SceneType kidTypes[] = new SceneType[0];
  
  
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    Batch <Kind     > propB  = new Batch();
    Batch <SceneType> childB = new Batch();
    
    for (int i = 0; i < args.length; i += 2) try {
      final Object label = args[i], val = args[i + 1];
      if (label == BORDERS ) borders = (Kind) val;
      if (label == FLOORS  ) floors  = (Kind) val;
      if (label == DOOR    ) door    = (Kind) val;
      if (label == WINDOW  ) window  = (Kind) val;
      if (label == PROP    ) propB .add((Kind) val);
      if (label == CHILD   ) childB.add((SceneType) val);
      if (label == MIN_SIZE) minSize = (Integer) val;
      if (label == MAX_SIZE) maxSize = (Integer) val;
    } catch (Exception e) { I.report(e); }
    
    props    = propB .toArray(Kind.class);
    kidTypes = childB.toArray(SceneType.class);
  }
  
  
  public static SceneType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  
  /**  Property queries-
    */
  //  TODO:  Check whether this is needed...
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
    for (SceneType kid : kidTypes) {
      if (kid.hasFurnitureOfType(trait)) result = true;
    }
    
    traitQueryCache.put(trait, result);
    return result;
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(World world) {
    int size = 64;
    if (minSize > 0 && maxSize > 0) {
      size = minSize + Nums.round(1 + Rand.index(maxSize - minSize), 16, false);
    }
    else if (minSize > 0 && minSize > size) size = minSize;
    else if (maxSize > 0 && maxSize < size) size = maxSize;
    return generateScene(world, size, false);
  }
  
  
  public abstract Scene generateScene(
    World world, int size, boolean forTesting
  );
  
  public abstract void applyToScene(
    Scene scene, int offX, int offY, int facing, int limit, boolean forTesting
  );
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}










