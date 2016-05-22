

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import java.awt.Image;




public abstract class Blueprint extends Index.Entry implements
  Session.Saveable
{
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Blueprint> INDEX = new Index <Blueprint> ();
  
  final public String name, description;
  final public Image sprite;
  
  int buildCost   = 0;
  int buildTime   = 0;
  int maintenance = 0;
  int powerCost   = 0;
  int lifeSupport = 0;
  int visitLimit  = 2;
  
  int studyBonus  = 0;
  int sensorBonus = 0;
  
  
  
  protected Blueprint(
    String name, String ID, String description, String spritePath
  ) {
    super(INDEX, ID);
    this.name = name;
    this.description = description;
    this.sprite = Kind.loadImage(spritePath);
    this.initStats();
  }
  
  
  void initStats() {
    return;
  }
  
  
  public static Blueprint loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public int buildCost  () { return buildCost  ; }
  public int visitLimit () { return visitLimit ; }
  public int maintenance() { return maintenance; }
  public int lifeSupport() { return lifeSupport; }
  public int powerCost  () { return powerCost  ; }
  
  
  protected abstract Room createRoom(Base base, int slotID);
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}











