

package proto.game.scene;
import proto.game.world.*;
import proto.util.*;



//  TODO:  You'll also need to ensure contiguous pathing between all the
//  various sub-units (even if door-hacks are needed.)

public class SceneTypeGrid extends SceneType {
  
  
  /**  Data fields, construction and save/load methods-
    */
  public static class GridUnit {
    SceneTypeFixed type;
    int ID;
    int priority, percent, minCount, maxCount;
  }
  
  final int resolution, wallPad;
  final GridUnit units[];
  
  
  public SceneTypeGrid(
    String name, String ID,
    int resolution, int wallPad,
    GridUnit... units
  ) {
    super(name, ID);
    this.resolution = resolution;
    this.wallPad    = wallPad;
    this.units      = units;
    int unitID = 0;
    for (GridUnit unit : units) unit.ID = unitID++;
  }
  
  
  
  /**  Specifying sub-units for placement within the grid-
    */
  final public static int
    PRIORITY_HIGH   = 4,
    PRIORITY_MEDIUM = 2,
    PRIORITY_LOW    = 0
  ;
  
  public static GridUnit unit(
    SceneTypeFixed type, int priority,
    int percent, int minCount, int maxCount
  ) {
    GridUnit unit = new GridUnit();
    unit.type     = type;
    unit.priority = priority;
    unit.percent  = percent;
    unit.minCount = minCount;
    unit.maxCount = maxCount;
    return unit;
  }
  
  
  public static GridUnit percentUnit(SceneTypeFixed type, int percent) {
    return unit(type, PRIORITY_MEDIUM, percent, -1, -1);
  }
  
  
  public static GridUnit numberUnit(SceneTypeFixed type, int number) {
    return unit(type, PRIORITY_HIGH, -1, number, number);
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(World world, int size, boolean forTesting) {
    I.say("GENERATING GRID SCENE "+this);
    
    final int gridSize = size / resolution;
    size = (gridSize * resolution) + ((gridSize + 1) * wallPad) + 2;
    
    Scene scene = new Scene(world, size);
    scene.setupScene(forTesting);
    int counts[] = new int[units.length];
    int off = wallPad + 1;
    
    for (Coord c : Visit.grid(0, 0, gridSize, gridSize, 1)) {
      
      int atX = off + (c.x * (resolution + wallPad));
      int atY = off + (c.y * (resolution + wallPad));
      
      Pick <GridUnit> pick = new Pick();
      for (GridUnit unit : units) {
        int count = counts[unit.ID];
        int percent = (count * 100) / (gridSize * gridSize);
        
        if (unit.maxCount > 0 && count   >= unit.maxCount) continue;
        if (unit.percent  > 0 && percent >= unit.percent ) continue;
        if (! unit.type.checkBordering(scene, atX, atY, resolution)) continue;
        
        float rating = unit.priority * 1f / PRIORITY_MEDIUM;
        rating += Nums.max(0, unit.minCount - count);
        rating += Rand.num() / 2;
        pick.compare(unit, rating);
      }
      GridUnit picked = pick.result();
      
      if (picked == null) continue;
      I.say("PICKED GRID UNIT "+picked.type+" AT "+atX+" "+atY);
      
      picked.type.applyToScene(scene, atX, atY, resolution);
      counts[picked.ID]++;
    }
    return scene;
  }
}


