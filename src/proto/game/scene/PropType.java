

package proto.game.scene;
import proto.game.person.*;
import proto.common.*;




public class PropType extends Kind {
  
  
  public PropType(
    String name, String ID, String spritePath,
    int subtype, int wide, int high, int blockLevel, boolean blockSight,
    Object... initStats
  ) {
    super(
      name, ID, spritePath, "",
      wide, high, blockLevel, blockSight,
      TYPE_PROP, subtype, initStats
    );
  }
  
  
  public PropType(String name, String uniqueID, String info) {
    super(name, uniqueID, info, TYPE_PROP);
  }
  
  
  /**  Special pathing/opacity methods-
    */
  public boolean thin() {
    return wide() == 0 || high() == 0;
  }
  
  
  public boolean effect() {
    return subtype() == Kind.SUBTYPE_EFFECT;
  }
  
  
  
  /**  Custom action support-
    */
  public Action manipulationFor(Person p, Scene s, Prop ofType) {
    return null;
  }
  
  
  public void onPersonEntry(Person p, Scene s, Prop ofType) {
    return;
  }
  
  
  public void updateFogFor(Scene s, PropEffect ofType) {
    return;
  }
  
  
  public void onTurnStart(Scene s, PropEffect ofType) {
    return;
  }
  
  
  public void onTurnEnd(Scene s, PropEffect ofType) {
    return;
  }
  
  
  
  /**  Rendering support-
    */
  protected float spriteScale() {
    return 1.0f;
  }
}


