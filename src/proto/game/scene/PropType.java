

package proto.game.scene;
import proto.common.*;




public class PropType extends Kind {
  
  
  public PropType(
    String name, String ID, String spritePath,
    int wide, int high, int blockLevel, boolean blockSight,
    Object... initStats
  ) {
    super(
      name, ID, spritePath, "",
      wide, high, blockLevel, blockSight,
      TYPE_PROP, -1, initStats
    );
  }
  
  
  public PropType(String name, String uniqueID, String info) {
    super(name, uniqueID, info, TYPE_PROP);
  }
  
}