

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
  
  SceneType childPatterns[] = new SceneType[0];
  Box2D childLayouts[];
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
    final Box2D area = new Box2D().set(2, 2, size - 4, size - 4);
    populateScene(scene, area);
    return scene;
  }
  
  
  void populateScene(Scene scene, Box2D area) {
    final int
      minX = (int) area.xpos(),
      minY = (int) area.ypos(),
      dimX = (int) area.xdim(),
      dimY = (int) area.ydim()
    ;
    
    for (Coord c : Visit.grid(minX, minY, dimX, dimY, 1)) {
      scene.addProp(floors, c.x, c.y);
    }
    
    int x = minX, y = minY;
    final int DIRS[] = { N, E, S, W };
    for (int n = 0; n < 4; n++) {
      final int dir = DIRS[n], side;
      side = (dir == W || dir == E) ? dimX : dimY;
      
      int doorSpace = Nums.min(8, side / 2);
      int windSpace = Nums.min(4, side / 4);
      
      for (int s = side; s-- > 0;) {
        Kind prop = borders;
        if (s % windSpace == 0) prop = window;
        if (s % doorSpace == 0) prop = door;
        scene.addProp(prop, x, y);
        x += T_X[dir];
        y += T_Y[dir];
      }
    }
    
    /*
    int totalPropArea = 0, totalArea = (int) area.area();
    for (Kind propType : props) {
      totalPropArea += propType.wide() * propType.high();
    }
    //*/
  }
  
  
  
}





