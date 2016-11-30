

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneType extends Index.Entry implements
  TileConstants, Session.Saveable
{
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <SceneType> INDEX = new Index <SceneType> ();
  
  final String name;
  
  Kind borders;
  Kind floors;
  Kind props[];
  float propWeights[];
  
  SceneType childPatterns[];
  Box2D childLayouts[];
  
  
  public SceneType(String name, String ID) {
    super(INDEX, ID);
    this.name = name;
  }
  
  
  public static SceneType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(Place place, World world, int size) {
    final Scene scene = new Scene(world, size);
    final Box2D area  = new Box2D().set(0, 0, size, size);
    populateScene(scene, area);
    return scene;
  }
  
  
  void populateScene(Scene s, Box2D area) {
    final int
      minX = (int) area.xpos(),
      minY = (int) area.ypos(),
      dimX = (int) area.xdim(),
      dimY = (int) area.ydim()
    ;
    
    for (Coord c : Visit.grid(minX, minY, dimX, dimY, 1)) {
      s.addProp(floors, c.x, c.y);
    }
    for (Coord c : Visit.perimeter(minX, minY, dimX, dimY)) {
      s.addProp(borders, c.x, c.y);
    }
    
    int totalPropArea = 0, totalArea = (int) area.area();
    for (Kind propType : props) {
      totalPropArea += propType.wide() * propType.high();
    }
  }
  
  
  
}





