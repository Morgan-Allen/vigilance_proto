

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneTypeGrid extends SceneType {
  
  
  /**  Data fields and construction-
    */
  final int wide, high;
  
  static class Placing {
    PropType type;
    int x, y, facing;
  }
  List <Placing> placings = new List();
  
  
  public SceneTypeGrid(
    String name, String ID,
    int wide, int high
  ) {
    super(
      name, ID,
      MIN_WIDE, wide, MAX_WIDE, wide,
      MIN_HIGH, high, MAX_HIGH, high
    );
    this.wide = wide;
    this.high = high;
  }
  
  
  public void attachPlacing(PropType type, int x, int y, int facing) {
    if (type == null) return;
    Placing p = new Placing();
    p.type = type;
    p.x = x;
    p.y = y;
    p.facing = facing;
    placings.add(p);
  }
  
  
  public SceneTypeGrid(
    String name, String ID,
    PropType floor, PropType propTypes[],
    int wide, int high, byte typeGrid[][]
  ) {
    this(name, ID, wide, high);
    if (wide != typeGrid[0].length) I.complain("WRONG WIDTH" );
    if (high != typeGrid   .length) I.complain("WRONG HEIGHT");
    
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      int index = typeGrid[c.x][c.y];
      PropType type = index == 0 ? floor : propTypes[index - 1];
      if (type != floor && floor != null) attachPlacing(floor, c.y, c.x, N);
      if (type != null                  ) attachPlacing(type , c.y, c.x, N);
    }
  }
  
  
  public Scenery generateScenery(
    World world, int prefWide, int prefHigh, boolean testing
  ) {
    return generateScenery(world, testing);
  }
  
  
  public Scenery generateScenery(
    World world, boolean testing
  ) {
    Scenery gen = new Scenery(wide, high, testing);
    for (Placing p : placings) {
      int propDir = (p.facing + gen.facing) % 8;
      if (p.type == floors) propDir = N;
      
      if (Prop.hasSpace(gen, p.type, p.x, p.y, propDir)) {
        gen.addProp(p.type, p.x, p.y, propDir, world);
      }
    }
    return gen;
  }
}


