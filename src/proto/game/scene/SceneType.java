

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
    MIN_WIDE = "Min. Wide",
    MAX_WIDE = "Max. Wide",
    MIN_HIGH = "Min. High",
    MAX_HIGH = "Max. High"
  ;
  
  final String name;
  
  int minWide = -1, maxWide = -1, minHigh = -1, maxHigh = -1;
  PropType borders, door, window, floors;
  PropType props[];
  float propWeights[];
  SceneType kidTypes[] = new SceneType[0];
  
  
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    Batch <PropType > propB  = new Batch();
    Batch <SceneType> childB = new Batch();
    
    for (int i = 0; i < args.length; i += 2) try {
      final Object label = args[i], val = args[i + 1];
      if (label == BORDERS ) borders = (PropType) val;
      if (label == FLOORS  ) floors  = (PropType) val;
      if (label == DOOR    ) door    = (PropType) val;
      if (label == WINDOW  ) window  = (PropType) val;
      if (label == PROP    ) propB .add((PropType) val);
      if (label == CHILD   ) childB.add((SceneType) val);
      if (label == MIN_WIDE) minWide = (Integer) val;
      if (label == MAX_WIDE) maxWide = (Integer) val;
      if (label == MIN_HIGH) minHigh = (Integer) val;
      if (label == MAX_HIGH) maxHigh = (Integer) val;
    } catch (Exception e) { I.report(e); }
    
    props    = propB .toArray(PropType.class);
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
    int wide = clampSize(64, minWide, maxWide);
    int high = clampSize(64, minHigh, maxHigh);
    return generateScene(world, wide, high, false);
  }
  
  
  private int clampSize(int defaultSize, int minSize, int maxSize) {
    int size = defaultSize;
    if (minSize > 0 && maxSize > 0) {
      int minMaxGap = maxSize - minSize;
      size = minSize;
      if (minMaxGap > 0) size += Nums.round(
        1 + Rand.index(minMaxGap + 1), 16, false
      );
    }
    else if (minSize > 0 && minSize > size) size = minSize;
    else if (maxSize > 0 && maxSize < size) size = maxSize;
    return size;
  }
  
  
  public abstract Scene generateScene(
    World world, int wide, int high, boolean testing
  );
  
  
  public abstract void applyToScene(
    Scene scene, int offX, int offY, int facing, int w, int h, boolean testing
  );
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}










