

package proto.game.scene;
import proto.common.*;
import proto.util.*;



public class ScenePattern implements TileConstants {
  
  
  Kind borders;
  Kind floors;
  Kind props[];
  float propWeights[];
  
  ScenePattern childPatterns[];
  Box2D childLayouts[];
  
  
  
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










